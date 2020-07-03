package com.alibaba.dubbo.demo.provider.javaspi;

import com.alibaba.dubbo.common.URL;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description:
 */
public class OptimusPrime implements Robot {

    @Override
    public void sayHello(URL url) {
        System.out.println("Hello, I am Optimus Prime.");
    }

}
