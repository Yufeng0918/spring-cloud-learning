package com.bp.springcloud.alibaba.service;

import com.bp.springcloud.entities.CommonResult;
import com.bp.springcloud.entities.Payment;
import org.springframework.stereotype.Component;


@Component
public class PaymentFallbackService implements PaymentService {

    @Override
    public CommonResult<Payment> paymentSQL(Long id) {
        return new CommonResult<>(446, "fallback, PaymentFallbackService", new Payment(id, "errorSerial"));
    }
}
