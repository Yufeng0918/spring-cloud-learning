package com.bp.springcloud.controller;

import com.bp.springcloud.entities.CommonResult;
import com.bp.springcloud.entities.Payment;
import com.bp.springcloud.lb.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class OrderController {

    public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private LoadBalancer loadBalancer;

    @Resource
    private DiscoveryClient discoveryClient;

    @GetMapping("/consumer/payment/create")
    public CommonResult<Payment> create(Payment payment) {

        return restTemplate.postForObject(PAYMENT_URL + "/payment/create", payment, CommonResult.class);
    }

    @GetMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPayment(@PathVariable("id") Long id) {

        return restTemplate.getForObject(PAYMENT_URL + "/payment/get/" + id, CommonResult.class);
    }

    @GetMapping("/consumer/payment/getForEntity/{id}")
    public CommonResult<Payment> getPaymentEntity(@PathVariable("id") Long id) {

        ResponseEntity<CommonResult> entity =
                restTemplate.getForEntity(PAYMENT_URL + "/payment/get/" + id, CommonResult.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            return entity.getBody();
        }

        return new CommonResult<>(444, "failure");
    }

    @GetMapping(value = "/consumer/payment/lb")
    public String getPaymentLB() {

//        List<ServiceInstance> instanceList = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
//        if (instanceList == null || instanceList.size() == 0) {
//            return null;
//        }
//
//        ServiceInstance serviceInstance = loadBalancer.instances(instanceList);
//        URI uri = serviceInstance.getUri();
        return restTemplate.getForObject(PAYMENT_URL + "/payment/lb", String.class);
    }


    @GetMapping("/consumer/payment/zipkin")
    public String paymentZipkin() {
        String result = restTemplate.getForObject(PAYMENT_URL + "/payment/zipkin/", String.class);
        return result;
    }
}
