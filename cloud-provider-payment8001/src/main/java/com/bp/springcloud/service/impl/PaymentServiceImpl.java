package com.bp.springcloud.service.impl;

import com.bp.springcloud.dao.PaymentDao;
import com.bp.springcloud.entities.Payment;
import com.bp.springcloud.service.PaymentService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Auther: daiyu
 * @Date: 15/4/20 11:16
 * @Description:
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDao paymentDao;

    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    public Payment getPaymentById(@Param("id") Long id) {
        return paymentDao.getPaymentById(id);
    }
}
