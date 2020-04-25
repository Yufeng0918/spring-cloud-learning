package com.bp.springcloud.alibaba.service.impl;

import com.bp.springcloud.alibaba.dao.StorageDao;
import com.bp.springcloud.alibaba.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Resource
    private StorageDao storageDao;

    @Override
    public void decrease(Long productId, Integer count) {
        LOGGER.info("------->storage-service decrease start");
        storageDao.decrease(productId, count);
        LOGGER.info("------->storage-service decrease end");
    }
}
