package com.colin.agent;

import javassist.*;
import javassist.bytecode.CodeAttribute;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author colin
 * @create 2022-11-07 17:13
 */
public class MyAgent implements ClassFileTransformer {

    // 修改特定包的下类的字节码
    public final String injectedClassName = "com.colin.agent";

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        className = className.replace("/", ".");
        if (className.startsWith(injectedClassName)){
            CtClass ctclss = null;
            try {
                // 使用全限定名称，用于取得字节码类，使用 javassist 技术实现
                ctclss = ClassPool.getDefault().get(className);
                CtMethod[] methods = ctclss.getMethods();

                for (CtMethod ctMethod: methods){
                    CodeAttribute codeAttribute = ctMethod.getMethodInfo2().getCodeAttribute();
                    if (codeAttribute == null){
                        continue;
                    }

                    if (!ctMethod.isEmpty()){
                        // 目标方法前执行
                        ctMethod.insertBefore("System.out.println(\"hello Im agent : " + ctMethod.getName() + "\");");
                    }
                }
                return ctclss.toBytecode();
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
