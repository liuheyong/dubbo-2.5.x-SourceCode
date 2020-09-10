package com.alibaba.dubbo.common.threadpool;

/**
 * @author: wenyixicodedog
 * @create: 2020-09-08
 * @description:
 */
public class PrintABC {

    public static void main(String[] args) {
        new Thread(new MyThread("A")).start();
        new Thread(new MyThread("B")).start();
        new Thread(new MyThread("C")).start();
    }

}

class MyThread implements Runnable {
    private String name;
    static int n = 1;

    public MyThread(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (n > 100) {
                    break;
                }
                if (name.equals("A")) {
                    if (n % 3 == 1) {
                        System.out.println(name + "输出：" + n);
                        n++;
                    }
                }
                if (name.equals("B")) {
                    if (n % 3 == 2) {
                        System.out.println(name + "输出：" + n);
                        n++;
                    }
                }
                if (name.equals("C")) {
                    if (n % 3 == 0) {
                        System.out.println(name + "输出：" + n);
                        n++;
                    }
                }
            }
        }
    }
}