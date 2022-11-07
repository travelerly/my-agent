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

}
