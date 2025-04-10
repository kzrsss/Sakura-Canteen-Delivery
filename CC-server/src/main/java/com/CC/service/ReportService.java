package com.CC.service;

import com.CC.vo.OrderReportVO;
import com.CC.vo.SalesTop10ReportVO;
import com.CC.vo.TurnoverReportVO;
import com.CC.vo.UserReportVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ReportService {
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end);
}
