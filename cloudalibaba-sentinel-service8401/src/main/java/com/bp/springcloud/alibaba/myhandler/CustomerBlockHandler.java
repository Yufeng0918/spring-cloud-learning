package com.bp.springcloud.alibaba.myhandler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.bp.springcloud.entities.CommonResult;


public class CustomerBlockHandler {

    public static CommonResult handlerException(BlockException exception) {
        return new CommonResult(445, "customized global handlerException----1");
    }

    public static CommonResult handlerException2(BlockException exception) {
        return new CommonResult(444, "customized global handlerException----2");
    }
}
