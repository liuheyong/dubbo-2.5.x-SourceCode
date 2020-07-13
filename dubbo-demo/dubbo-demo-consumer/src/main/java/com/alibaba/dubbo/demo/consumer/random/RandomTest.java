package com.alibaba.dubbo.demo.consumer.random;

import com.alibaba.dubbo.demo.consumer.Consumer;

import java.util.Random;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-11
 * @description:
 */
public class RandomTest {
    public static void main(String[] args) {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            Consumer.service.execute(() -> System.out.println(random.nextInt()));
        }
    }
}
