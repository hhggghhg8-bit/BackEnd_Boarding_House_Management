package com.example.QLPT.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL "/uploads/**" tới thư mục vật lý "uploads/" ở máy chủ
        // file:uploads/ tương đương với thư mục gốc của Project Spring Boot
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
