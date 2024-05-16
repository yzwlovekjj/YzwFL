package com.yzw.FL.Client;


import com.yzw.FL.Common.MySerializer;
import com.yzw.FL.Message.Client_Training;
import com.yzw.FL.Message.MessageType;
import com.yzw.FL.Message.Server_Model;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;


@Slf4j
public class FLClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg){
        //处理消息
        try {
            process(msg, ctx);
        }catch (Exception e){
            e.printStackTrace();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

    private void process(ByteBuf msg, ChannelHandlerContext ctx) throws RuntimeException{
        short messageType = msg.readShort();
        if (messageType != MessageType.Client_Ready.getCode() && messageType != MessageType.Server_Model.getCode()) {
            throw new IllegalArgumentException("暂不支持此种数据" + messageType);
        }
        log.info("客户端接收数据成功,消息为" + MessageType.TypeFromCode(messageType));
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes);
        Object deserialize = MySerializer.deserialize(bytes, messageType);
        if (messageType == MessageType.Server_Model.getCode()) {
            processServerModel((Server_Model) deserialize, ctx);
        }
    }

    private void processServerModel(Server_Model msg, ChannelHandlerContext ctx){
        File file = new File("src/main/java/com/yzw/FL/Learning/model_state_dict_3.pth");
        // 文件夹不存在时尝试创建
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            log.info("Failed to create directory: " + file.getParentFile());
            throw new RuntimeException("Failed to create directory");
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(msg.getParameters());
            String pythonScriptPath = "src/main/java/com/yzw/FL/Learning/train.py";
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);
            if(processStart(processBuilder)){
                Client_Training cr = new Client_Training(0, readBytesFromFile());
                ctx.writeAndFlush(cr);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean processStart(ProcessBuilder processBuilder) {
        try {
            // 启动进程
            Process process = processBuilder.start();
            // 读取输出流
            try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = outputReader.readLine()) != null) {
                    log.info("Output: " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 读取错误流
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    log.info("Error: " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exitCode = process.waitFor();
            log.info("Exited with code: " + exitCode);
            if (exitCode == 0) {
                log.info("Python script executed successfully.");
            } else {
                log.info("Python script failed with exit code " + exitCode);
            }
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private byte[] readBytesFromFile() throws Exception {
        String filePath = "src/main/java/com/yzw/FL/Learning/model_state_dict.pth";
        File file = new File(filePath);
        if (!file.exists()) {
            throw new NoSuchFileException("File not found at: " + filePath);
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            log.info("Error reading the file: " + e.getMessage());
            throw e;
        }
    }
}
