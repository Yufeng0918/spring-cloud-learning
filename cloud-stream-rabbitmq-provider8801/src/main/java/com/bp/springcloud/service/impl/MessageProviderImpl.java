package com.bp.springcloud.service.impl;

import com.bp.springcloud.service.IMessageProvider;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.stereotype.Service;

/**
 * @Auther: daiyu
 * @Date: 17/4/20 23:37
 * @Description:
 */
@Service
@EnableBinding(Source.class)
public class MessageProviderImpl implements IMessageProvider {

    @Override
    public String send() {
        return null;
    }
}
