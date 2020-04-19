package com.github.vole.demo.model;

import lombok.Data;

@Data
public class Station {

    String arrive_time;
    String station_code;
    String station_name;
    String start_time;
    String stopover_time;
    String station_no;
    Boolean isEnabled;
}
