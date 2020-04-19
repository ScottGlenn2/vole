package com.github.vole.demo.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResponseInfo2 {

    String validateMessagesShowId;
    Boolean status;
    Long httpstatus;
    Map data;
    List messages;
    Object validateMessages;
}
