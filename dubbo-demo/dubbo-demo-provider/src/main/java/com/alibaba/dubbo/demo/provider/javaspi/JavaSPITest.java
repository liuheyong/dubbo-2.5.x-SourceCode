package com.alibaba.dubbo.demo.provider.javaspi;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;

import java.util.ServiceLoader;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description:
 */
public class JavaSPITest {

    public static void main(String[] args) {
        //ServiceLoader<Robot> serviceLoader = ServiceLoader.load(Robot.class);
        //System.out.println("Java SPI Test");
        //serviceLoader.forEach(Robot::sayHello);

        Robot robot = ExtensionLoader.getExtensionLoader(Robot.class).getAdaptiveExtension();
        URL url = URL.valueOf("test://localhost/test?robot=bumblebee");
        robot.sayHello(url);

        //ExtensionLoader<Robot> extensionLoader = ExtensionLoader.getExtensionLoader(Robot.class);
        //
        //Robot optimusPrime = extensionLoader.getExtension("optimusPrime");
        //optimusPrime.sayHello();
        //Robot bumblebee = extensionLoader.getExtension("bumblebee");
        //bumblebee.sayHello();
    }

}
