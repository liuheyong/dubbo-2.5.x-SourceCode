package com.alibaba.dubbo.demo.consumer.random;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-12
 * @description:
 */
public class ThreadLocalRandomDemo {

    // TODO 正确地使用 ThreadLocalRandom，肯定需要给每个线程初始化一个 seed，那就需要调用
    //   ThreadLocalRandom.current() 方法。那么有个疑问，在每个线程里都调用 ThreadLocalRandom.current()，
    //   会产生多个 ThreadLocalRandom 实例吗,不会的，源码中有判断，如果已经有了，就不会再进行创建。
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Player().start();
        }
    }

    private static class Player extends Thread {
        @Override
        public void run() {
            System.out.println(getName() + ": " + RANDOM.nextInt(100));
        }
    }

}

//Thread-0: 4
//Thread-1: 4
//Thread-2: 4
//Thread-3: 4
//Thread-4: 4
//Thread-5: 4
//Thread-6: 4
//Thread-7: 4
//Thread-8: 4
//Thread-9: 4
//UNSAFE类是JDK中rt.jar包提供的硬件级别的原子性操作类，UNSAFE.putLong(t, SEED, seed);这里不是把seed以键值
//对存放进UNSAFE，而是放入Thread的实例t中，SEED是Thread实例中变量threadLocalRandomSeed的偏移量，
//也就是内存地址。这样每个线程才能维护一个种子变量，而ThreadLocalRandom中不维护种子变量，只是起到一个工
//具类的作用，跟ThreadLocal作用一样。
//你这里为什么多个线程出现重复随机数，这里current()操作的是主线程，也就是把初始种子设置到主线程中，而在RANDOM.nex
//tInt中又会从当前线程中取出种子，因为当前线程的种子没有初始化赋值，所以值为默认值0，即所有线程的初始种子
//值为0，又因为老种子生成新种子的算法是固定，所以所有线程的新种子是一样，导致所有线程产生的随机数都是一样的。
