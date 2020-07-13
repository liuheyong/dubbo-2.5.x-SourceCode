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

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.AtomicPositiveInteger;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Round robin load balance.
 *
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "roundrobin";

    // key = 全限定类名 + "." + 方法名，比如 com.xxx.DemoService.sayHello
    // value AtomicPositiveInteger 初始值为0
    private final ConcurrentMap<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<>();

    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {

        // key = 全限定类名 + "." + 方法名，比如 com.xxx.DemoService.sayHello
        String key = invokers.get(0).getUrl().getServiceKey() + "." + invocation.getMethodName();
        int length = invokers.size(); // Number of invokers
        // 最大权重
        int maxWeight = 0;
        // 最小权重
        int minWeight = Integer.MAX_VALUE;
        // invoker和对应的权重map
        final LinkedHashMap<Invoker<T>, IntegerWrapper> invokerToWeightMap = new LinkedHashMap<>();
        // 权重总和
        int weightSum = 0;

        // 下面这个循环主要用于查找最大和最小权重，计算权重总和等
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            // 获取最大和最小权重
            maxWeight = Math.max(maxWeight, weight); // Choose the maximum weight
            minWeight = Math.min(minWeight, weight); // Choose the minimum weight
            if (weight > 0) {
                // 将invoker weight 封装到 invokerToWeightMap 中
                invokerToWeightMap.put(invokers.get(i), new IntegerWrapper(weight));
                // 累加权重
                weightSum += weight;
            }
        }

        // key = 全限定类名 + "." + 方法名，比如 com.xxx.DemoService.sayHello
        // value AtomicPositiveInteger 初始值为0
        AtomicPositiveInteger sequence = sequences.get(key);
        if (sequence == null) {
            // TODO 用初始值{@code 0}创建一个新的AtomicPositiveInteger
            sequences.putIfAbsent(key, new AtomicPositiveInteger());
            sequence = sequences.get(key);
        }

        // TODO sequence.getAndIncrement()是保证多线程安全的关键
        int currentSequence = sequence.getAndIncrement();
        //如果最小权重和最大权重不相等，说明各个invoker权重都不相等
        if (maxWeight > 0 && minWeight < maxWeight) {
            //对总权重取模，用于轮询取invoker。
            //举个例子:假如invoker有两个，权重分别为2和3，总权重为5，那么mod就有5中可能取值——0、1、2、3、4
            //如果mod <= 1 ,在内层循环中一遍就能找到invoker
            //如果mod >= 2 ,就要在外层循环控制总的循环次数，内层循环每次抹掉每个invoker的一个权重并且mod减一
            //直到mod=0而且存在首次的一个invoker的权重不为0，这个invoker就返回
            int mod = currentSequence % weightSum;
            //取所有invoker中最大的作为总的遍历次数
            for (int i = 0; i < maxWeight; i++) {
                //每次遍历所有的invoker，如果不满足条件，对于每一个invoker都抹掉一个权重
                for (Map.Entry<Invoker<T>, IntegerWrapper> each : invokerToWeightMap.entrySet()) {
                    final Invoker<T> k = each.getKey();
                    final IntegerWrapper v = each.getValue();
                    if (mod == 0 && v.getValue() > 0) {
                        return k;
                    }
                    if (v.getValue() > 0) {
                        v.decrement();
                        mod--;
                    }
                }
            }
        }
        // 服务提供者之间的权重相等，此时直接通过轮询选择 Invoker —— 依次交替获取invoker
        return invokers.get(currentSequence % length);
    }

    private static final class IntegerWrapper {

        private int value;

        public IntegerWrapper(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public void decrement() {
            this.value--;
        }
    }

}