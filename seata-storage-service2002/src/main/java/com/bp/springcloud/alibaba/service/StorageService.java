package com.bp.springcloud.alibaba.service;


public interface StorageService {

    void decrease(Long productId, Integer count);
}
