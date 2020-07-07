package com.alibaba.dubbo.demo.provider.activate;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-07
 * @description:
 */
@Activate(value = {"activateKey2"}, group = {"order"})
public class ValueActivateImpl implements Robot{

    @Override
    public void sayHello(String url) {
        System.out.println("Hello, I am ValueActivateImpl.");
    }

}
