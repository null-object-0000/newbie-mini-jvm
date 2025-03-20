package demo;

import cn.hutool.core.thread.ThreadUtil;

public class HelloHeap {
    public static void main(String[] args) {
        System.out.println("Hello Heap");
        ThreadUtil.sleep(200000000);
    }
}
