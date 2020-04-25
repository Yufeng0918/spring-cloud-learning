package com.bp.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: daiyu
 * @Date: 16/4/20 09:28
 * @Description:
 */
@Configuration
public class MyRule {

    @Bean
    public IRule myRibbonRule() {
        return new RoundRobinRule();
    }
}
