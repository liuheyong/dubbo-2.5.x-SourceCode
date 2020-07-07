package com.alibaba.dubbo.demo.provider.javaspi;

import com.alibaba.dubbo.common.extension.ExtensionLoader;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-03
 * @description:
 */
public class Robot$Adaptive implements com.alibaba.dubbo.demo.provider.javaspi.Robot {

    public void sayHello(com.alibaba.dubbo.common.URL arg0) {
        if (arg0 == null) throw new IllegalArgumentException("url == null");
        com.alibaba.dubbo.common.URL url = arg0;
        String extName = url.getParameter("robot");
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.demo.provider.activate.Robot) name from url(" + url.toString() + ") use keys([robot])");
        com.alibaba.dubbo.demo.provider.javaspi.Robot extension =
                ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.demo.provider.javaspi.Robot.class).getExtension(extName);
        extension.sayHello(arg0);
    }

}
