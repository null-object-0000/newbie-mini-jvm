package mini.cl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mini.cl.loader.MiniClassLoader;
import mini.data.area.MiniConstantPool;
import mini.data.area.MiniVirtualMachineMemory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassFile {
 * u4             magic;
 * u2             minor_version;
 * u2             major_version;
 * u2             constant_pool_count;
 * cp_info        constant_pool[constant_pool_count-1];
 * u2             access_flags;
 * u2             this_class;
 * u2             super_class;
 * u2             interfaces_count;
 * u2             interfaces[interfaces_count];
 * u2             fields_count;
 * field_info     fields[fields_count];
 * u2             methods_count;
 * method_info    methods[methods_count];
 * u2             attributes_count;
 * attribute_info attributes[attributes_count];
 * }
 * 参考：https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html
 */
public class MiniClass {
    @Getter
    private final DataInputStream input;
    @Getter
    private MiniClassLoader classLoader;

    @Getter
    private final Map<String, Object> staticVariables = new HashMap<>();

    private int magic; // 魔数，固定值0xCAFEBABE
    private int minorVersion; // 次版本号
    private int majorVersion; // 主版本号
    private int accessFlags; // 访问标志
    private int thisClass; // 当前类的索引
    private int superClass; // 父类的索引
    private int[] interfaces; // 接口列表
    @Getter
    private MiniMemberInfo[] fields; // 字段数量
    @Getter
    private MiniMemberInfo[] methods; // 方法数量

    public MiniClass(DataInputStream input) {
        this.input = input;
    }

    public MiniClass(DataInputStream input, MiniClassLoader classLoader) {
        this.input = input;
        this.classLoader = classLoader;
    }

