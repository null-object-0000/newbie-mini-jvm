package mini;

import cn.hutool.core.util.RandomUtil;
import mini.cl.MethodCaller;
import mini.cl.MiniClass;
import mini.cl.MiniStackFrame;
import mini.data.area.MiniVirtualMachineMemory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟栈式虚拟机执行字节码指令
 */
public class MiniExecutionEngine {
    private MiniExecutionEngine() {
    }

    public final static MiniExecutionEngine INSTANCE = new MiniExecutionEngine();

    /**
     * 执行指令
     */
    public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
        String opcode = instruction.split(" ")[0];

        // 如果指令携带 _%d 则去除末尾这部分
        if (opcode.contains("_")) {
            opcode = opcode.substring(0, opcode.indexOf("_"));
        }

        String className = opcode.substring(0, 1).toUpperCase() + opcode.substring(1) + "Instruction";
        try {
            // 为了便于测试和维护，指令类放在当前类的内部并通过反射调用
            Class<?> clazz = Class.forName(String.format("%s.%s$%s", this.getClass().getPackageName(), this.getClass().getSimpleName(), className));
            Instruction instructionInstance = (Instruction) clazz.getDeclaredConstructor().newInstance();
            instructionInstance.execute(stackFrame, pc, instruction);
        } catch (Exception e) {
            System.out.printf("   %4d (%s#%s): [error] 执行 %s 指令失败: %s%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), instruction, e.getMessage());
        }
    }

    /**
     * 模拟栈式虚拟机的指令集
     * <p>
     * 该接口定义了一个方法，用于执行给定的指令。实现该接口的类可以根据具体的需求来执行不同的指令。
     * </p>
     */
    public interface Instruction {
        /**
         * 执行指令
         */
        void execute(MiniStackFrame stackFrame, int pc, String instruction) throws IOException;
    }

