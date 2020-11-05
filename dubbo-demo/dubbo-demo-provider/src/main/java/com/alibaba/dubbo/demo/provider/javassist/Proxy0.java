package com.alibaba.dubbo.demo.provider.javassist;

import com.alibaba.dubbo.common.bytecode.ClassGenerator;
import com.alibaba.dubbo.demo.DemoService;
import com.alibaba.dubbo.rpc.service.EchoService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-08
 * @description: javassist生成的代理类示例
 */
public class Proxy0 implements ClassGenerator.DC, EchoService, DemoService {

    /* 方法数组 */
    public static Method[] methods;
    private InvocationHandler handler;

    public Proxy0(InvocationHandler invocationHandler) {
        this.handler = invocationHandler;
    }

    public Proxy0() {
    }

    @Override
    public String sayHello(String string) throws Throwable {
        // 将参数存储到 Object 数组中
        Object[] arrobject = new Object[]{string};
        // 调用 InvocationHandler 实现类的 invoke 方法得到调用结果
        Object object = this.handler.invoke(this, methods[0], arrobject);
        // 返回调用结果
        return (String) object;
    }

    @Override
    public String sayBye(String name) {
        return null;
    }

    /**
     * 回声测试方法
     */
    @Override
    public Object $echo(Object object) throws Throwable {
        Object[] arrobject = new Object[]{object};
        Object object2 = this.handler.invoke(this, methods[1], arrobject);
        return object2;
    }
}

