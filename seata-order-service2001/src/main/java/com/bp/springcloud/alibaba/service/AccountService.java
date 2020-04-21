package com.bp.springcloud.alibaba.service;

import com.bp.springcloud.entities.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * @Auther: daiyu
 * @Date: 21/4/20 15:05
 * @Description:
 */
@FeignClient(value = "seata-account-service")
public interface AccountService {

    @PostMapping("/account/decrease")
    CommonResult decrease(@RequestParam("productId") Long productId, @RequestParam("money") BigDecimal money);
}