    /**
     * nop
     * 空指令，不执行任何操作
     */
    public static class NopInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
//            System.out.printf("   %4d (%s#%s): [nop] 执行空指令%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    /**
     * dup
     * 复制栈顶元素
     */
    public static class DupInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            int value = stackFrame.getOperandStack().peek();
            stackFrame.getOperandStack().push(value);
            System.out.printf("   %4d (%s#%s): [dup] 复制栈顶元素 %d%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), value);
        }
    }

    /**
     * bipush
     * 将常量压入栈
     */
    public static class BipushInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            int value = Integer.parseInt(instruction.split(" ")[1]);
            stackFrame.getOperandStack().push(value);
            System.out.printf("   %4d (%s#%s): [bipush] 将常量 %d 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), value);
        }
    }

    /**
     * ldc
     * 将常量压入栈
     */
    public static class LdcInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            Integer constantIndex = Integer.parseInt(instruction.split(" ")[1]);

            stackFrame.getOperandStack().push(constantIndex);

            System.out.printf("   %4d (%s#%s): [ldc] 将常量 %d 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), constantIndex);
        }
    }

    /**
     * aconst_null
     * 将 null 值压入栈
     */
    public static class AconstInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            stackFrame.getOperandStack().push(null);
            System.out.printf("   %4d (%s#%s): [aconst_null] 将 null 值压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    /**
     * iconst
     * 将常量压入栈
     */
    public static class IconstInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            // iconst_1
            int constant = Integer.parseInt(instruction.substring(7));
            stackFrame.getOperandStack().push(constant);
            System.out.printf("   %4d (%s#%s): [iconst] 将常量 %s 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), constant);
        }
    }

    /**
     * astore
     * 将栈顶值存储到局部变量
     */
    public static class AstoreInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            // astore_1
            int index = Integer.parseInt(instruction.substring(7));
            Integer value = stackFrame.getOperandStack().pop();
            stackFrame.getLocalVariableTable().put(index, value);
            System.out.printf("   %4d (%s#%s): [astore] 将栈顶值 %s 存储到局部变量 %d%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), value, index);
        }
    }

    /**
     * istore
     * 将栈顶值存储到局部变量
     */
    public static class IstoreInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            // istore_1
            int index = Integer.parseInt(instruction.substring(7));
            int value = stackFrame.getOperandStack().pop();
            stackFrame.getLocalVariableTable().put(index, value);
            System.out.printf("   %4d (%s#%s): [istore] 将栈顶值 %d 存储到局部变量 %d%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), value, index);
        }
    }

    /**
     * iload
     * 用于将局部变量表中指定索引的int类型值压入操作数栈顶
     */
    public static class IloadInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            // iload_1
            int loadIndex = Integer.parseInt(instruction.substring(6));
            Integer loadValue = stackFrame.getLocalVariableTable().get(loadIndex);
            stackFrame.getOperandStack().push(loadValue);
            System.out.printf("   %4d (%s#%s): [iload] 将局部变量 " + loadIndex + " 的值 " + loadValue + " 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    /**
     * aload
     * 用于将局部变量表中指定索引的引用类型值（如对象引用）压入操作数栈顶
     */
    public static class AloadInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            // aload_1
            int loadIndex = Integer.parseInt(instruction.substring(6));
            Integer loadValue = stackFrame.getLocalVariableTable().get(loadIndex);
            stackFrame.getOperandStack().push(loadValue);
            System.out.printf("   %4d (%s#%s): [aload] 将局部变量 " + loadIndex + " 的值 " + loadValue + " 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    /**
     * getstatic
     * 将静态变量压入栈
     */
    public static class GetstaticInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            String[] field = instruction.split(" ")[1].split(":");
            String fieldName = field[0];
            String fieldType = field[1];

            Integer value = (Integer) stackFrame.getClazz().getStaticVariables().get(fieldName);
            stackFrame.getOperandStack().push(value);
            System.out.printf("   %4d (%s#%s): [getstatic] 将静态变量 " + fieldName + " 的值 " + value + " 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    /**
     * putstatic
     * 将栈顶值存储到静态变量
     */
    public static class PutstaticInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            String[] field = instruction.split(" ")[1].split(":");
            String fieldName = field[0];
            String fieldType = field[1];
            int value = stackFrame.getOperandStack().pop();
            stackFrame.getClazz().getStaticVariables().put(fieldName, value);
            System.out.printf("   %4d (%s#%s): [putstatic] 将栈顶值 %d 存储到静态变量 %s%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), value, fieldName);
        }
    }

    /**
     * putfield
     * 给对象字段赋值。
     */
    public static class PutfieldInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            String[] field = instruction.split(" ")[1].split(":");
            String fieldName = field[0];
            String fieldType = field[1];

            // 1. 弹出栈顶的字段值
            int fieldValue = stackFrame.getOperandStack().pop();
            // 2. 弹出栈顶的对象引用
            int objectRef = stackFrame.getOperandStack().pop();
            // 3. 将字段值存储到对象的字段中 TODO: 这里应该要先读取对象大小，然后读取全部内容
            String object = MiniVirtualMachineMemory.HEAP_AREA.YOUNG_GEN.EDEN.read(objectRef, 1024);
            StringBuilder objectHeader = new StringBuilder(object);
            // 先找到 fields= ，然后从 fields= 开始往后搜索 fieldName: 的位置，然后替换 : 后到 ; 中的值
            int fieldsIndex = objectHeader.indexOf(";Fields=");
            int fieldIndex = objectHeader.indexOf(fieldName + ":", fieldsIndex);
            if (fieldIndex != -1) {
                int startIndex = fieldIndex + fieldName.length() + 1;
                int endIndex = objectHeader.indexOf(";", startIndex);

                // 看是不是对象，使得话说明是引用地址，要去常量池里找
                String newFieldValue;
                if (fieldType.startsWith("L")) {
                    newFieldValue = (String) MiniVirtualMachineMemory.METHOD_AREA.getConstantPool(stackFrame.getClazz()).getConstant(fieldValue);
                } else {
                    newFieldValue = String.valueOf(fieldValue);
                }

                objectHeader.replace(startIndex, endIndex, newFieldValue);
                // 更新对象的字段值
                MiniVirtualMachineMemory.HEAP_AREA.YOUNG_GEN.EDEN.write(objectRef, objectHeader.toString());
                System.out.printf("   %4d (%s#%s): [putfield] 将栈顶值 %s 存储到对象 %d 的字段 %s 中%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), newFieldValue, objectRef, fieldName);
            } else {
                System.out.printf("   %4d (%s#%s): [putfield] 找不到字段 %s%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), fieldName);
            }
        }
    }

    /**
     * new
     * 创建对象实例
     */
    public static class NewInstruction implements Instruction {

        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) throws IOException {
            String className = instruction.split(" ")[1];

            // 1. 加载类的元信息
            MiniClass clazz = MiniVirtualMachineMemory.METHOD_AREA.APP_CLASS_LOADER.loadClass(className);
            // 2. 计算对象的大小并为其分配内存空间（内存规整-指针碰撞、内存不规整-空闲链表）
            int objectSize = clazz.getInstanceSize();
            // 3. 处理并发安全问题（CAS、TLAB）
            // 5. 设置对象头的信息（hashCode、GC 分代年龄、锁状态标志等）
            StringBuilder objectHeader = new StringBuilder()
                    .append("Mark Word=")
                    .append("lock:" + 0).append(";")
                    .append("biased_lock:" + 0).append(";")
                    .append("hash:" + 0).append(";")
                    .append("age:0").append(";")
                    .append("JavaThread:" + 0).append(";")
                    .append("epoch:" + 0).append(";")
                    .append("ptr_to_lock_record:" + 0).append(";")
                    .append("ptr_to_heavyweight_monitor:" + 0).append(";")
                    .append("Klass Pointer=" + clazz).append(";");

            // 4. 初始化分配到的空间（属性的默认初始化）
            objectHeader.append("Fields=");
            for (MiniClass.MiniMemberInfo field : clazz.getFields()) {
                // 跳过静态变量
                if ((field.getAccessFlags() & 0x0008) != 0) continue;

                objectHeader.append(field.getName()).append(":");

                if (field.getDescriptor().equals("I")) {
                    objectHeader.append("0");
                } else if (field.getDescriptor().equals("Z")) {
                    objectHeader.append("false");
                } else if (field.getDescriptor().equals("C")) {
                    objectHeader.append("0");
                } else if (field.getDescriptor().equals("F")) {
                    objectHeader.append("0.0");
                } else if (field.getDescriptor().equals("D")) {
                    objectHeader.append("0.0");
                } else if (field.getDescriptor().equals("J")) {
                    objectHeader.append("0");
                } else if (field.getDescriptor().startsWith("L")) {
                    objectHeader.append("null");
                }

                objectHeader.append(";");
            }

            // 看看比计算的 objectSize 小的话就插入空的字符串填充
            if (objectHeader.length() < objectSize) {
                int paddingSize = objectSize - objectHeader.length();
                objectHeader.append(" ".repeat(Math.max(0, paddingSize)));
            } else {
                // TODO: 这里理论应该要在设置对象头的上方
                objectSize = objectHeader.length();
            }

            int objectRef = MiniVirtualMachineMemory.HEAP_AREA.YOUNG_GEN.EDEN.allocate(objectSize);

            MiniVirtualMachineMemory.HEAP_AREA.YOUNG_GEN.EDEN.write(objectRef, objectHeader.toString());

            stackFrame.getOperandStack().push(objectRef);
            // 局部变量表中存储对象引用
            stackFrame.getLocalVariableTable().put(0, objectRef);

            System.out.printf("   %4d (%s#%s): [new] 创建 %s 对象实例 %d%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), className, objectRef);
        }
    }

    /**
     * iadd
     * 弹出栈顶两个值相加，结果压入栈
     */
    public static class IaddInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            int value1 = stackFrame.getOperandStack().pop();
            int value2 = stackFrame.getOperandStack().pop();
            int result = value1 + value2;
            stackFrame.getOperandStack().push(result);
            System.out.printf("   %4d (%s#%s): [iadd] 将栈顶两个值 " + value1 + " 和 " + value2 + " 相加，结果 " + result + " 压入栈%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    public static class InvokeInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) throws IOException {
            String instructionName = instruction.split(" ")[0];

            String className = instruction.split(" ")[1].split("[.]")[0];
            String methodName = instruction.split(" ")[1].split("[.]")[1];
            String paramsAndReturnType = instruction.split(" ")[2];

            MiniClass clazz = MiniVirtualMachineMemory.METHOD_AREA.APP_CLASS_LOADER.loadClass(className);
            MiniClass.MiniMemberInfo method = clazz.getMethod(methodName);

            // 解析参数和返回值类型，eg: (II)I
            // 解析括号内的参数类型
            String params = paramsAndReturnType.substring(paramsAndReturnType.indexOf("(") + 1, paramsAndReturnType.indexOf(")"));
            String returnType = paramsAndReturnType.substring(paramsAndReturnType.indexOf(")") + 1);
            Map<Integer, Integer> localVariableTable = new HashMap<>();
            for (int i = 0; i < params.length(); i++) {
                char paramType = params.charAt(i);
                if (paramType == 'I') {
                    // int 类型
                    int value = stackFrame.getOperandStack().pop();
                    localVariableTable.put(i, value);
                } else if (paramType == 'L') {
                    // 截取到分号结束
                    int endIndex = params.indexOf(";", i);
                    String objectType = params.substring(i, endIndex + 1);
                    i = endIndex;
                } else {
                    throw new IllegalArgumentException("不支持的参数类型: " + paramType);
                }
            }

            if ("invokespecial".equals(instructionName)) {
                // TODO: 不知道这么做是不是对的
                localVariableTable = stackFrame.getLocalVariableTable();
            }

            System.out.printf("   %4d (%s#%s): [%s] 调用实例初始化方法 %s.%s %s%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName(), instructionName, className, methodName, paramsAndReturnType);

            MiniStackFrame callStackFrame = MethodCaller.call(clazz, method, localVariableTable);

            if (returnType.equals("I")) {
                // 返回值类型为 int
                Integer returnValue = callStackFrame.getOperandStack().pop();
                stackFrame.getOperandStack().push(returnValue);
            } else if (returnType.equals("V")) {
                // 返回值类型为 void
            } else {
                throw new IllegalArgumentException("不支持的返回值类型: " + returnType);
            }
        }
    }

    /**
     * invokespecial
     * 调用实例初始化方法
     */
    public static class InvokespecialInstruction extends InvokeInstruction {

    }

    /**
     * invokestatic
     * 调用静态方法
     */
    public static class InvokestaticInstruction extends InvokeInstruction {

    }

    /**
     * invokevirtual
     * 调用实例方法
     */
    public static class InvokevirtualInstruction extends InvokeInstruction {

    }

    /**
     * ireturn
     * 返回指令，结束执行
     */
    public static class IreturnInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            System.out.printf("   %4d (%s#%s): [ireturn] 执行结束%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }

    /**
     * return
     * 返回指令，结束执行
     */
    public static class ReturnInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            System.out.printf("   %4d (%s#%s): [return] 执行结束%n", pc, stackFrame.getClazz().getName(), stackFrame.getMethodName());
        }
    }
}