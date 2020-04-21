package com.bp.springcloud.alibaba.dao;

import com.bp.springcloud.alibaba.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Auther: daiyu
 * @Date: 21/4/20 14:46
 * @Description:
 */

@Mapper
public interface OrderDao {


    void create(Order order);

    void update(@Param("userId") Long userId, @Param("status") Integer status);
}
