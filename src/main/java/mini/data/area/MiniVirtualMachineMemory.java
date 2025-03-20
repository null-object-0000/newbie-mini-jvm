package mini.data.area;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import mini.cl.MiniStackFrame;
import mini.data.structure.MiniStack;

/**
 * 模拟内存区域
 */
public class MiniVirtualMachineMemory {
    /**
     * 程序计数器
     */
    private static int pc;
    /**
     * 虚拟机栈（线程私有）
     * - 一个方法对应一个栈帧
     */
    public final static MiniStack<MiniStackFrame> VIRTUAL_STACK = new MiniStack<>();
    /**
     * 本地方法栈
     * @deprecated 这个是 C 或 C++ 类的本地方法才用到的
     */
    @Deprecated
    public final static MiniStack<MiniStackFrame> NATIVE_STACK = new MiniStack<>();
    /**
     * 方法区
     * JDK 1.8 之前采用永久代实现
     * JDK 1.8 及以后采用元空间实现
     */
    public final static MiniMetaSpace METHOD_AREA = new MiniMetaSpace();
    /**
     * 堆区
     */
    public final static MiniHeapArea HEAP_AREA = new MiniHeapArea();

    public static class MiniHeapArea {
        public final YoungGen YOUNG_GEN = new YoungGen();
        public final MiniHeap OLD_GEN = new MiniHeap(10 * 1024 * 1024);
    }

    public static class YoungGen {
        public final MiniHeap EDEN = new MiniHeap(10 * 1024 * 1024);
        public final MiniHeap SURVIVOR_0 = new MiniHeap(10 * 1024 * 1024);
        public final MiniHeap SURVIVOR_1 = new MiniHeap(10 * 1024 * 1024);
    }

    public static class MiniHeap {
        private int MOCK_POINTER = RandomUtil.randomInt(100000, 999999);
        private boolean cas = false;
        private int offset = 0;
        private final byte[] data;

        public MiniHeap(int size) {
            this.data = new byte[size];
        }

        /**
         * 申请内存
         * @return 内存地址
         */
        public int allocate(int size) {
            if (cas == false) {
                cas = true;
                try {
                    // 耗时操作
                    return MOCK_POINTER + offset;
                } finally {
                    cas = false;
                }
            } else {
                ThreadUtil.sleep(100);
                return allocate(size);
            }
        }

        public void write(int offset, String value) {
            offset = offset - MOCK_POINTER;

            for (int i = 0; i < value.length(); i++) {
                data[offset + i] = (byte) value.charAt(i);
            }
        }

        public String read(int offset, int size) {
            offset = offset - MOCK_POINTER;

            StringBuilder sb = new StringBuilder();
            for (int i = offset; i < offset + size; i++) {
                sb.append((char) data[i]);
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (byte datum : data) {
                sb.append((char) datum);
            }
            return sb.toString();
        }
    }
}
