package com.github.vole.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.vole.common.utils.StringUtil;
import com.github.vole.demo.model.*;
import com.github.vole.demo.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TrainServiceImpl implements TrainService {

    @Autowired
    private StationUtil stationUtil;

    StringBuilder ticketInfo = null;

    long sleepTime = 2000;
    int time = 3;

    private String Cookie = "RAIL_DEVICEID=EZ1Cs8avREtgQqL1b6WlQ4hVUIUP85cVj7NEgSZ0okzojzHYqCjC3YRVuZflhTbhSlIXrVU1FnYxIUawHqSfggTnp03uLi-x_9qKKt3JjpUebPMOVKiDWyG5Bormq7t7-rvhdx-py-wILRA0ZqgwtlylwaoEx4La;";
    private String allCookie = "tk=WhwI9v59EQs6F09BLiaONaTXRdoQ9Ah3tXu4K4jaRLkubm1m0; JSESSIONID=F99999247F96C00093CC9F75A04CEC24;" +
            " BIGipServerotn=703595018.38945.0000; BIGipServerpool_passport=300745226.50215.0000; RAIL_EXPIRATION=1587587122006;" +
            " RAIL_DEVICEID=WaHx3lxQpHgGV7Jsw0LVPFDBhQpivsg8baQCnYGQvM8G6w5ahtLoezgy2kUcSf1oHQAX2viVJMR26CW30yl3tiu1eO50Uu1gtXkxXo" +
            "W9199Dtry27u4KFgY7yJ8hRM5SK_RgRz_ws71LR8IY5Tu5nfOupmEc9e26; route=c5c62a339e7744272a54643b3be5bf64; " +
            "_jc_save_fromDate=2020-05-01; _jc_save_wfdc_flag=dc; _jc_save_fromStation=%u957F%u6625%2CCCT; " +
            "_jc_save_toStation=%u897F%u5B89%2CXAY; _jc_save_toDate=2020-04-19";


    /**
     * 根据trainNo查询当前线路所有站点
     *
     * @param train_no
     * @param startStationCode
     * @param arriveStationCode
     * @param train_date
     * @return
     */
    public List<Station> stationsOnThisLieChe(String train_no, String startStationCode, String arriveStationCode, String train_date) {
        // 查询当前线路所有站点
        String params = "train_no=" + train_no + "&from_station_telecode=" + startStationCode + "&to_station_telecode=" + arriveStationCode + "&depart_date=" + train_date;
        String url = "https://kyfw.12306.cn/otn/czxx/queryByTrainNo?" + params;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder().addHeader("Cookie", Cookie).get()
                .url(url)
                .build();
        OkHttpClient eagerClient = okHttpClient.newBuilder().readTimeout(50000, TimeUnit.MILLISECONDS).build();
        List<Station> stations = null;//需要转换的json数组
        try {
            Response response = eagerClient.newCall(request).execute();
            boolean error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            // 如果请求失败，就停一段时间再试，设置最大尝试次数
            for (int i = 1; error && i <= time; i++) {
                ticketInfo.append(url + "网络异常").append("\n");
                log.error(url + "网络异常");
                try {
                    Thread.sleep(sleepTime * i);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                response = eagerClient.newCall(request).execute();
                error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            }
            if (response.body() != null) {
                String string = response.body().string();
                boolean notBlank = StringUtil.isNotBlank(string);
                if (notBlank && !error) {
                    ResponseInfo2 ticketInfo = JSONObject.parseObject(string, ResponseInfo2.class);
                    Map data = ticketInfo.getData();
                    stations = JSONObject.parseArray(data.get("data").toString(), Station.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stations;
    }

    /**
     * 查询可预订车次
     * @param train_date
     * @param from_station
     * @param to_station
     * @return
     */
    public TravelInfo queryTrain(String train_date, Station from_station, Station to_station) {
        ticketInfo = new StringBuilder();
        TravelInfo travelInfo = new TravelInfo();
        List<Train> trainList = new LinkedList<>();
        // 查询起始站所有线路 // 查询有无车票 https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=2020-05-01&leftTicketDTO.from_station=TJP&leftTicketDTO.to_station=TTK&purpose_codes=ADULT
        String url = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=" + train_date
                + "&leftTicketDTO.from_station=" + from_station.getStation_code()
                + "&leftTicketDTO.to_station=" + to_station.getStation_code() + "&purpose_codes=ADULT";
//        String checi = "110000K12808"; // k128
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder()
                .addHeader("Cookie", Cookie)
                .get()
                .url(url)
                .build();
        OkHttpClient eagerClient = okHttpClient.newBuilder().readTimeout(50000, TimeUnit.MILLISECONDS).build();
        try {
            Response response = eagerClient.newCall(request).execute();
            boolean error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            // 如果请求失败，就停一段时间再试，设置最大尝试次数
            for (int i = 1; error && i <= time; i++) {
                ticketInfo.append(url + "网络异常").append("\n");
                log.error(url + "网络异常");
                try {
                    Thread.sleep(sleepTime * i);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                response = eagerClient.newCall(request).execute();
                error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            }
            if (response.body() != null) {
                String string = response.body().string();
                boolean notBlank = StringUtil.isNotBlank(string);
                if (notBlank && !error) {
                    ResponseInfo responseInfo = JSONObject.parseObject(string, ResponseInfo.class);
                    Map data = responseInfo.getData();
                    String result = data.get("result").toString();
                    String[] split = result.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        String[] strings = s.split("\\|");
                        // 是否只展示有车票，即可预订的车次与站点情况
                        boolean must = strings.length > 29;
                        if (must) {
                            Train train = new Train();
                            train.setTrainNo(strings[2]);
                            train.setTrainName(strings[3]);
                            train.setFrom_station(from_station);
                            train.setTo_station(to_station);
                            trainList.add(train);
                            log.info(train.toString());
//                for (int i1 = 0; i1 < strings.length; i1++) {
////                    log.info("index:"+i1+",content:"+strings[i1]);
//                    // 2-列车号，3-车次号，6-起始站，7-终点站
//                    // 11:能否预订，23：有无票或票数量，26,28,29
//                    log.info("列车："+strings[3]+",A1票数："+strings[23]+",A3票数："+strings[26]+",A4票数："+strings[28]+",硬座："+strings[29]);
//                }
                        }
                    }
//            tickets = JSONObject.parseArray(data.get("result").toString(), Ticket.class);
//            log.info( "当前线路所有站点名称及顺序号，到站和发站时间: " + tickets);

                }else {
                    ticketInfo.append(url + "网络异常").append("\n");
                    log.error(url + "网络异常");
                }
            }else {
                ticketInfo.append(url + "网络异常").append("\n");
                log.error(url + "网络异常");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        travelInfo.setTrainList(trainList);
        return travelInfo;
    }


    public StringBuilder queryLeftTicketString(String train_date, Station from_station, Station to_station, String prefer) {
        // 查询起始站所有线路 // 查询有无车票 https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=2020-05-01&leftTicketDTO.from_station=TJP&leftTicketDTO.to_station=TTK&purpose_codes=ADULT
        String url = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=" + train_date
                + "&leftTicketDTO.from_station=" + from_station.getStation_code()
                + "&leftTicketDTO.to_station=" + to_station.getStation_code() + "&purpose_codes=ADULT";
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder()
                .addHeader("Cookie", Cookie)
                .get()
                .url(url)
                .build();
        OkHttpClient eagerClient = okHttpClient.newBuilder().readTimeout(50000, TimeUnit.MILLISECONDS).build();
        try {
            Response response = eagerClient.newCall(request).execute();
            boolean error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            // 如果请求失败，就停一段时间再试，设置最大尝试次数
            for (int i = 1; error && i <= time; i++) {
                try {
                    Thread.sleep(sleepTime * i);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                response = eagerClient.newCall(request).execute();
                error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            }
            if (response.body() != null) {
                String string = response.body().string();
//            log.info(string);
                boolean notBlank = StringUtil.isNotBlank(string);
                if (notBlank && !error) {
                    ResponseInfo responseInfo = JSONObject.parseObject(string, ResponseInfo.class);
                    Map data = responseInfo.getData();
                    String result = data.get("result").toString();
                    String[] split = result.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
//                log.info("index:"+i+",content:"+s);
                        String s2 = new String(s);
                        String[] strings = s.split("\\|");
                        // 是否只展示有车票，即可预订的车次与站点情况
                        boolean must;
                        if (StringUtil.isNotBlank(prefer)) {
                            must = strings.length > 29 && strings[11].equalsIgnoreCase("Y") && strings[3].equalsIgnoreCase(prefer);
                        } else {
                            must = strings.length > 29 && strings[11].equalsIgnoreCase("Y");
                        }
                        if (must) {
                            String[] split1 = s2.split("\\|预订\\|");
                            String[] split2 = split1[1].split("\\|Y\\|");
                            String s1 = split2[0];
                            String ss = new String(s1);
                            String[] split4 = ss.split("\\|");
                            Station fromStation = stationUtil.findStation(split4[4]);
                            Station toStation = stationUtil.findStation(split4[5]);
                            String substring = train_date.substring(0, 4);
                            String[] split3 = split2[1].split("\\|" + substring);
                            String s3 = split3[1];
                            String lieChe = "列车："+split4[1]+"\t"+fromStation.getStation_name()+"-"+toStation.getStation_name();
                            String yuPao = "余票："+s3;
                            log.info(lieChe+"\n");
                            log.info(s3+"\n");
                            ticketInfo.append(lieChe).append("\n").append(yuPao).append("\n");
                        }
                    }
                }else {
                    ticketInfo.append(url + "网络异常").append("\n");
                    log.error(url + "网络异常");
                }
            }else {
                ticketInfo.append(url + "网络异常").append("\n");
                log.error(url + "网络异常");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ticketInfo;
    }


    /**
     * 查询列车余票
     * @param train_date
     * @param from_station
     * @param to_station
     * @param prefer
     * @return
     */
    @Deprecated
    public List<TicketInfo> queryLeftTicket(String train_date, Station from_station, Station to_station, String prefer) {
        List<TicketInfo> ticketInfoList = new LinkedList<>();
        // 查询起始站所有线路 // 查询有无车票 https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=2020-05-01&leftTicketDTO.from_station=TJP&leftTicketDTO.to_station=TTK&purpose_codes=ADULT
        String url = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=" + train_date
                + "&leftTicketDTO.from_station=" + from_station.getStation_code()
                + "&leftTicketDTO.to_station=" + to_station.getStation_code() + "&purpose_codes=ADULT";
//        String checi = "110000K12808"; // k128
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder()
                .addHeader("Cookie", Cookie)
                .get()
                .url(url)
                .build();
        OkHttpClient eagerClient = okHttpClient.newBuilder().readTimeout(50000, TimeUnit.MILLISECONDS).build();
        try {
            Response response = eagerClient.newCall(request).execute();
            boolean error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            // 如果请求失败，就停一段时间再试，设置最大尝试次数
            for (int i = 1; error && i <= time; i++) {
                log.error(url + "网络异常");
                try {
                    Thread.sleep(sleepTime * i);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                response = eagerClient.newCall(request).execute();
                error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            }
            if (response.body() != null) {
                String string = response.body().string();
//            log.info(string);
                boolean notBlank = StringUtil.isNotBlank(string);
                if (notBlank && !error) {
                    ResponseInfo responseInfo = JSONObject.parseObject(string, ResponseInfo.class);
                    Map data = responseInfo.getData();
                    String result = data.get("result").toString();
                    String[] split = result.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
//                log.info("index:"+i+",content:"+s);
                        String s2 = new String(s);
                        String[] strings = s.split("\\|");
                        // 是否只展示有车票，即可预订的车次与站点情况
                        boolean must;
                        if (StringUtil.isNotBlank(prefer)) {
                            must = strings.length > 29 && strings[11].equalsIgnoreCase("Y") && strings[3].equalsIgnoreCase(prefer);
                        } else {
                            must = strings.length > 29 && strings[11].equalsIgnoreCase("Y");
                        }
                        if (must) {
                            log.info(s2.replace("\\|", ""));
                            TicketInfo ticketInfo = new TicketInfo();
                            ticketInfo.setTrain_no(strings[2]);
                            ticketInfo.setLieChe(strings[3]);
                            ticketInfo.setFrom_station(from_station.getStation_name());
                            ticketInfo.setTo_station(to_station.getStation_name());
                            ticketInfo.setA1(strings[23]);
                            ticketInfo.setA3(strings[26]);
                            ticketInfo.setA4(strings[28]);
                            ticketInfo.setYZorWZ(strings[29]);
                            ticketInfoList.add(ticketInfo);
                            log.info(ticketInfo.toString());
//                for (int i1 = 0; i1 < strings.length; i1++) {
////                    log.info("index:"+i1+",content:"+strings[i1]);
//                    // 2-列车号，3-车次号，6-起始站，7-终点站
//                    // 11:能否预订，23：有无票或票数量，26,28,29
//                    log.info("列车："+strings[3]+",A1票数："+strings[23]+",A3票数："+strings[26]+",A4票数："+strings[28]+",硬座："+strings[29]);
//                }
                        }
                    }
//            tickets = JSONObject.parseArray(data.get("result").toString(), Ticket.class);
//            log.info( "当前线路所有站点名称及顺序号，到站和发站时间: " + tickets);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ticketInfoList;
    }
}
