package com.alibaba.dubbo.common.threadpool;

/**
 * @author: wenyixicodedog
 * @create: 2020-09-02
 * @description:
 */
public class PrintOrder {

    public static void main(String[] args) {
        //人数
        final int n = 10;
        //起始报数
        final int k = 1;
        //报数步长
        final int m = 2;
        startPrint(n, k, m);
    }

    public static void startPrint(int n, int k, int m) {
        //声明数组，长度为人的数量n
        int[] arrays = new int[n];
        //填充数组
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = i + 1;
        }
        int curr = 0;
        int num = k;
        int j = 1;
        while (j <= n) {
            if (arrays[num - 1] != 0) {
                curr++;
                if (curr == m) {
                    System.out.println(arrays[num - 1] + "出队");
                    arrays[num - 1] = 0;
                    j++;
                    curr = 0;
                }
            }
            num++;
            if (num > n)
                num = 1;
        }
    }
}
