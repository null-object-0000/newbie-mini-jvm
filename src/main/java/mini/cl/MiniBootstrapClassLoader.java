package mini.cl;

import cn.hutool.core.io.FileUtil;
import mini.data.area.MiniMetaSpace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MiniBootstrapClassLoader {
    private final static String CLASS_PATH = "C:\\Users\\nicha\\Code\\newbie-mini-jvm\\src\\main\\java\\demo\\";

    public static MiniClass loadClass(String className) throws IOException {
        if (MiniMetaSpace.CLASS_CACHE.containsKey(className)) {
            return MiniMetaSpace.CLASS_CACHE.get(className);
        }

        // 第一阶段：加载 Loading
        MiniClass clazz = _load(className);

        // 第二阶段：链接 Linking
        // 1. 验证 Verify
        clazz._linking_verify();
        // 2. 准备 Prepare
        clazz._linking_prepare();
        // 3. 解析 Resolve
        clazz._linking_resolve();

        // 第三阶段：初始化 Initialization

        MiniMetaSpace.CLASS_CACHE.put(className, clazz);
        return clazz;
    }

    /**
     * 1. 通过全类名获取定义此类的二进制字节流。
     * 2. 将字节流所代表的静态存储结构转换为方法区的运行时数据结构。
     * 3. 在内存中生成一个代表该类的 Class 对象，作为方法区这些数据的访问入口。
     */
    private static MiniClass _load(String className) {
        String fileName = className.replace(".", "/") + ".class";
        byte[] classData = FileUtil.readBytes(CLASS_PATH + fileName);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(classData));
        return new MiniClass(input);
    }

    private static void readAndCheckMagic(DataInputStream input) throws IOException {
        int magic = input.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IOException("Magic number incorrect! Expect 0xCAFEBABE but was " + Integer.toHexString(magic));
        }
    }
}
