package mini;

import mini.cl.MethodCaller;
import mini.cl.MiniBootstrapClassLoader;
import mini.cl.MiniClass;
import mini.cl.MiniStackFrame;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟栈式虚拟机执行字节码指令
 */
public class MiniStackVM {
    private MiniStackVM() {
    }

    public final static MiniStackVM INSTANCE = new MiniStackVM();

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
            System.out.printf("    %4d: [error] 执行 %s 指令失败: %s%n", pc, instruction, e.getMessage());
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
//            System.out.printf("    %4d: [nop] 执行空指令%n", pc);
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
            System.out.printf("    %4d: [bipush] 将常量 %d 压入栈%n", pc, value);
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
            System.out.printf("    %4d: [aconst_null] 将 null 值压入栈%n", pc);
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
            System.out.printf("    %4d: [iconst] 将常量 %s 压入栈%n", pc, constant);
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
            System.out.printf("    %4d: [istore] 将栈顶值 %d 存储到局部变量 %d%n", pc, value, index);
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
            System.out.printf("    %4d: [iload] 将局部变量 " + loadIndex + " 的值 " + loadValue + " 压入栈%n", pc);
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
            System.out.printf("    %4d: [aload] 将局部变量 " + loadIndex + " 的值 " + loadValue + " 压入栈%n", pc);
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
            System.out.printf("    %4d: [getstatic] 将静态变量 " + fieldName + " 的值 " + value + " 压入栈%n", pc);
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
            System.out.printf("    %4d: [putstatic] 将栈顶值 %d 存储到静态变量 %s%n", pc, value, fieldName);
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
            System.out.printf("    %4d: [iadd] 将栈顶两个值 " + value1 + " 和 " + value2 + " 相加，结果 " + result + " 压入栈%n", pc);
        }
    }

    public static class InvokeInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) throws IOException {
            String instructionName = instruction.split(" ")[0];

            String className = instruction.split(" ")[1].split("[.]")[0];
            String methodName = instruction.split(" ")[1].split("[.]")[1];
            String paramsAndReturnType = instruction.split(" ")[2];

            MiniClass clazz = MiniBootstrapClassLoader.loadClass(className);
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

            System.out.printf("    %4d: [%s] 调用实例初始化方法 %s.%s %s%n", pc, instructionName, className, methodName, paramsAndReturnType);
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
            System.out.printf("    %4d: [ireturn] 执行结束%n", pc);
        }
    }

    /**
     * return
     * 返回指令，结束执行
     */
    public static class ReturnInstruction implements Instruction {
        @Override
        public void execute(MiniStackFrame stackFrame, int pc, String instruction) {
            System.out.printf("    %4d: [return] 执行结束%n", pc);
        }
    }
}