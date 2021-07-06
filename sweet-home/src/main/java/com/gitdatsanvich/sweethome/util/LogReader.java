package com.gitdatsanvich.sweethome.util;

import com.gitdatsanvich.common.util.DingDingAlert;
import com.gitdatsanvich.sweethome.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangChen
 * @date 2021/7/2 12:43
 */
@Component
@Slf4j
public class LogReader {
    private final AtomicInteger readLine = new AtomicInteger(0);
    @Value("${log-reader.path}")
    private String LOG_PATH;

    @Scheduled(cron = "0/5 * * * * *")
    public void read() {
        //读取目录下文件
        try {
            readFile();
        } catch (IOException e) {
            log.error("日志读取异常", e);
            DingDingAlert.pushAlert("异常！", "日志读取异常！" + e.getMessage());
        }
    }

    private void readFile() throws IOException {
        /* Construct BufferedReader from FileReader */
        FileReader in = new FileReader(LOG_PATH);
        BufferedReader br = new BufferedReader(in);
        String line;
        int lineIndex = 0;
        while ((line = br.readLine()) != null) {
            lineIndex++;
            if (lineIndex > readLine.get()) {
                readLine.incrementAndGet();
                //推送log
                WebSocketServer.sendAll(line);
            }
        }
        in.close();
        br.close();
    }
}
