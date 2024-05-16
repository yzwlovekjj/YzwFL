package com.yzw.FL.Server;

import com.yzw.FL.Common.FLConfig;
import com.yzw.FL.Common.MySerializer;
import com.yzw.FL.Message.Client_Ready;
import com.yzw.FL.Message.Client_Training;
import com.yzw.FL.Message.MessageType;
import com.yzw.FL.Message.Server_Model;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class FLServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private FLConfig flConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg)  {
        //处理消息
        try {
            process(msg, channelHandlerContext);
        } catch (Exception e) {
            e.printStackTrace();
            channelHandlerContext.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

    private void process(ByteBuf msg, ChannelHandlerContext channelHandlerContext) throws Exception {
        short messageType = msg.readShort();
        if (messageType != MessageType.Client_Ready.getCode() && messageType != MessageType.Client_Training.getCode()) {
            throw new IllegalArgumentException("暂不支持此种数据" + messageType);
        }
        log.info("客户端接收数据成功,消息为" + MessageType.TypeFromCode(messageType));
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes);
        Object deserialize = MySerializer.deserialize(bytes, messageType);
        //如果是Client_Ready消息
        if(messageType == MessageType.Client_Ready.getCode()){
            processClientReady(channelHandlerContext, (Client_Ready) deserialize);
        }
        //如果是Client_Training消息
        if(messageType == MessageType.Client_Training.getCode()){
            processClientTraining(channelHandlerContext, (Client_Training) deserialize);
        }
        if(FLServer.channels.size() == flConfig.getNumber()){
            boolean allTrue = FLServer.dataMap.entrySet()
                    .stream()
                    .allMatch(Map.Entry::getValue);
            if(allTrue){
                if(messageType == MessageType.Client_Training.getCode()){
                    String pythonScriptPath = "src/main/java/com/yzw/FL/Learning/aggregate.py";
                    ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);
                    if(processStart(processBuilder)){
                        log.info("Models are aggregated successfully.");
                    }
                }
                sendModel();
                FLServer.dataMap.forEach((key, value) -> FLServer.dataMap.put(key, false));
            }
        }


    }

    private void processClientTraining(ChannelHandlerContext ctx, Client_Training msg) {
        FLServer.dataMap.put(ctx.channel(), true);
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = remoteAddress.getAddress().getHostAddress();
        int port = remoteAddress.getPort();
        ip = ip + "_" + port;
        File file = new File(String.format("src/main/java/com/yzw/FL/Learning/%s.pth", ip));
        // 文件夹不存在时尝试创建
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(msg.getParameters());

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processClientReady(ChannelHandlerContext channelHandlerContext, Client_Ready deserialize){
        FLServer.dataMap.put(channelHandlerContext.channel(), true);
    }

    private void sendModel() throws Exception {
        byte[] parameters = readBytesFromFile();
        ChannelGroupFuture sendFuture = FLServer.channels.writeAndFlush(new Server_Model(FLServer.epoch, parameters));
        sendFuture.addListener((ChannelGroupFutureListener) future -> {
            if (future.isSuccess()) {
                // 响应发送成功，现在可以安全关闭连接
                log.info("Send Model Success");
            } else {
                for (ChannelFuture channelFuture : future) {
                    if (!channelFuture.isSuccess()) {
                        Throwable error = channelFuture.cause();
                        log.error("Send message failure for channel: " + channelFuture.channel() + ", error: " + error.getMessage());
                    }
                }
                // 处理发送失败的情况
                Throwable error = future.cause();
                log.info("Send message failure: " + error.getMessage());
            }
        });
    }

    //文件转成bytes[]数组
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
            log.info("Python scrip exited with code: " + exitCode);
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
}