    public String getName() {
        String name = (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(this).getConstant((Integer) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(this).getConstant(thisClass));
        return name.replace("/", "."); // 替换斜杠为点
    }

    public MiniClass getSuperClass() throws IOException {
        String superClassName = (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(this).getConstant((Integer) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(this).getConstant(superClass));
        superClassName = superClassName
                .replace("java/lang/", "demo/java/lang/Mini")
                .replace("java/io/", "demo/java/io/Mini")
                .replace("/", ".");

        if (this.getName().equals(superClassName)) {
            return null;
        }

        return MiniVirtualMachineMemory.METHOD_AREA.APP_CLASS_LOADER.loadClass(superClassName);
    }

    public MiniClass[] getInterfaces() {
        MiniClass[] interfaceClasses = new MiniClass[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            String interfaceName = (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(this).getConstant(interfaces[i]);
            // TODO: 基于 MiniBootstrapClassLoader 通过类名获取类对象
            interfaceClasses[i] = null;
        }
        return interfaceClasses;
    }

    /**
     * 在加载阶段提前读取父类信息
     */
    public MiniClass _loading_loadSuperClass() throws IOException {
        this.readAndCheckMagic();
        this.readAndCheckVersion();

        // 读取常量池
        MiniVirtualMachineMemory.METHOD_AREA.putConstantPool(this, MiniConstantPool.read(input));

        // 读取访问标志
        this.accessFlags = input.readUnsignedShort();

        // 读取当前类的索引
        this.thisClass = input.readUnsignedShort();
        // 读取父类的索引
        this.superClass = input.readUnsignedShort();

        input.reset();

        return this;
    }

    /**
     * 1. 文件格式验证（Class 文件格式检查）
     * 2. 元数据验证（字节码语义检查）
     * 3. 字节码验证（程序语义检查）
     * 4. 符号引用验证（类的正确性检查）
     * https://javaguide.cn/java/jvm/class-loading-process.html#%E9%AA%8C%E8%AF%81
     */
    public MiniClass _linking_verify() throws IOException {
        System.out.println("Verify");

        this.readAndCheckMagic();
        this.readAndCheckVersion();

        // 读取常量池
        MiniVirtualMachineMemory.METHOD_AREA.putConstantPool(this, MiniConstantPool.read(input));

        // 读取访问标志
        this.accessFlags = input.readUnsignedShort();

        // 读取当前类的索引
        this.thisClass = input.readUnsignedShort();
        // 读取父类的索引
        this.superClass = input.readUnsignedShort();

        // 读取接口
        this.readInterfaces();
        // 读取字段
        this.readFields();
        // 读取方法
        this.readMethods();
        // 读取属性
        this.readAttributes();

        // 确认是否还有数据
        if (input.available() > 0) {
            throw new IOException("Extra data found in class file");
        }

        // 关闭输入流
        input.close();

        return this;
    }

    public MiniClass _linking_prepare() {
        System.out.println("Prepare: " + this.getName());

        for (MiniMemberInfo field : fields) {
            // 识别是否是静态变量
            int accessFlags = field.getAccessFlags();
            if ((accessFlags & 0x0008) != 0) {
                String descriptor = field.getDescriptor();
                if (descriptor.equals("I")) {
                    System.out.println("    " + field.getName() + " = 0");
                    this.staticVariables.put(field.getName(), 0);
                } else if (descriptor.startsWith("L")) {
                    String className = descriptor.substring(1, descriptor.length() - 1);
                    System.out.println("    " + field.getName() + " = " + className);
                } else {
                    System.err.println("Warning: Unrecognized field descriptor: " + descriptor);
                }
            }
        }
        return this;
    }

    public MiniClass _linking_resolve() {
        System.out.println("Resolve: " + this.getName());
        return this;
    }

    private void readAndCheckMagic() throws IOException {
        magic = input.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IOException("Magic number incorrect! Expect 0xCAFEBABE but was " + Integer.toHexString(magic));
        }
    }

    private void readAndCheckVersion() throws IOException {
        minorVersion = input.readUnsignedShort();
        majorVersion = input.readUnsignedShort();
    }

    private void readInterfaces() throws IOException {
        interfaces = new int[input.readUnsignedShort()];
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = input.readUnsignedShort();
        }
    }

    private void readFields() throws IOException {
        fields = MiniMemberInfo.read(this, input);
    }

    private void readMethods() throws IOException {
        methods = MiniMemberInfo.read(this, input);

        for (MiniMemberInfo method : methods) {
            for (int i = 0; i < method.attributesCount; i++) {
                MiniAttributeInfo attribute = method.getAttributes()[i];
                if ("Code".equals(attribute.getAttributeName())) {
                    MiniCodeAttribute codeAttribute = MiniCodeAttribute.read(attribute);
                    method.getAttributes()[i] = codeAttribute;
                }
            }
        }
    }

    private void readAttributes() throws IOException {
        int attributesCount = input.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            int attributeNameIndex = input.readUnsignedShort();
            int attributeLength = input.readInt();
            byte[] attributeInfo = new byte[attributeLength];
            input.readFully(attributeInfo);
        }
    }

    public MiniMemberInfo getMethod(String name) {
        return Arrays.stream(this.methods)
                .filter(m -> m.getName().equals(name))
                .findFirst().orElse(null);
    }

    public int getInstanceSize() {
        // 计算实例大小
        int size = 0;
        for (MiniMemberInfo field : fields) {
            // 判断是否是静态变量，是的话跳过
            if ((field.getAccessFlags() & 0x0008) != 0) continue;

            String descriptor = field.getDescriptor();
            if (descriptor.equals("I")) {
                size += 4; // int 占用 4 字节
            } else if (descriptor.startsWith("L")) {
                size += 8; // 对象引用占用 8 字节
            } else if (descriptor.equals("Z")) {
                size += 1; // boolean 占用 1 字节
            } else if (descriptor.equals("C")) {
                size += 2; // char 占用 2 字节
            } else {
                System.err.println("Warning: Unrecognized field descriptor: " + descriptor);
            }
        }
        return size;
    }

    @Data
    public static class MiniMemberInfo {
        private MiniClass clazz;

