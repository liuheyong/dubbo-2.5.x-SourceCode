/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.support.RpcUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractClusterInvoker
 *
 */
public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClusterInvoker.class);

    protected final Directory<T> directory;

    protected final boolean availablecheck;

    private AtomicBoolean destroyed = new AtomicBoolean(false);

    private volatile Invoker<T> stickyInvoker = null;

    public AbstractClusterInvoker(Directory<T> directory) {
        this(directory, directory.getUrl());
    }

    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if (directory == null)
            throw new IllegalArgumentException("service directory == null");

        this.directory = directory;
        //sticky: 在availablecheck为true时，应始终在使用之前检查invoker.isAvailable（）
        this.availablecheck = url.getParameter(Constants.CLUSTER_AVAILABLE_CHECK_KEY, Constants.DEFAULT_CLUSTER_AVAILABLE_CHECK);
    }

    public Class<T> getInterface() {
        return directory.getInterface();
    }

    public URL getUrl() {
        return directory.getUrl();
    }

    public boolean isAvailable() {
        Invoker<T> invoker = stickyInvoker;
        if (invoker != null) {
            return invoker.isAvailable();
        }
        return directory.isAvailable();
    }

    public void destroy() {
        if (destroyed.compareAndSet(false, true)) {
            directory.destroy();
        }
    }

    /**
     * Select a invoker using loadbalance policy.</br>
     * a)Firstly, select an invoker using loadbalance. If this invoker is in previously selected list, or, if this invoker is unavailable, then continue step b (reselect), otherwise return the first selected invoker</br>
     * b)Reslection, the validation rule for reselection: selected > available. This rule guarantees that the selected invoker has the minimum chance to be one in the previously selected list, and also guarantees this invoker is available.
     *
     * @param loadbalance load balance policy
     * @param invocation
     * @param invokers invoker candidates
     * @param selected  exclude selected invokers or not
     * @return
     * @throws
     */
    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0)
            return null;
        // 获取调用方法名
        String methodName = invocation == null ? "" : invocation.getMethodName();

        // =========================== 粘滞连接特性 ===========================
        // 获取 sticky 配置，sticky 表示粘滞连接。所谓粘滞连接是指让服务消费者尽可能的
        // 调用同一个服务提供者，除非该提供者挂了再进行切换
        boolean sticky = invokers.get(0).getUrl().getMethodParameter(methodName, Constants.CLUSTER_STICKY_KEY, Constants.DEFAULT_CLUSTER_STICKY);
        {
            // 检测 invokers 列表是否包含 stickyInvoker，如果不包含，
            // 说明 stickyInvoker 代表的服务提供者挂了，此时需要将其置空
            if (stickyInvoker != null && !invokers.contains(stickyInvoker)) {
                stickyInvoker = null;
            }
            // 在 sticky 为 true，且 stickyInvoker != null 的情况下。如果 selected 包含
            // stickyInvoker，表明 stickyInvoker 对应的服务提供者可能因网络原因未能成功提供服务。
            // 但是该提供者并没挂，此时 invokers 列表中仍存在该服务提供者对应的 Invoker。
            if (sticky && stickyInvoker != null && (selected == null || !selected.contains(stickyInvoker))) {
                // 如果此时可用性检查通过的话该Invoker可继续提供服务。
                if (availablecheck && stickyInvoker.isAvailable()) {
                    return stickyInvoker;
                }
            }
        }

        // =========================== LB选择 Invoker ===========================
        // 如果线程走到当前代码处，说明前面的 stickyInvoker 为空，或者不可用。
        // TODO  此时继续调用 doSelect 选择 Invoker
        Invoker<T> invoker = doselect(loadbalance, invocation, invokers, selected);

        // 如果 sticky 为 true，则将负载均衡组件选出的 Invoker 赋值给 stickyInvoker
        if (sticky) {
            stickyInvoker = invoker;
        }
        return invoker;
    }

    private Invoker<T> doselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0)
            return null;
        if (invokers.size() == 1)
            return invokers.get(0);
        // 如果只有两个invoker，退化成轮循【新版本中删除了这段逻辑】
        if (invokers.size() == 2 && selected != null && selected.size() > 0) {
            return selected.get(0) == invokers.get(0) ? invokers.get(1) : invokers.get(0);
        }

        // TODO  通过负载均衡组件选择 Invoker
        Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);

        // 如果 selected 包含负载均衡选择出的 Invoker，或者该 Invoker 无法经过可用性检查，此时进行重选
        if ((selected != null && selected.contains(invoker)) || (!invoker.isAvailable() && getUrl() != null && availablecheck)) {
            try {
                // TODO  通过负载均衡重新选择 Invoker
                Invoker<T> rinvoker = reselect(loadbalance, invocation, invokers, selected, availablecheck);

                // 如果 rinvoker 不为空，则将其赋值给 invoker
                if (rinvoker != null) {
                    invoker = rinvoker;
                } else {
                    // rinvoker 为空，定位 invoker 在 invokers 中的位置
                    int index = invokers.indexOf(invoker);
                    try {
                        // 避免碰撞
                        // 获取 index + 1 位置处的 Invoker，以下代码等价于：
                        //     invoker = invokers.get((index + 1) % invokers.size());
                        invoker = index < invokers.size() - 1 ? invokers.get(index + 1) : invoker;
                    } catch (Exception e) {
                        logger.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
                    }
                }
            } catch (Throwable t) {
                logger.error("clustor relselect fail reason is :" + t.getMessage() + " if can not slove ,you can set cluster.availablecheck=false in url", t);
            }
        }
        return invoker;
    }

    /**
     * Reselect, use invokers not in `selected` first, if all invokers are in `selected`, just pick an available one using loadbalance policy.
     *
     * @param loadbalance
     * @param invocation
     * @param invokers
     * @param selected
     * @return
     * @throws RpcException
     */
    private Invoker<T> reselect(LoadBalance loadbalance, Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected, boolean availablecheck) throws RpcException {

        //如果有可能预先分配一个，肯定会使用此list
        List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>(invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());

        //First, try picking a invoker not in `selected`.
        // 根据 availablecheck 进行不同的处理
        if (availablecheck) { // invoker.isAvailable() should be checked
            // 遍历 invokers 列表
            for (Invoker<T> invoker : invokers) {
                // 检测可用性
                if (invoker.isAvailable()) {
                    // 如果 selected 列表不包含当前 invoker，则将其添加到 reselectInvokers 中
                    if (selected == null || !selected.contains(invoker)) {
                        reselectInvokers.add(invoker);
                    }
                }
            }
            // reselectInvokers 不为空，此时通过负载均衡组件进行选择
            if (reselectInvokers.size() > 0) {
                return loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        // 不检查 Invoker 可用性
        } else { // do not check invoker.isAvailable()
            for (Invoker<T> invoker : invokers) {
                // 如果 selected 列表不包含当前 invoker，则将其添加到 reselectInvokers 中
                if (selected == null || !selected.contains(invoker)) {
                    reselectInvokers.add(invoker);
                }
            }
            // 通过负载均衡组件进行选择
            if (reselectInvokers.size() > 0) {
                return loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }
        // 只需使用负载均衡策略选择一个可用的调用程序
        {
            // 若线程走到此处，说明 reselectInvokers 集合为空，此时不会调用负载均衡组件进行筛选。
            // 这里从 selected 列表中查找可用的 Invoker，并将其添加到 reselectInvokers 集合中
            if (selected != null) {
                for (Invoker<T> invoker : selected) {
                    if ((invoker.isAvailable()) && !reselectInvokers.contains(invoker)) {
                        reselectInvokers.add(invoker);
                    }
                }
            }
            if (reselectInvokers.size() > 0) {
                return loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }
        return null;
    }

    public Result invoke(final Invocation invocation) throws RpcException {

        checkWhetherDestroyed();

        LoadBalance loadbalance = null;

        // 绑定 attachments 到 invocation 中【新版本中添加的代码】.
        //Map<String, String> contextAttachments = RpcContext.getContext().getAttachments();
        //if (contextAttachments != null && contextAttachments.size() != 0) {
        //    ((RpcInvocation) invocation).addAttachments(contextAttachments);
        //}

        // 列举 Invoker
        List<Invoker<T>> invokers = list(invocation);
        if (invokers != null && invokers.size() > 0) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl().getMethodParameter(invocation.getMethodName(), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        } else {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(Constants.DEFAULT_LOADBALANCE);
        }
        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
        // 调用 doInvoke 进行后续操作
        return doInvoke(invocation, invokers, loadbalance);
    }

    protected void checkWhetherDestroyed() {

        if (destroyed.get()) {
            throw new RpcException("Rpc cluster invoker for " + getInterface() + " on consumer " + NetUtils.getLocalHost()
                    + " use dubbo version " + Version.getVersion()
                    + " is now destroyed! Can not invoke any more.");
        }
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + getUrl().toString();
    }

    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (invokers == null || invokers.size() == 0) {
            throw new RpcException("Failed to invoke the method "
                    + invocation.getMethodName() + " in the service " + getInterface().getName()
                    + ". No provider available for the service " + directory.getUrl().getServiceKey()
                    + " from registry " + directory.getUrl().getAddress()
                    + " on the consumer " + NetUtils.getLocalHost()
                    + " using the dubbo version " + Version.getVersion()
                    + ". Please check if the providers have been started and registered.");
        }
    }

    protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers,
                                       LoadBalance loadbalance) throws RpcException;

    protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
        // 调用 Directory 的 list 方法列举 Invoker
        List<Invoker<T>> invokers = directory.list(invocation);
        return invokers;
    }
}
