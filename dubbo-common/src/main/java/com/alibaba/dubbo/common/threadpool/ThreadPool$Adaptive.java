package com.alibaba.dubbo.common.threadpool;

import com.alibaba.dubbo.common.extension.ExtensionLoader;

/**
 * @author: wenyixicodedog
 * @create: 2020-07-09
 * @description:
 */
public class ThreadPool$Adaptive implements com.alibaba.dubbo.common.threadpool.ThreadPool {

    public java.util.concurrent.Executor getExecutor(com.alibaba.dubbo.common.URL arg0) {
        if (arg0 == null) throw new IllegalArgumentException("url == null");
        com.alibaba.dubbo.common.URL url = arg0;
        String extName = url.getParameter("threadpool", "fixed");
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.common.threadpool.ThreadPool) name from url" +
                    "(" + url.toString() + ") use keys([threadpool])");
        com.alibaba.dubbo.common.threadpool.ThreadPool extension =
                ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.common.threadpool.ThreadPool.class).getExtension(extName);
        return extension.getExecutor(arg0);
    }
}
