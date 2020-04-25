package com.bp.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: daiyu
 * @Date: 16/4/20 14:33
 * @Description:
 */
@Service
public class PaymentService {

    public String paymentInfoOk(Integer id) {
        return "Thread name: " + Thread.currentThread().getName() + " paymentOk, id: " + id;
    }


    @HystrixCommand(fallbackMethod = "paymentInfoTimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
    })
    public String paymentInfoTimeout(Integer id) {

        try {
            int waitTime = 3;
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Thread name: " + Thread.currentThread().getName() + " paymentTimeout, id: " + id;
    }


    public String paymentInfoTimeoutHandler(Integer id) {
        return "Thread name: " + Thread.currentThread().getName() + " paymentInfoTimeoutHandler, id: " + id;
    }


    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
            // request limit
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            // windows to calculate failure rate
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
            // failure percentage
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
        if (id < 0) {
            throw new RuntimeException("Id can not be negative");
        }
        String serialNumber = IdUtil.simpleUUID();
        return Thread.currentThread().getName() + "\t" + "success, serial number: " + serialNumber;
    }

    public String paymentCircuitBreakerFallback(@PathVariable("id") Integer id) {
        return "overload in circuit break " + id;
    }
}
