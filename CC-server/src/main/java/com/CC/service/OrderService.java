package com.CC.service;

import com.CC.dto.*;
import com.CC.result.PageResult;
import com.CC.vo.OrderPaymentVO;
import com.CC.vo.OrderStatisticsVO;
import com.CC.vo.OrderSubmitVO;
import com.CC.vo.OrderVO;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {


    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);


    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /**
     * 订单详情页
     * @param id
     * @return
     */
    OrderVO detail(Long id);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);

    PageResult pageQuery4Admin(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO orderStatic();

    void cancelByAdmin(OrdersCancelDTO ordersCancelDTO);

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void delivery(Long id);

    void complete(Long id);

    void rejectByAdmin(OrdersRejectionDTO ordersRejectionDTO);

    void reminder(Long id);
}
