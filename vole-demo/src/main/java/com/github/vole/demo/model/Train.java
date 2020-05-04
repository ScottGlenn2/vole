package com.github.vole.demo.model;

import lombok.Data;


@Data
public class Train {

    String trainNo;
    String trainName;
    Station from_station;
    Station to_station;

}
