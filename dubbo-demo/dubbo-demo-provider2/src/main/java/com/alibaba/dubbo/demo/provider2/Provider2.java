/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.demo.provider2;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Provider2 {

    public static void main(String[] args) throws Exception {
        //Prevent to get IPV6 address,this way only work in debug mode
        //But you can pass use -Djava.net.preferIPv4Stack=true,then it work well whether in debug mode or not
        System.setProperty("java.net.preferIPv4Stack", "true");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"META-INF/spring/dubbo-demo-provider2.xml"});
        context.start();

        System.in.read(); // press any key to exit
    }

}
//服务暴露：
//给一个service标签的serviceBean生成了一个代理好的Invoker，这个invoker放在一个叫exporter的对象下，
//然后这个exporter放在了serviceBean的一个exporters变量下，准备被调用，然后创建的nettyServer放在了
//serviceBean的变量protocol下的一个变量serverMap里面，这样一个serverBean的netty服务，方法代理类是
//不是都生成好了。这里注意protocol是单例生成的，所以如果有bean打开过nettyServer，别的bean就不会再打开。
//然后回到很前面的ServiceConfig的doExport里面：
//发现他把生成好了serviceBean放到了一个ApplicationModel里面，后面consumer也会放到这里面
