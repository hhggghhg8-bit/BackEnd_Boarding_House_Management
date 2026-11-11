package com.example.QLPT;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class QlptApplication {

	// Ép toàn bộ hệ thống Backend chạy theo múi giờ Việt Nam (UTC+7)
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		System.out.println("========== ĐÃ ÉP MÚI GIỜ HỆ THỐNG VỀ ASIA/HO_CHI_MINH ==========");
	}

	public static void main(String[] args) {
		SpringApplication.run(QlptApplication.class, args);
	}

}