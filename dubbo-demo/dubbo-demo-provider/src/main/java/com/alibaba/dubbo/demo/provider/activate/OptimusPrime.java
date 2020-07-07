package com.alibaba.dubbo.demo.provider.activate;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.Adaptive;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description:
 */
@Activate(group = {"order"})
//@Adaptive
public class OptimusPrime implements Robot {

    @Override
    public void sayHello(String url) {
        System.out.println("Hello, I am OptimusPrime");
    }

}
