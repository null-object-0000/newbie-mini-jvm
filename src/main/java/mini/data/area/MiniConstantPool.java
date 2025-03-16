package mini.data.area;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * JDK 1.7 及以下在【方法区】中存储类的常量池
 * JDK 1.8 及以上在【本地内存】->【元空间】中存储类的常量池
 * 常量池
 * https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.4
 */
public class MiniConstantPool {
    private final Object[] constantPool;

    public MiniConstantPool(Object[] constantPool) {
        this.constantPool = constantPool;
    }

    public static MiniConstantPool read(DataInputStream input) throws IOException {
        int constantPoolCount = input.readUnsignedShort();
        MiniConstantPool miniConstantPool = new MiniConstantPool(new Object[constantPoolCount]);

        for (int i = 1; i < constantPoolCount; i++) {
            int tag = input.readUnsignedByte();
            switch (tag) {
                case 9: // CONSTANT_Fieldref
                case 10: // CONSTANT_Methodref
                case 12: // CONSTANT_NameAndType
                case 18: // CONSTANT_InvokeDynamic
                    miniConstantPool.constantPool[i] = input.readUnsignedShort() + ":" + input.readUnsignedShort();
                    break;
                case 7: // CONSTANT_Class
                case 8: // CONSTANT_String
                    miniConstantPool.constantPool[i] = input.readUnsignedShort();
                    break;
                case 1: // CONSTANT_Utf8
                    miniConstantPool.constantPool[i] = input.readUTF();
                    break;
                case 15: // CONSTANT_MethodHandle
                    miniConstantPool.constantPool[i] = input.readUnsignedByte() + ":" + input.readUnsignedShort();
                    break;
                case 3: // CONSTANT_Integer
                    miniConstantPool.constantPool[i] = input.readInt();
                    break;
                default:
                    throw new IOException("Invalid constant pool tag: " + tag);
            }
        }

        return miniConstantPool;
    }

    public Object getConstant(int index) {
        return constantPool[index];
    }

    public void setConstant(int index, Object value) {
        constantPool[index] = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < constantPool.length; i++) {
            sb.append("constant pool #").append(i).append(": ").append(constantPool[i]).append("\n");
        }
        return sb.toString();
    }
}
