package mini.cl.loader;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import mini.cl.MiniClass;
import mini.data.area.MiniVirtualMachineMemory;

import java.io.IOException;

/**
 * 系统类加载器
 */
public final class MiniApplicationClassLoader extends MiniClassLoader {

    public MiniApplicationClassLoader() {
        super(null);
    }

    @Override
    public MiniClass loadClass(String className) throws IOException {
        // 双亲委派，优先找上一层级的类加载器尝试记载
        MiniClass clazz = MiniVirtualMachineMemory.METHOD_AREA.EXT_CLASS_LOADER.loadClass(className);
        if (clazz != null) return clazz;

        className = className.replace(".", "/")
                .replace("\\", "/");

        /* TODO
            JVM 在判定两个 class 是否相同时，不仅要判断两个类名是否相同，而且要判断是否由同一个类加载器实例加载的。
            只有两者同时满足的情况下，JVM 才认为这两个 class 是相同的。
         */
        if (MiniVirtualMachineMemory.METHOD_AREA.CLASS_CACHE.containsKey(className)) {
            return MiniVirtualMachineMemory.METHOD_AREA.CLASS_CACHE.get(className);
        }

        String fileName = className + ".class";
        byte[] classData = FileUtil.readBytes(SystemUtil.getUserInfo().getCurrentDir() + "\\src\\main\\java\\" + fileName);
        clazz = super.defineClass(classData);

        MiniVirtualMachineMemory.METHOD_AREA.CLASS_CACHE.put(className, clazz);

        return clazz;
    }
}
