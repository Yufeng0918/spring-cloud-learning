package com.bp.springcloud.lb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: daiyu
 * @Date: 16/4/20 10:08
 * @Description:
 */
@Component
@Slf4j
public class MyLB implements LoadBalancer {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public final int getAndIncrement() {

        int current;
        int next;

        while (true) {
            current = this.atomicInteger.get();
            next = current >= Integer.MAX_VALUE? 0 : current + 1;
            if (this.atomicInteger.compareAndSet(current, next)) {
                break;
            }
        }
        log.info("MyLB " + next);
        return next;
    }

    @Override
    public ServiceInstance instances(List<ServiceInstance> serviceInstanceList) {

        int serverIdx = getAndIncrement() % serviceInstanceList.size();
        return serviceInstanceList.get(serverIdx);
    }
}
