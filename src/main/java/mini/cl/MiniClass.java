package mini.cl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mini.MiniStackVM;
import mini.data.area.MiniConstantPool;
import mini.data.area.MiniMetaSpace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

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
    private DataInputStream input;
    @Getter
    private MiniClassLoader classLoader;

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
        String name = (String) MiniMetaSpace.constantPool.getConstant((Integer) MiniMetaSpace.constantPool.getConstant(thisClass));
        return name.replace("/", "."); // 替换斜杠为点
    }

    public MiniClass getSuperClass() {
        String name = (String) MiniMetaSpace.constantPool.getConstant((Integer) MiniMetaSpace.constantPool.getConstant(superClass));
        // TODO: 基于 MiniBootstrapClassLoader 通过类名获取类对象
        return null;
    }

    public MiniClass[] getInterfaces() {
        MiniClass[] interfaceClasses = new MiniClass[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            String interfaceName = (String) MiniMetaSpace.constantPool.getConstant(interfaces[i]);
            // TODO: 基于 MiniBootstrapClassLoader 通过类名获取类对象
            interfaceClasses[i] = null;
        }
        return interfaceClasses;
    }

    public MiniClass _linking_verify() throws IOException {
        this.readAndCheckMagic();
        this.readAndCheckVersion();

        // 读取常量池
        MiniMetaSpace.constantPool = MiniConstantPool.read(input);

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
        // 先执行 <clinit> 方法
        MiniClass.MiniMemberInfo clinit = Arrays.stream(this.getMethods())
                .filter(m -> m.getName().equals("<clinit>"))
                .findFirst().orElseThrow();

        MiniClass.MiniCodeAttribute codeAttribute = Arrays.stream(clinit.getAttributes())
                .filter(a -> a instanceof MiniClass.MiniCodeAttribute)
                .map(a -> (MiniClass.MiniCodeAttribute) a)
                .findFirst().orElseThrow();

        System.out.println("static {};\n    Code:");

        // 解析字节码指令
        byte[] code = codeAttribute.getCode();
        // 读取指令
        int pc = 0;
        /* https://www.cnblogs.com/longjee/p/8675771.html */
        while (pc < code.length) {
            int oldPc = pc;
            byte opcode = code[pc++];

            switch (opcode) {
                case 0x00: // nop
                    MiniStackVM.INSTANCE.execute(oldPc, "nop");
                    break;
                case 0x01: // aconst_null
                    MiniStackVM.INSTANCE.execute(oldPc, "aconst_null");
                    break;
                case 0x02: // iconst_m1
                    MiniStackVM.INSTANCE.execute(oldPc, "iconst_m1");
                    break;
                case 0x03: // iconst_0
                case 0x04: // iconst_1
                case 0x05: // iconst_2
                case 0x06: // iconst_3
                case 0x07: // iconst_4
                case 0x08: // iconst_5
                    String instruction = "iconst_" + (opcode - 3);
                    MiniStackVM.INSTANCE.execute(oldPc, instruction);
                    break;
                case 0x10: // bipush
                    instruction = "bipush_" + code[pc++];
                    MiniStackVM.INSTANCE.execute(oldPc, instruction);
                    break;
                case 0x2A: // aload_0
                case 0x2B: // aload_1
                case 0x2C: // aload_2
                case 0x2D: // aload_3
                    instruction = "aload_" + (opcode - 42);
                    MiniStackVM.INSTANCE.execute(oldPc, instruction);
                    break;
                case 0x1A: // iload_0
                case 0x1B: // iload_1
                case 0x1C: // iload_2
                case 0x1D: // iload_3
                    instruction = "iload_" + (opcode - 26);
                    MiniStackVM.INSTANCE.execute(oldPc, instruction);
                    break;
                /* 对象操作指令 */
                case (byte) 0xB2: // getstatic
                case (byte) 0xB3: // putstatic
                    // 读取常量池索引
                    int index = ((code[pc] & 0xFF) << 8) | (code[pc + 1] & 0xFF);
                    pc += 2;
                    String instructionName = opcode == (byte) 0xB2 ? "getstatic" : "putstatic";

                    MiniStackVM.INSTANCE.execute(oldPc, instructionName + "_" + index);
                    break;
                case 0x60: // iadd
                    MiniStackVM.INSTANCE.execute(oldPc, "iadd");
                    break;
                case 0x3B: // istore_0
                case 0x3C: // istore_1
                case 0x3D: // istore_2
                case 0x3E: // istore_3
                    instruction = "istore_" + (opcode - 59);
                    MiniStackVM.INSTANCE.execute(oldPc, instruction);
                    break;
                case (byte) 0xB6: // invokevirtual
                    // 读取常量池索引
                    index = ((code[pc] & 0xFF) << 8) | (code[pc + 1] & 0xFF);
                    pc += 2;
                    // 读取常量池中的值（left:right）
                    Object value = MiniMetaSpace.constantPool.getConstant(index);
                    Integer classIndex = (Integer) getLeft(value);
                    String className = (String) MiniMetaSpace.constantPool.getConstant(classIndex);
                    Object field = getRight(value);
                    Object fieldName = getLeft(field);
                    Object fieldValue = getRight(field);

                    // 打印结果
                    System.out.printf("    %4d: invokevirtual %s.%s %s\n", oldPc, className, fieldName, fieldValue);
                    break;
                case (byte) 0xB1: // return
                    MiniStackVM.INSTANCE.execute(oldPc, "return");
                    break;
                case (byte) 0xBA: // invokedynamic
                    index = ((code[pc] & 0xFF) << 8) | (code[pc + 1] & 0xFF);
                    pc += 2;
                    value = MiniMetaSpace.constantPool.getConstant(index);
                    Object left = this.getLeft(value);
                    Object right = this.getRight(value);

                    Object leftSubLeft = this.getLeft(left);
                    Object leftSubRight = this.getRight(left);

                    Object rightSubLeft = this.getLeft(right);
                    Object rightSubRight = this.getRight(right);

                    System.out.printf("    %4d: invokedynamic %s:%s %s:%s\n", oldPc, MiniMetaSpace.constantPool.getConstant((Integer) leftSubLeft), leftSubRight, rightSubLeft, rightSubRight);
                    break;
                default:
                    System.out.printf("Unknown instruction: 0x%02X\n", opcode);
            }
        }

        return this;
    }

    public MiniClass _linking_resolve() {
        return this;
    }

    public MiniClass parse() throws IOException {
        this._linking_verify();

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
        fields = MiniMemberInfo.read(input);
    }

    private void readMethods() throws IOException {
        methods = MiniMemberInfo.read(input);

        for (MiniMemberInfo method : methods) {
            for (int i = 0; i < method.attributesCount; i++) {
                MiniAttributeInfo attribute = method.getAttributes()[i];
                String attributeName = (String) MiniMetaSpace.constantPool.getConstant(attribute.getAttributeNameIndex());
                if ("Code".equals(attributeName)) {
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

    @Data
    public static class MiniMemberInfo {
        private int accessFlags;
        private int nameIndex;
        private int descriptorIndex;
        private int attributesCount;
        private MiniAttributeInfo[] attributes;

        public String getName() {
            return (String) MiniMetaSpace.constantPool.getConstant(this.nameIndex);
        }

        public String getDescriptor() {
            return (String) MiniMetaSpace.constantPool.getConstant(this.descriptorIndex);
        }

        public static MiniMemberInfo[] read(DataInputStream input) throws IOException {
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

                    MiniAttributeInfo attribute = new MiniAttributeInfo();
                    attribute.setAttributeNameIndex(attributeNameIndex);
                    attribute.setAttributeLength(attributeLength);
                    attribute.setInfo(info);

                    attributes[j] = attribute;
                }

                MiniMemberInfo member = new MiniMemberInfo();
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
        private int attributeNameIndex;
        private int attributeLength;
        private byte[] info;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MiniCodeAttribute extends MiniAttributeInfo {
        private int maxStack;
        private int maxLocals;
        private int codeLength;
        private byte[] code;

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

            MiniCodeAttribute codeAttribute = new MiniCodeAttribute();
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

    private Object getLeft(Object constant) {
        if (constant instanceof String str) {
            if (str.contains(":")) {
                String[] parts = str.split(":");
                return MiniMetaSpace.constantPool.getConstant(Integer.parseInt(parts[0]));
            } else {
                throw new IllegalArgumentException("constant must be String: " + str);
            }
        } else {
            throw new IllegalArgumentException("constant must be String");
        }
    }

    private Object getRight(Object constant) {
        if (constant instanceof String str) {
            if (str.contains(":")) {
                String[] parts = str.split(":");
                return MiniMetaSpace.constantPool.getConstant(Integer.parseInt(parts[1]));
            } else {
                throw new IllegalArgumentException("constant must be String: " + str);
            }
        } else {
            throw new IllegalArgumentException("constant must be String");
        }
    }
}
