package com.bp.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @Auther: daiyu
 * @Date: 15/4/20 12:38
 * @Description:
 */
@RestController
@Slf4j
public class OrderConsulController {

    public static final String PAYMENT_URL = "http://consul-provider-payment";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping(value = "/consumer/consul")
    public String paymentInfo() {
        return restTemplate.getForObject(PAYMENT_URL + "/payment/consul", String.class);
    }

}
