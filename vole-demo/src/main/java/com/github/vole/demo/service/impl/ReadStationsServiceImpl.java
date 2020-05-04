package com.github.vole.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.vole.common.utils.StringUtil;
import com.github.vole.demo.model.*;
import com.github.vole.demo.service.ReadStationsService;
import com.github.vole.demo.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReadStationsServiceImpl implements ReadStationsService {

    @Autowired
    private TrainService trainService;

    @Autowired
    private StationUtil stationUtil;

    private String prefer = "k128";
    private String train_date = "2020-04-30";
    private String startStationCode = "TJP";
    private String arriveStationCode = "TTK";
    private String train_no = "110000K12808";
    // TODO 账号登陆获取cookie

    // TODO 将stations_names存储到缓存中

    public static void main(String[] args) {
        //获取唯一可用的对象
        ReadStationsServiceImpl readStationsService = new ReadStationsServiceImpl();
        StationUtil stationUtil = new StationUtil();
        log.info(String.valueOf(readStationsService.readStations(readStationsService.train_date, readStationsService.startStationCode,
                readStationsService.arriveStationCode, readStationsService.prefer)));
    }


    public String readStations(String train_date, String startStationName, String arriveStationName, String prefer) {
        StringBuilder ticketString = null;
        Station from_station = null, to_station = null;
        List<TicketInfo> ticketInfoList = new LinkedList<>();
        List<Train> trainList = new LinkedList<>();
        if (StringUtil.isNotBlank(startStationName)) {
            from_station = stationUtil.findStation(startStationName);
        }
        if (StringUtil.isNotBlank(arriveStationName)) {
            to_station = stationUtil.findStation(arriveStationName);
        }
        if (StringUtil.isBlank(train_date) || from_station == null || to_station == null) {
            return null;
        } else {
            // 当前起始站，到达站车票情况
            TravelInfo travelInfo = trainService.queryTrain(train_date, from_station, to_station);
            List<Station> stations = null;
            for (int i = 0; travelInfo.getTrainList() != null && i < travelInfo.getTrainList().size(); i++) {
                Train train = travelInfo.getTrainList().get(i);
                if (StringUtil.isNotBlank(prefer) && prefer.equalsIgnoreCase(train.getTrainName())) {
                    stations = trainService.stationsOnThisLieChe(train.getTrainNo(), from_station.getStation_code(), to_station.getStation_code(), train_date);
                    trainList.addAll(stationUtil.queryTicketInfoListByStations(train, stations, from_station, to_station));
                    break;
                } else {
                    stations = trainService.stationsOnThisLieChe(train.getTrainNo(), from_station.getStation_code(), to_station.getStation_code(), train_date);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    trainList.addAll(stationUtil.queryTicketInfoListByStations(train, stations, from_station, to_station));
                }
            }
            if (CollectionUtils.isNotEmpty(trainList)) {
                for (int i = 0; i < trainList.size(); i++) {
                    Train train = trainList.get(i);
                    ticketString = trainService.queryLeftTicketString(train_date, train.getFrom_station(), train.getTo_station(), train.getTrainName());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return ticketString != null ? ticketString.toString() : "";
    }

}
