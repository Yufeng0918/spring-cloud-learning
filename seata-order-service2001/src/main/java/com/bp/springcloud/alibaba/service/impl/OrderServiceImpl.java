package com.bp.springcloud.alibaba.service.impl;

import com.bp.springcloud.alibaba.dao.OrderDao;
import com.bp.springcloud.alibaba.domain.Order;
import com.bp.springcloud.alibaba.service.AccountService;
import com.bp.springcloud.alibaba.service.OrderService;
import com.bp.springcloud.alibaba.service.StorageService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Auther: daiyu
 * @Date: 21/4/20 15:04
 * @Description:
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private StorageService storageService;

    @Resource
    private AccountService accountService;

    @Override
    @GlobalTransactional(name = "fsp-create-order", rollbackFor = Exception.class)
    public void create(Order order) {

        log.info("create new order");
        orderDao.create(order);

        log.info("decrease inventory");
        storageService.decrease(order.getProductId(), order.getCount());

        log.info("decrease amount");
        accountService.decrease(order.getUserId(), order.getMoney());

        log.info("update order");
        orderDao.update(order.getUserId(), 0);

        log.info("Done!");
    }
}
