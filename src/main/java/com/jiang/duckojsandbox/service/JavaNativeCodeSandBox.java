package com.jiang.duckojsandbox.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.jiang.duckojsandbox.model.ExecuteRequest;
import com.jiang.duckojsandbox.model.ExecuteResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * java原生实现的代码沙箱
 */
public class JavaNativeCodeSandBox implements CodeSandBox {


    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();

        ExecuteRequest executeRequest = new ExecuteRequest();
        executeRequest.setInputList(Arrays.asList("12, 13", "14, 15"));
        executeRequest.setSubmitLanguage("java");
        String code = ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
        executeRequest.setSubmitCode(code);
        ExecuteResponse executeResponse = javaNativeCodeSandBox.doExecute(executeRequest);
        System.out.println(executeResponse);

    }

    @Override
    public ExecuteResponse doExecute(ExecuteRequest executeRequest) {

        List<String> inputList = executeRequest.getInputList();
        String submitLanguage = executeRequest.getSubmitLanguage();
        String submitCode = executeRequest.getSubmitCode();

        // (1) 把用户代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        //用户代码存放目录：
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        //java代码存放：
        String userJavaCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        //写入文件
        File userCodeFile = FileUtil.writeString(submitCode, userJavaCodePath, StandardCharsets.UTF_8);

        //(2) 编译代码：
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process process = Runtime.getRuntime().exec(compileCmd);
            //等待程序执行成功，根据exitValue来判断是否执行成功
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                System.out.println("编译成功");
                //获取控制台输出成功信息；
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuffer compileOutputBuffer = new StringBuffer();
                String readLine;

                while ((readLine = bufferedReader.readLine()) != null) {
                    compileOutputBuffer.append(readLine);
                }
                System.out.println(compileOutputBuffer);
            } else {
                System.out.println("编译失败,错误码为：" + exitValue);

                //编译失败正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuffer compileOutputBuffer = new StringBuffer();
                //逐行读取数据
                String outputReadLine;
                while ((outputReadLine = bufferedReader.readLine()) != null) {
                    compileOutputBuffer.append(outputReadLine);
                }
                System.out.println(compileOutputBuffer);

                //编译失败输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuffer compileErrorOutputBuffer = new StringBuffer();
                //逐行读取数据
                String errorReadLine;
                while ((errorReadLine = errorBufferedReader.readLine()) != null) {
                    compileErrorOutputBuffer.append(errorReadLine);
                }
                System.out.println(compileErrorOutputBuffer);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
