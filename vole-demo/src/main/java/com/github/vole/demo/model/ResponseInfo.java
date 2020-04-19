package com.github.vole.demo.model;

import lombok.Data;

import java.util.Map;

@Data
public class ResponseInfo {

    Long httpstatus;
    Map data;
    String messages;
    Boolean status;
}
