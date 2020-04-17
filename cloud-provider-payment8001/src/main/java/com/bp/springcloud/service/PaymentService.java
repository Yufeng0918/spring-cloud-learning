package com.bp.springcloud.service;

import com.bp.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Param;

/**
 * @Auther: daiyu
 * @Date: 15/4/20 11:16
 * @Description:
 */
public interface PaymentService {

    public int create(Payment payment);

    public Payment getPaymentById(@Param("id") Long id);
}
