package com.colin.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author colin
 * @create 2022-11-07 16:57
 */
public class PremainDemo {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("hello agent，双参数 premain");
        System.out.println("agentArgs===>" + agentArgs);

        /**
         * JVMTI 接口测试
         * Instrumentation 的最大作用，就是类定义动态改变和操作。
         */
        /*inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader,
                                    String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {

                System.out.println("premain load class : " + className);
                return classfileBuffer;
            }
        });*/

        /**
         * javassist 技术测试
         */
        inst.addTransformer(new MyAgent());
    }

    /**
     * premain 方法参数：可以传一个参数，也可传两个参数，但两个参数的 premain 重载方法优先执行
     */
    /*public static void premain(String agentArgs) {
        System.out.println("hello agent，单参数 premain");
        System.out.println("agentArgs===>" + agentArgs);
    }*/
}
