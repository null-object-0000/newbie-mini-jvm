package mini.cl.loader;

import mini.cl.MiniClass;

import java.io.IOException;

/**
 * 扩展类加载器
 */
public final class MiniExtensionClassLoader extends MiniClassLoader {

    public MiniExtensionClassLoader() {
        super(null);
    }

    @Override
    public MiniClass loadClass(String className) throws IOException {
        MiniClass clazz = MiniBootstrapClassLoader.loadClass(className);
        if (clazz != null) return clazz;

        // TODO
        return null;
    }
}
