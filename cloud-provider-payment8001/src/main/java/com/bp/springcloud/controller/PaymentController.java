package com.bp.springcloud.controller;

import com.bp.springcloud.entities.CommonResult;
import com.bp.springcloud.entities.Payment;
import com.bp.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: daiyu
 * @Date: 15/4/20 11:18
 * @Description:
 */
@RestController
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping(value = "/payment/create")
    public CommonResult create(@RequestBody Payment payment) {
        int result = paymentService.create(payment);
        log.info("payment create result ", result);

        if (result > 0) {
            return new CommonResult(200, "success: " + serverPort, result);
        }

        return new CommonResult(444, "failure: " + serverPort, null);
    }


    @GetMapping(value = "/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id) {

        Payment result = paymentService.getPaymentById(id);
        log.info("payment query result is: ", result);

        if (result != null) {
            return new CommonResult(200, "success: " + serverPort, result);
        }

        return new CommonResult(444, "failure: " + serverPort, null);
    }


    @GetMapping(value = "/payment/discovery")
    public Object discovery() {
        List<String> serviceList = discoveryClient.getServices();
        serviceList.forEach(s -> log.info("service: " + s));

        List<ServiceInstance> instanceList = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        instanceList
                .forEach(i -> log.info(i.getServiceId() + "\t" + i.getHost() + "\t" + i.getPort() + "\t" + i.getUri()));
        return this.discoveryClient;
    }


    @GetMapping(value = "/payment/lb")
    public String getPaymentLB() {
        return serverPort;
    }


    @GetMapping(value = "/payment/feign/timeout")
    public String getPaymentFeignTimeout() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return serverPort;
    }

    @GetMapping("/payment/zipkin")
    public String paymentZipkin() {
        return "hi ,i'am payment zipkin server fall back，welcome to sg";
    }
}
