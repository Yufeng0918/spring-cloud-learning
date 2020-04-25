package com.bp.springcloud.controller;

import com.bp.springcloud.service.PaymentHystrixService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Auther: daiyu
 * @Date: 16/4/20 11:00
 * @Description:
 */
@RestController
@Slf4j
@DefaultProperties(defaultFallback = "paymentGlobalFallbackMethod")
public class OrderFeignController {

    @Resource
    private PaymentHystrixService paymentHystrixService;


    @GetMapping("/consumer/payment/hystrix/ok/{id}")
//    @HystrixCommand
    public String paymentInfoOK(@PathVariable("id") Integer id) {
//        int a = 1 / 0;
        String result = paymentHystrixService.paymentInfoOK(id);
        return result;
    }

    @GetMapping("/consumer/payment/hystrix/timeout/{id}")
    @HystrixCommand(fallbackMethod = "paymentTimeOutFallbackMethod", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1500")
    })
    public String paymentInfoTimeOut(@PathVariable("id") Integer id) {
//        int age = 10/0;
        String result = paymentHystrixService.paymentInfoTimeOut(id);
        return result;
    }

    public String paymentTimeOutFallbackMethod(@PathVariable("id") Integer id) {
        return "payment service overload, please try again later";
    }

    public String paymentGlobalFallbackMethod() {
        return "Global Fallback";
    }
}
