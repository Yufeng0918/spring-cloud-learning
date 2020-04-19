package com.bp.springcloud.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @auther zzyy
 * @create 2020-02-24 16:26
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SentinelMain8401 {

    public static void main(String[] args) {

        SpringApplication.run(SentinelMain8401.class, args);
    }
}
