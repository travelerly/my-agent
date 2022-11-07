package com.colin.agent;

/**
 * @author colin
 * @create 2022-11-07 17:00
 */
public class AgentTest {

    public static void main(String[] args) {
        System.out.println("这是 javaagent 第一个 main 方法测试");
        testMyAgent();
        testMyAgent2("hello","my agent");
    }

    public static void testMyAgent(){
        System.out.println("test my agent");
    }

    public static void testMyAgent2(String str1,String str2){
        System.out.println("test my agent: " + str1 + " " + str2);
    }

}
