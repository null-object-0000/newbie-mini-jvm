package mini.cl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import mini.data.area.MiniMetaSpace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public class MiniBootstrapClassLoader {
    public static MiniClass loadClass(String className) throws IOException {
        className = className.replace(".", "/")
                .replace("\\", "/");

        /* TODO
            JVM 在判定两个 class 是否相同时，不仅要判断两个类名是否相同，而且要判断是否由同一个类加载器实例加载的。
            只有两者同时满足的情况下，JVM 才认为这两个 class 是相同的。
         */
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
        _initialization(clazz);

        MiniMetaSpace.CLASS_CACHE.put(className, clazz);
        return clazz;
    }

    /**
     * 1. 通过全类名获取定义此类的二进制字节流。
     * 2. 将字节流所代表的静态存储结构转换为方法区的运行时数据结构。
     * 3. 在内存中生成一个代表该类的 Class 对象，作为方法区这些数据的访问入口。
     */
    private static MiniClass _load(String className) {
        String fileName = className + ".class";
        byte[] classData = FileUtil.readBytes(SystemUtil.getUserInfo().getCurrentDir() + "\\src\\main\\java\\" + fileName);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(classData));
        return new MiniClass(input);
    }

    private static void _initialization(MiniClass clazz) {
        // 先执行 <clinit> 方法
        MiniClass.MiniMemberInfo clinit = clazz.getMethod("<clinit>");
        if (clinit == null) return;
        MethodCaller.call(clazz, clinit, new HashMap<>());
    }


}
