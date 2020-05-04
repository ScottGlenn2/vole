package com.github.vole.demo.service;

import com.github.vole.demo.model.Station;
import com.github.vole.demo.model.TicketInfo;
import com.github.vole.demo.model.TravelInfo;

import java.util.List;

public interface TrainService {

    /**
     * 根据trainNo查询当前线路所有站点
     *
     * @param train_no
     * @param startStationCode
     * @param arriveStationCode
     * @param train_date
     * @return
     */
    public List<Station> stationsOnThisLieChe(String train_no, String startStationCode, String arriveStationCode, String train_date);


    /**
     * 查询可预订车次
     * @param train_date
     * @param from_station
     * @param to_station
     * @return
     */
    TravelInfo queryTrain(String train_date, Station from_station, Station to_station);




    /**
     * 查询列车余票
     * @param train_date
     * @param from_station
     * @param to_station
     * @param prefer
     * @return
     */
    public List<TicketInfo> queryLeftTicket(String train_date, Station from_station, Station to_station, String prefer);




    StringBuilder queryLeftTicketString(String train_date, Station from_station, Station to_station, String prefer);

}
