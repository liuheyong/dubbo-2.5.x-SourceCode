package com.alibaba.dubbo.demo.provider.activate;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-02
 * @description:
 */
public class ActivateTest {

    public static void main(String[] args) {

        //================================Java SPI================================
        //ServiceLoader<Robot> serviceLoader = ServiceLoader.load(Robot.class);
        //System.out.println("Java SPI Test");
        //serviceLoader.forEach(Robot::sayHello);

        //================================Java Adaptive================================
        //Robot robot = ExtensionLoader.getExtensionLoader(Robot.class).getAdaptiveExtension();
        //URL url = URL.valueOf("test://localhost/test?robot=bumblebee");
        //robot.sayHello(url);

        //ExtensionLoader<Robot> extensionLoader = ExtensionLoader.getExtensionLoader(Robot.class);
        //Robot optimusPrime = extensionLoader.getExtension("optimusPrime");
        //optimusPrime.sayHello();
        //Robot bumblebee = extensionLoader.getExtension("bumblebee");
        //bumblebee.sayHello();

        //================================Java Activate================================
        //ExtensionLoader<Robot> extExtensionLoader = ExtensionLoader.getExtensionLoader(Robot.class);
        //URL url = URL.valueOf("test://localhost/test?activateKey2=activateValue");
        //List<Robot> list = extExtensionLoader.getActivateExtension(url, new String[]{}, "order");
        //list.forEach(o -> o.sayHello(o.getClass().getSimpleName()));

    }

}
