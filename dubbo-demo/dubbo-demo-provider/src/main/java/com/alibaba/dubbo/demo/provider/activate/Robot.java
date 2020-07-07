package com.alibaba.dubbo.demo.provider.activate;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description: Java @activate演示
 */
@SPI
public interface Robot {

    void sayHello(String url);

}
