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
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcStatus;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * LeastActiveLoadBalance
 *
 */
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "leastactive";

    //private final Random random = new Random();

    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        // invoker的数量
        int length = invokers.size();
        // 存储invoker中最少连接
        int leastActive = -1;
        // 具有相同“最小活跃数”的服务者提供者数量
        int leastCount = 0;
        // 具有相同“最小活跃数”的服务者提供者数量的下标，
        int[] leastIndexs = new int[length];
        // 总权重，用于在最少连接相等的invoker中进行根据权重随机获取invoker的过程中随机取下标
        int totalWeight = 0;
        // 第一个最小活跃数的 Invoker 权重值，用于与其他具有相同最小活跃数的 Invoker 的权重进行对比，
        // 以检测是否“所有具有相同最小活跃数的 Invoker 的权重”均相等
        int firstWeight = 0;
        // 用于记录是否所有具有相同“最小活跃数”的服务者提供者的权重都相等
        boolean sameWeight = true;
        // TODO 首先遍历所有的invoker计算并记录每个invoker的权重、最少连接，并找出最少连接及最少连接count
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            // 获取 Invoker 对应的活跃数
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive(); // Active number
            // 获取权重  ⭐️
            int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT); // Weight
            // 找到最小活动值的invoker时重新开始
            if (leastActive == -1 || active < leastActive) {
                leastActive = active; // 使用当前活跃数 active 更新最小活跃数 leastActive
                leastCount = 1; // 更新 leastCount 为 1
                leastIndexs[0] = i; // 记录当前下标值到 leastIndexs 中
                totalWeight = weight; // 记录总权重
                firstWeight = weight; // 记录上一个invoker权重
                sameWeight = true; // 记录invoker权重是否相等
            } else if (active == leastActive) { // 当前 Invoker 的活跃数 active 与最小活跃数 leastActive 相同
                leastIndexs[leastCount++] = i; // 在 leastIndexs 中记录下当前 Invoker 在 invokers 集合中的下标
                totalWeight += weight; // 累加权重
                // 检测当前 Invoker 的权重与 firstWeight 是否相等，
                // 不相等则将 sameWeight 置为 false
                if (sameWeight && i > 0 && weight != firstWeight) {
                    sameWeight = false;
                }
            }
        }
        // 当只有一个 Invoker 具有最小活跃数
        if (leastCount == 1) {
            // 直接返回该 Invoker
            return invokers.get(leastIndexs[0]);
        }
        // 有多个 Invoker 具有相同的最小活跃数，但它们之间的权重不同
        if (!sameWeight && totalWeight > 0) {
            // 随机生成一个 [0, totalWeight) 之间的数字
            int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);
            // 循环让随机数减去具有最小活跃数的 Invoker 的权重值，
            // 当 offset 小于等于0时，返回相应的 Invoker
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexs[i];
                // 获取权重值，并让随机数减去权重值  ⭐️
                offsetWeight -= getWeight(invokers.get(leastIndex), invocation);
                if (offsetWeight <= 0)
                    return invokers.get(leastIndex);
            }
        }
        // 如果权重相同或权重为0时，随机返回一个 Invoker
        return invokers.get(leastIndexs[ThreadLocalRandom.current().nextInt(leastCount)]);
    }
}