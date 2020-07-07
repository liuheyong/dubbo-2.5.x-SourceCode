package com.alibaba.dubbo.demo.provider.activate;

import com.alibaba.dubbo.common.extension.Activate;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-07
 * @description:
 */
@Activate(after = "optimusPrime",group = "order")
public class BeforeActivateImpl implements Robot{

    @Override
    public void sayHello(String url) {
        System.out.println("Hello, I am BeforeActivateImpl.");
    }

}
