package com.gitdatsanvich.sweethome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;
import java.io.File;

@SpringBootApplication
public class SweetHomeApplication {

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
}
