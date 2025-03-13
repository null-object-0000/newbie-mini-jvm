# newbie-mini-jvm

此项目将试图让工作多年的 Java 程序员通过自己动手写 Java 虚拟机的方式深入理解 JVM 原理。

## 一、前置知识准备

### 1. JVM 体系结构总览

### 2. 指令集架构

首先我们来回忆一下一个 Java 程序的执行过程：

1. Java 源码编译器 `javac` 将 Java 源码编译成字节码文件 `.class`。
2. Java 虚拟机 `JVM` 通过类加载器将 `.class` 文件加载到内存中。
3. `JVM` 通过字节码执行引擎将字节码转换为机器码并执行。

我们编写了一个简单的 Java 程序，源码见：[HelloStackVM.java](./src/main/java/demo/HelloStackVM.java)

然后我们通过 `javac` 和 `javap` 命令来查看编译后的字节码指令。

``` bash
$ javac ./src/main/java/demo/HelloStackVM.java
$ javap -c ./src/main/java/demo/HelloStackVM.class
```

通过 `javap` 我们可以看到 `HelloStackVM` 类的字节码指令集，我们重点关注 `main` 方法的字节码指令集 ，下面以表格的形式对应字节码指令和
Java 源码的关系：

| Java 源码                  | 字节码指令                                                 |
|--------------------------|-------------------------------------------------------|
| `int a = 3;`             | `iconst_3` `istore_1`                                 |
| `int b = 4;`             | `iconst_4` `istore_2`                                 |
| `int c = a + b;`         | `iload_1` `iload_2` `iadd` `istore_3`                 |
| `System.out.println(c);` | `getstatic` `iload_3` `invokedynamic` `invokevirtual` |
| `return;`                | `return`                                              |

我们来分析一下字节码指令（只需要了解大致意思即可）：

- `iconst_3`：将整数常量 3 压入操作数栈。
- `istore_1`：将操作数栈顶的整数值存入局部变量表的第 1 个位置。
- `iload_1`：将局部变量表第 1 个位置的整数值压入操作数栈。
- `iadd`：将操作数栈顶的两个整数值相加，并将结果压入操作数栈。
- `istore_3`：将操作数栈顶的整数值存入局部变量表的第 3 个位置。
- `getstatic`：获取静态字段的值，并将其压入操作数栈。
- `invokedynamic`：动态调用方法。
- `invokevirtual`：调用实例方法。
- `return`：返回。

我们先跳过类加载机制和运行时数据区的内容，先来实现一个简单的字节码执行引擎。 在开始之前，我们需要先来了解一种数据结构：栈。

#### 栈（Stack）

栈是一种后进先出（LIFO）的数据结构，具有以下特点：

- 只能在栈顶进行插入和删除操作。
- 栈顶元素是最后插入的元素，栈底元素是最先插入的元素。

有两种常见的栈实现方式：

| 实现方式 | 关键说明                       | 优点         | 缺点     |
|------|----------------------------|------------|--------|
| 数组   | 数组存储栈中的元素，栈顶指针指向数组的最后一个元素。 | 访问速度快      | 栈的大小固定 |
| 链表   | 链表存储栈中的元素，栈顶指针指向链表的头节点。    | 栈的大小可以动态扩展 | 访问速度慢  |

我们使用链表来实现栈，链表的每个节点包含一个数据域和一个指向下一个节点的指针。源码见：[Stack.java](./src/main/java/data/structure/Stack.java)

为什么要使用栈呢？因为 Java 虚拟机的字节码指令集是基于栈的，所有的操作都是在操作数栈上进行的。

#### 栈虚拟机（Stack Virtual Machine）

接下来我们来实现一个可以执行字节码指令的栈虚拟机，源码见：[StackVM.java](./src/main/java/mini/StackVM.java)

另外我们还在单元测试中模拟了 `HelloStackVM` 类的 `main`
方法的字节码指令集，来验证我们实现的栈虚拟机是否正确。源码见：[StackVMTest.java](./src/test/java/mini/StackVMTest.java)

## 二、类文件解析

## 三、类加载机制

## 四、运行时数据区

## 五、字节码执行引擎

## 六、方法调用机制

## 七、异常处理机制

## 八、垃圾回收
