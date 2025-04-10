package com.CC.mapper;

import com.github.pagehelper.Page;
import com.CC.dto.GoodsSalesDTO;
import com.CC.dto.OrdersPageQueryDTO;
import com.CC.entity.OrderDetail;
import com.CC.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

//    /**
//     * 用于替换微信支付更新数据库状态的问题
//     * @param orderStatus
//     * @param orderPaidStatus
//     */
//    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
//    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    /**
     * 订单详情页
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id} ")
    Orders getById(Long id);

    @Select("select * from order_detail where order_id = #{id} ")
    List<OrderDetail> getDetailesById(Long id);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 查询超时订单
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status , LocalDateTime orderTime);

    /**
     * 根据条件统计营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 订单数据
     * @param map
     * @return
     */
    Integer countOrderByMap(Map map);

    /**
     * 统计销量top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
