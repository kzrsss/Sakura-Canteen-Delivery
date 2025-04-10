package com.CC.mapper;

import com.CC.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细数据
     * @param orderDetailslist
     */
    void insertBatch(List<OrderDetail> orderDetailslist);

    @Select("select * from order_detail where order_id = #{id} ")
    List<OrderDetail> getByOrderId(Long id);
}
