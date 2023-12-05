package com.gitdatsanvich.sweethome;

import com.gitdatsanvich.common.constants.CommonConstants;
import com.gitdatsanvich.sweethome.netty.NettyServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.unit.DataSize;

import javax.annotation.Resource;
import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.net.InetSocketAddress;

/**
 * @author gitdatsanvich
 */
@SpringBootApplication
@EnableScheduling
public class SweetHomeApplication implements CommandLineRunner {
    @Resource
    private NettyServer nettyServer;

    public static void main(String[] args) {
        SpringApplication.run(SweetHomeApplication.class, args);
    }

    /**
     * 服务配置
     *
     * @return MultipartConfigElement
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //KB,MB 上传下载传输文件大小配置
        factory.setMaxFileSize(DataSize.ofMegabytes(1024));
        factory.setMaxRequestSize(DataSize.ofMegabytes(1024));
        //文件上传临时路径
        String location = System.getProperty("user.dir") + "/data/tmp";
        File tmpFile = new File(location);
        if (!tmpFile.exists()) {
            boolean mkdirs = tmpFile.mkdirs();
            if (mkdirs) {
                System.out.println(("创建成功"));
            } else {
                System.out.println(("创建失败"));
            }
        }
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }

    @Bean
    @Nullable
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolScheduler = new ThreadPoolTaskScheduler();
        threadPoolScheduler.setThreadNamePrefix("SockJS-");
        threadPoolScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolScheduler.setRemoveOnCancelPolicy(true);
        return threadPoolScheduler;
    }

    @Override
    public void run(String... strings) {
        InetSocketAddress address = new InetSocketAddress(CommonConstants.SOCKET_IP, CommonConstants.PORT);
        System.out.println("netty服务启动地址" + CommonConstants.SOCKET_IP + ":" + CommonConstants.PORT);
        nettyServer.start(address);
    }
}
