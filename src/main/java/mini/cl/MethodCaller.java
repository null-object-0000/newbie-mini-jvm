package mini.cl;

import mini.MiniStackVM;
import mini.data.area.MiniMetaSpace;

import java.util.Arrays;
import java.util.Map;

public class MethodCaller {
    public static MiniStackFrame call(MiniClass clazz, MiniClass.MiniMemberInfo method, Map<Integer, Integer> localVariableTable) {
        System.out.printf("%s.%s : %s {};\n    Code:%n", clazz.getName(), method.getName(), method.getDescriptor());

        MiniClass.MiniCodeAttribute codeAttribute = Arrays.stream(method.getAttributes())
                .filter(a -> a instanceof MiniClass.MiniCodeAttribute)
                .map(a -> (MiniClass.MiniCodeAttribute) a)
                .findFirst().orElseThrow();

        // 一个方法对应一个栈帧
        MiniStackFrame stackFrame = new MiniStackFrame(clazz, method.getName(), localVariableTable);

        // TODO: 这里需要区分是静态方法还是实例方法，如果是实例方法需要传入 this 对象到栈帧中

        // 解析字节码指令
        byte[] code = codeAttribute.getCode();
        // 模拟 PC 寄存器，指向当前执行的字节码指令
        int pc = 0;
        /* https://www.cnblogs.com/longjee/p/8675771.html */
        while (pc < code.length) {
            int oldPc = pc;
            byte opcode = code[pc++];

            switch (opcode) {
                case 0x00: // nop
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "nop");
                    break;
                case 0x01: // aconst_null
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "aconst_null");
                    break;
                case 0x02: // iconst_m1
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "iconst_m1");
                    break;
                case 0x03: // iconst_0
                case 0x04: // iconst_1
                case 0x05: // iconst_2
                case 0x06: // iconst_3
                case 0x07: // iconst_4
                case 0x08: // iconst_5
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "iconst_" + (opcode - 3));
                    break;
                case 0x10: // bipush
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "bipush" + " " + code[pc++]);
                    break;
                case 0x2A: // aload_0
                case 0x2B: // aload_1
                case 0x2C: // aload_2
                case 0x2D: // aload_3
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "aload_" + (opcode - 42));
                    break;
                case 0x1A: // iload_0
                case 0x1B: // iload_1
                case 0x1C: // iload_2
                case 0x1D: // iload_3
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "iload_" + (opcode - 26));
                    break;
                case 0x60: // iadd
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "iadd");
                    break;
                case 0x3B: // istore_0
                case 0x3C: // istore_1
                case 0x3D: // istore_2
                case 0x3E: // istore_3
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "istore_" + (opcode - 59));
                    break;
                /* 对象操作指令 */
                case (byte) 0xB2: // getstatic
                case (byte) 0xB3: // putstatic
                case (byte) 0xB4: // getfield
                case (byte) 0xB5: // putfield
                    // 读取常量池索引
                    int index = ((code[pc] & 0xFF) << 8) | (code[pc + 1] & 0xFF);
                    pc += 2;
                    Object value = MiniMetaSpace.getConstantPool(clazz).getConstant(index);
                    String className = MethodCaller.getClassName(clazz, value);
                    Object field = getRight(clazz, value);
                    Object fieldName = getLeft(clazz, field);
                    Object fieldType = getRight(clazz, field);

                    String instructionName = switch (opcode) {
                        case (byte) 0xB2 -> "getstatic";
                        case (byte) 0xB3 -> "putstatic";
                        case (byte) 0xB4 -> "getfield";
                        case (byte) 0xB5 -> "putfield";
                        default -> null;
                    };

                    // 看是不是当前类的静态变量
                    if (clazz.getName().equals(className.replace("/", "."))) {
                        MiniStackVM.INSTANCE.execute(stackFrame, oldPc, String.format("%s %s:%s", instructionName, fieldName, fieldType));
                    } else {
                        MiniStackVM.INSTANCE.execute(stackFrame, oldPc, String.format("%s %s.%s:%s", instructionName, className, fieldName, fieldType));
                    }
                    break;
                case (byte) 0xB6: // invokevirtual
                case (byte) 0xB7: // invokespecial
                case (byte) 0xB8: // invokestatic
                    index = ((code[pc] & 0xFF) << 8) | (code[pc + 1] & 0xFF);
                    pc += 2;
                    value = MiniMetaSpace.getConstantPool(clazz).getConstant(index);
                    className = MethodCaller.getClassName(clazz, value);
                    Object methodInfo = getRight(clazz, value);
                    String methodName = (String) getLeft(clazz, methodInfo);
                    String returnType = (String) getRight(clazz, methodInfo);

                    // 看当前是不是 MiniObject，如果是的话就把 <clinit> 以及 <init> 方法剔除
                    if ("demo.java.lang.MiniObject".equals(stackFrame.getClazz().getName())
                            && ("<clinit>".equals(methodName) || "<init>".equals(methodName))) {
                        break;
                    }

                    instructionName = switch (opcode) {
                        case (byte) 0xB6 -> "invokevirtual";
                        case (byte) 0xB7 -> "invokespecial";
                        case (byte) 0xB8 -> "invokestatic";
                        default -> null;
                    };

                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, String.format("%s %s.%s %s", instructionName, className, methodName, returnType));
                    break;
                case (byte) 0xAC: // ireturn
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "ireturn");
                    break;
                case (byte) 0xB1: // return
                    MiniStackVM.INSTANCE.execute(stackFrame, oldPc, "return");
                    break;
                case (byte) 0xBA: // invokedynamic
                    index = ((code[pc] & 0xFF) << 8) | (code[pc + 1] & 0xFF);
                    pc += 2;
                    value = MiniMetaSpace.getConstantPool(clazz).getConstant(index);

                    Object left = getLeft(clazz, value);
                    Object right = getRight(clazz, value);

                    Object leftSubLeft = null;
                    Object leftSubRight = null;
                    className = null;
                    if (left instanceof String) {
                        leftSubLeft = getLeft(clazz, left);
                        leftSubRight = getRight(clazz, left);

                        className = MethodCaller.getClassName(clazz, left);
                    }

                    Object rightSubLeft = getLeft(clazz, right);
                    Object rightSubRight = getRight(clazz, right);

                    System.out.printf("    %4d: invokedynamic %s:%s %s:%s\n", oldPc, className, leftSubRight, rightSubLeft, rightSubRight);

                    break;
                default:
                    System.out.printf("Unknown instruction: 0x%02X\n", opcode);
            }
        }

        System.out.printf("%s.%s : %s End%n", clazz.getName(), method.getName(), method.getDescriptor());

        return stackFrame;
    }

    private static String getClassName(MiniClass clazz, Object constant) {
        Object classNameIndex = MethodCaller.getLeft(clazz, constant);

        if (classNameIndex == null) return null;
        if (classNameIndex instanceof String) return (String) classNameIndex;
        if (!(classNameIndex instanceof Integer)) return null;

        String className = (String) MiniMetaSpace.getConstantPool(clazz).getConstant((Integer) classNameIndex);

        // 这里模拟下，强行把 java/lang/ 替换为 demo/java/lang/Mini

        return className.replace("java/lang/", "demo/java/lang/Mini")
                .replace("java/io/", "demo/java/io/Mini");
    }

    private static Object getLeft(MiniClass clazz, Object constant) {
        if (constant instanceof String str) {
            if (str.contains(":")) {
                String[] parts = str.split(":");
                return MiniMetaSpace.getConstantPool(clazz).getConstant(Integer.parseInt(parts[0]));
            } else {
                throw new IllegalArgumentException("constant must be String: " + str);
            }
        } else {
            throw new IllegalArgumentException("constant must be String");
        }
    }

    private static Object getRight(MiniClass clazz, Object constant) {
        if (constant instanceof String str) {
            if (str.contains(":")) {
                String[] parts = str.split(":");
                return MiniMetaSpace.getConstantPool(clazz).getConstant(Integer.parseInt(parts[1]));
            } else {
                throw new IllegalArgumentException("constant must be String: " + str);
            }
        } else {
            throw new IllegalArgumentException("constant must be String");
        }
    }
}
