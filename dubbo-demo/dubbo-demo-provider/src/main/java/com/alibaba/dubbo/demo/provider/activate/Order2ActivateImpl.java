package com.alibaba.dubbo.demo.provider.activate;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-07
 * @description:
 */
@Activate(order = 2, group = {"order"})
public class Order2ActivateImpl implements Robot{

    @Override
    public void sayHello(String url) {
        System.out.println("Hello, I am Order2ActivateImpl.");
    }

}
