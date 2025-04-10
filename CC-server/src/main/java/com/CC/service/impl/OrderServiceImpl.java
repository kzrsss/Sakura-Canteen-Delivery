package com.CC.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.CC.constant.MessageConstant;
import com.CC.context.BaseContext;

import com.CC.dto.*;
import com.CC.entity.*;
import com.CC.exception.AddressBookBusinessException;
import com.CC.exception.OrderBusinessException;
import com.CC.exception.ShoppingCartBusinessException;
import com.CC.mapper.*;
import com.CC.result.PageResult;
import com.CC.service.OrderService;
import com.CC.vo.OrderPaymentVO;
import com.CC.vo.OrderStatisticsVO;
import com.CC.vo.OrderSubmitVO;
import com.CC.vo.OrderVO;
import com.CC.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional//事务注解
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常
        //收货地址为空；不允许下单
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车数据为空：不允许下单
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> lists = shoppingCartMapper.list(shoppingCart);
        if (lists == null && lists.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());
        //地址
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDetail();
        orders.setAddress(address);

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailslist = new ArrayList<>();
        //向订单明细表插入多条数据
        for (ShoppingCart list : lists) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(list, orderDetail);
            orderDetail.setOrderId(orders.getId());//关联id
            orderDetailslist.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailslist);
        //情况购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
        //封装VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        paySuccess(ordersPaymentDTO.getOrderNumber());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map = new HashMap<>();
        map.put("type", 1);//1来单提醒 2用户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        try {
            PageHelper.startPage(page, pageSize);
            OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
            ordersPageQueryDTO.setStatus(status);
            ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());


            List<OrderVO> list = new ArrayList<>();
            Page<Orders> pageResult = orderMapper.pageQuery4User(ordersPageQueryDTO);

            if(pageResult!=null && pageResult.size()>0){
                for(Orders order:pageResult){
                    List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());

                    OrderVO orderVO = new OrderVO();
                    BeanUtils.copyProperties(order,orderVO);
                    orderVO.setOrderDetailList(orderDetails);

                    list.add(orderVO);
                }
            }

            return new PageResult(pageResult.getTotal(), list);
        } catch (Exception e) {
            log.info("查询错误:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 订单详情页
     * @param id
     * @return
     */
    public OrderVO detail(Long id) {
        Orders order = orderMapper.getById(id);

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    public void cancel(Long id){
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消


        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

//        // 订单处于待接单状态下取消，需要进行退款
//        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
        orders.setPayStatus(Orders.REFUND);
//        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    public void repetition(Long id){
        //清空当前购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
        //从订单表查询该订单的订单详细数据
        List<OrderDetail> dlist = orderMapper.getDetailesById(id);
        List<ShoppingCart> cartList = new ArrayList<>();

        for(OrderDetail detail:dlist){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(detail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            cartList.add(shoppingCart);
        }

        shoppingCartMapper.insertBatch(cartList);
    }

    @Override
    public PageResult pageQuery4Admin(OrdersPageQueryDTO ordersPageQueryDTO) {
        try {
            PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

            Page<Orders> pageResult = orderMapper.pageQuery4User(ordersPageQueryDTO);

            List<OrderVO> orderVOList = getOrderVOList(pageResult);


            return new PageResult(pageResult.getTotal(), orderVOList);
        } catch (Exception e) {
            log.info("查询错误:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    public OrderStatisticsVO orderStatic() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消


        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

//        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

        //支付状态修改为 退款
        orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = orderMapper.getById(ordersConfirmDTO.getId());
        if(order == null || order.getStatus()!=2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(3)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 派送
     * @param id
     */
    public void delivery(Long id){
        Orders order = orderMapper.getById(id);
        if(order == null || order.getStatus()!=3){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(4)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {
        Orders order = orderMapper.getById(id);
        if(order == null || order.getStatus()!=4){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .deliveryTime(LocalDateTime.now())
                .id(id)
                .status(5)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    public void rejectByAdmin(OrdersRejectionDTO ordersRejectionDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消


        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

//        // 订单处于待接单状态下取消，需要进行退款
//        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

        //支付状态修改为 退款
        orders.setPayStatus(Orders.REFUND);
//        }
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelReason("商家拒单"+ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    /**
     * 催单
     * @param id
     */
    public void reminder(Long id) {
        Orders order = orderMapper.getById(id);
        if(order == null || order.getStatus()==Orders.PAID){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号"+order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    private List<OrderVO> getOrderVOList(Page<Orders> pageResult) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = pageResult.getResult();

        if(pageResult!=null && pageResult.size()>0){
            for(Orders order:ordersList){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDishes(getOrderDishes(order));
                orderVOList.add(orderVO);
            }
        }

        return orderVOList;

    }

    private String getOrderDishes(Orders order) {
        List<OrderDetail> dlist = orderDetailMapper.getByOrderId(order.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = dlist.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
}

