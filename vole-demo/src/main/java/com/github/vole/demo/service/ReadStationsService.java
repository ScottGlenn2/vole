package com.github.vole.demo.service;

import com.github.vole.demo.model.TicketInfo;

import java.util.List;

public interface ReadStationsService {

    public List<TicketInfo> readStations(String train_date, String startStationName, String arriveStationName, String prefer);
}
