package com.github.vole.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class TravelInfo {

    // 起始地
    String from;
    // 目的地
    String to;
    // 起始日期
    String fromDate;
    // 到达日期
    String toDate;
    // 可选列车信息
    List<Train> trainList;

}
