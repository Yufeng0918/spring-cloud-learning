package com.bp.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * @Auther: daiyu
 * @Date: 16/4/20 23:07
 * @Description:
 */
@Component
public class PaymentFallbackService implements PaymentHystrixService {

    @Override
    public String paymentInfoOK(Integer id) {
        return "paymentInfoOK fallback";
    }

    @Override
    public String paymentInfoTimeOut(Integer id) {
        return "paymentInfoTimeOut fallback";
    }
}
