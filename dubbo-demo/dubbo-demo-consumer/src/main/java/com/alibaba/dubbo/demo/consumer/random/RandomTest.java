package com.alibaba.dubbo.demo.consumer.random;

import com.alibaba.dubbo.rpc.RpcException;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-11
 * @description:
 */
public class RandomTest {

    //public static void main(String[] args) {
    //    Random random = new Random();
    //    for (int i = 0; i < 10000; i++) {
    //        Consumer.service.execute(() -> System.out.println(random.nextInt()));
    //    }
    //}

    public static void main(String[] args) {
        RpcException le = null;
        for (int i = 0; i < 3; i++) {
            try {
                if (1 == 1) {
                    throw new RpcException(3);
                }
            } catch (RpcException e) {
                le = e;
            } catch (Throwable e) {
                le = new RpcException(e.getMessage(), e);
            } finally {
            }
        }
        System.out.println(11111);
    }

}
