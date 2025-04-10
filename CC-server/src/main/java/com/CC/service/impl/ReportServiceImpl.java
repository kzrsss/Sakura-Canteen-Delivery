package com.CC.service.impl;

import com.CC.dto.GoodsSalesDTO;
import com.CC.entity.Orders;
import com.CC.mapper.OrderMapper;
import com.CC.mapper.UserMapper;
import com.CC.service.ReportService;
import com.CC.vo.OrderReportVO;
import com.CC.vo.SalesTop10ReportVO;
import com.CC.vo.TurnoverReportVO;
import com.CC.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        
        List<Double> turnoverlist = new ArrayList<>();
        List<LocalDate> datelist = getLocalDates(begin, end);
        for(LocalDate date:datelist){
            LocalDateTime begint = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endt = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin",begint);
            map.put("end",endt);
            map.put("status", Orders.COMPLETED);
            Double turnOver = orderMapper.sumByMap(map);
            turnOver = turnOver==null?0.0:turnOver;
            turnoverlist.add(turnOver);
        }
        String datel = StringUtil.join(",", datelist).replace("[","").replace("]","");
        String turnoverl = StringUtil.join(",", turnoverlist).replace("[","").replace("]","");

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(datel)
                .turnoverList(turnoverl)
                .build();

        return turnoverReportVO;
    }

    /**
     * 新增用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist = getLocalDates(begin,end);
        List<Integer> userNumList = new ArrayList<>();
        List<Integer> totalUserNumList = new ArrayList<>();
        Integer total = 0;
        for(LocalDate date:datelist){
            LocalDateTime begint = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endt = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin",begint);
            map.put("end",endt);
            Integer userNum = userMapper.countByMap(map);
            userNum = userNum==null?0:userNum;
            userNumList.add(userNum);
            total += userNum;
            totalUserNumList.add(total);
        }
        String datel = StringUtil.join(",", datelist).replace("[","").replace("]","");
        String userNuml = StringUtil.join(",", userNumList).replace("[","").replace("]","");
        String totalUserNuml = StringUtil.join(",", totalUserNumList).replace("[","").replace("]","");

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(datel)
                .newUserList(userNuml)
                .totalUserList(totalUserNuml)
                .build();

        return userReportVO;
    }

    @Override
    public OrderReportVO getOderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist = getLocalDates(begin,end);
        List<Integer> orderNumList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        Integer validOrder = 0;
        Integer totalOrder = 0;
        for(LocalDate date:datelist){
            LocalDateTime begint = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endt = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin",begint);
            map.put("end",endt);
            Integer orderNum = orderMapper.countOrderByMap(map);
            map.put("status",Orders.COMPLETED);
            Integer validorderNum = orderMapper.countOrderByMap(map);
            orderNum = orderNum==null?0:orderNum;
            validorderNum = validorderNum==null?0:validorderNum;
            orderNumList.add(orderNum);
            validOrderList.add(validorderNum);
            validOrder += validorderNum;
            totalOrder += orderNum;
        }
        Double validRate = (validOrder*1.0)/totalOrder;
        String datel = StringUtil.join(",", datelist).replace("[","").replace("]","");
        String orderNuml = StringUtil.join(",", orderNumList).replace("[","").replace("]","");
        String validOrderNuml = StringUtil.join(",", validOrderList).replace("[","").replace("]","");

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(datel)
                .orderCountList(orderNuml)
                .validOrderCountList(validOrderNuml)
                .validOrderCount(validOrder)
                .totalOrderCount(totalOrder)
                .orderCompletionRate(validRate)
                .build();

        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {

        LocalDateTime begint = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endt = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesList = orderMapper.getSalesTop10(begint,endt);
        List<String> namelist = goodsSalesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberlist = goodsSalesList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String namel = StringUtil.join(",", namelist).replace("[","").replace("]","");
        String numberl = StringUtil.join(",", numberlist).replace("[","").replace("]","");

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(namel)
                .numberList(numberl)
                .build();

        return salesTop10ReportVO;

    }

    private static List<LocalDate> getLocalDates(LocalDate begin, LocalDate end) {
        //存放从begin到end范围内每天的日期
        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            datelist.add(begin);
        }
        return datelist;
    }
}
