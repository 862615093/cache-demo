package com.ww.cache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
//@MapperScan("com.ww.cache.mapper")
//@EnableTransactionManagement
//@EnableAsync
public class SpringbootCache {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootCache.class, args);
    }
}