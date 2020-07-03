package com.alibaba.dubbo.demo.provider.javaspi;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.URL;
/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description: Java SPI演示
 */
@SPI(value = "optimusPrime")
public interface Robot {

    @Adaptive(value = "bumblebee")
    void sayHello(URL url);

}
