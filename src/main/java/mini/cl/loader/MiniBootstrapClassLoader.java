package mini.cl.loader;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import mini.cl.MethodCaller;
import mini.cl.MiniClass;
import mini.data.area.MiniVirtualMachineMemory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 引导类加载器
 * 负责加载 Java 核心类库，如 java.lang.*、java.util.* 等
 */
public class MiniBootstrapClassLoader {
    public static MiniClass loadClass(String className) throws IOException {
        className = className.replace(".", "/")
                .replace("\\", "/");

        // 看是否是 demo/java/lang/ 下面的类，如果不是，则直接返回
        if (!className.startsWith("demo/java")) {
            return null;
        }

        /* TODO
            JVM 在判定两个 class 是否相同时，不仅要判断两个类名是否相同，而且要判断是否由同一个类加载器实例加载的。
            只有两者同时满足的情况下，JVM 才认为这两个 class 是相同的。
         */
        if (MiniVirtualMachineMemory.METHOD_AREA.CLASS_CACHE.containsKey(className)) {
            return MiniVirtualMachineMemory.METHOD_AREA.CLASS_CACHE.get(className);
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

        MiniVirtualMachineMemory.METHOD_AREA.CLASS_CACHE.put(className, clazz);
        return clazz;
    }

    /**
     * 1. 通过全类名获取定义此类的二进制字节流。
     * 2. 将字节流所代表的静态存储结构转换为方法区的运行时数据结构。
     * 3. 在内存中生成一个代表该类的 Class 对象，作为方法区这些数据的访问入口。
     */
    private static MiniClass _load(String className) throws IOException {
        String fileName = className + ".class";
        byte[] classData = FileUtil.readBytes(SystemUtil.getUserInfo().getCurrentDir() + "\\src\\main\\java\\" + fileName);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(classData));
        MiniClass clazz = new MiniClass(input);

        // 检查是否存在父类，如果存在就先加载父类
        clazz._loading_loadSuperClass();
        clazz.getSuperClass();

        return clazz;
    }

    private static void _initialization(MiniClass clazz) {
        // 先执行 <clinit> 方法
        MiniClass.MiniMemberInfo clinit = clazz.getMethod("<clinit>");
        if (clinit == null) return;
        MethodCaller.call(clazz, clinit, new HashMap<>());
    }


}
