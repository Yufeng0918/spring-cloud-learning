package com.bp.springcloud.alibaba.controller;

import com.bp.springcloud.alibaba.domain.Order;
import com.bp.springcloud.alibaba.service.OrderService;
import com.bp.springcloud.entities.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Auther: daiyu
 * @Date: 21/4/20 15:34
 * @Description:
 */
@RestController
public class OrderController {


    @Resource
    private OrderService orderService;


    @GetMapping("/order/create")
    public CommonResult create(Order order) {
        orderService.create(order);
        return new CommonResult(200, "create order");
    }
}
