package com.github.vole.demo.controller;

import com.github.vole.demo.fegin.TraceService;
import com.github.vole.demo.model.TicketInfo;
import com.github.vole.demo.service.ReadStationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DemoController {

    @Autowired
    TraceService traceService;

    @Autowired
    ReadStationsService readStationsService;

    @GetMapping("/trace/{name}")
    public String demoTrace(@PathVariable String name) {

        return traceService.trace(name);
    }

    @RequestMapping("/ticketInfo")
    public List<TicketInfo> queryTJ(String a, String b, String c, String d){
        return readStationsService.readStations(a, b, c, d);
    }
}