        private int accessFlags;
        private int nameIndex;
        private int descriptorIndex;
        private int attributesCount;
        private MiniAttributeInfo[] attributes;

        public MiniMemberInfo(MiniClass clazz) {
            this.clazz = clazz;
        }

        public String getName() {
            return (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(clazz).getConstant(this.nameIndex);
        }

        public String getDescriptor() {
            return (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(clazz).getConstant(this.descriptorIndex);
        }

        public static MiniMemberInfo[] read(MiniClass clazz, DataInputStream input) throws IOException {
            int membersCount = input.readUnsignedShort();
            MiniMemberInfo[] members = new MiniMemberInfo[membersCount];
            for (int i = 0; i < membersCount; i++) {
                // 读取字段的访问标志
                int accessFlags = input.readUnsignedShort();
                // 读取字段的名称索引
                int nameIndex = input.readUnsignedShort();
                // 读取字段的描述符索引
                int descriptorIndex = input.readUnsignedShort();
                // 读取字段的属性数量
                int attributesCount = input.readUnsignedShort();

                // 读取字段的属性
                MiniAttributeInfo[] attributes = new MiniAttributeInfo[attributesCount];
                for (int j = 0; j < attributesCount; j++) {
                    int attributeNameIndex = input.readUnsignedShort();
                    int attributeLength = input.readInt();
                    byte[] info = new byte[attributeLength];
                    input.readFully(info);

                    MiniAttributeInfo attribute = new MiniAttributeInfo(clazz);
                    attribute.setAttributeNameIndex(attributeNameIndex);
                    attribute.setAttributeLength(attributeLength);
                    attribute.setInfo(info);

                    attributes[j] = attribute;
                }

                MiniMemberInfo member = new MiniMemberInfo(clazz);
                member.setAccessFlags(accessFlags);
                member.setNameIndex(nameIndex);
                member.setDescriptorIndex(descriptorIndex);
                member.setAttributesCount(attributesCount);
                member.setAttributes(attributes);
                members[i] = member;
            }

            return members;
        }

        @Override
        public String toString() {
            return String.format("%s %s", this.getName(), this.getDescriptor());
        }
    }

    @Data
    public static class MiniAttributeInfo {
        private MiniClass clazz;

        private int attributeNameIndex;
        private int attributeLength;
        private byte[] info;

        public MiniAttributeInfo(MiniClass clazz) {
            this.clazz = clazz;
        }

        public String getAttributeName() {
            return (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(clazz).getConstant(this.attributeNameIndex);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MiniCodeAttribute extends MiniAttributeInfo {
        private int maxStack;
        private int maxLocals;
        private int codeLength;
        private byte[] code;

        public MiniCodeAttribute(MiniClass clazz) {
            super(clazz);
        }

        public static MiniCodeAttribute read(MiniAttributeInfo attribute) throws IOException {
            // 使用属性内容的字节数组单独解析Code属性
            DataInputStream codeInput = new DataInputStream(new ByteArrayInputStream(attribute.getInfo()));
            int maxStack = codeInput.readUnsignedShort();
            int maxLocals = codeInput.readUnsignedShort();
            int codeLength = codeInput.readInt();
            byte[] code = new byte[codeLength];
            codeInput.readFully(code);
            // 注意：这里还需要处理异常表和附加属性（这里简化示例）
            codeInput.close();

            MiniCodeAttribute codeAttribute = new MiniCodeAttribute(attribute.getClazz());
            codeAttribute.setAttributeNameIndex(attribute.getAttributeNameIndex());
            codeAttribute.setAttributeLength(attribute.getAttributeLength());
            codeAttribute.setInfo(attribute.getInfo());

            codeAttribute.setMaxStack(maxStack);
            codeAttribute.setMaxLocals(maxLocals);
            codeAttribute.setCodeLength(codeLength);
            codeAttribute.setCode(code);

            return codeAttribute;
        }
    }
}
