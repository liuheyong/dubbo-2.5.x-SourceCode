package com.alibaba.dubbo.demo.provider.javaspi;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description:
 */
//@Adaptive
public class Bumblebee implements Robot {

    @Override
    public void sayHello(URL url) {
        System.out.println("Hello, I am Bumblebee.");
    }

}
