package com.github.vole.demo.service.impl;

//import feign.Request;
//import feign.Response;
//import feign.okhttp.OkHttpClient;
import com.alibaba.fastjson.JSONObject;
import com.github.vole.common.utils.StringUtil;
import com.github.vole.demo.model.ResponseInfo;
import com.github.vole.demo.model.ResponseInfo2;
import com.github.vole.demo.model.Station;
import com.github.vole.demo.model.TicketInfo;
import com.github.vole.demo.service.ReadStationsService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReadStationsServiceImpl implements ReadStationsService {

    @Value("${station_names.pathname}")
    public String pathname;
    private String prefer = "k128";
    private String train_date = "2020-04-30";
    private String startStationCode = "TJP";
    private String arriveStationCode = "TTK";
    private String train_no = "110000K12808";
    private Integer maxRange = 20;// 最大查询前后20站
    private String Cookie = "WaHx3lxQpHgGV7Jsw0LVPFDBhQpivsg8baQCnYGQvM8G6w5ahtLoezgy2kUcSf1oHQAX2viVJMR26CW30yl3tiu1eO50Uu1gtXkxXoW9199Dtry27u4KFgY7yJ8hRM5SK_RgRz_ws71LR8IY5Tu5nfOupmEc9e26";
    private String allCookie = "tk=WhwI9v59EQs6F09BLiaONaTXRdoQ9Ah3tXu4K4jaRLkubm1m0; JSESSIONID=F99999247F96C00093CC9F75A04CEC24;" +
            " BIGipServerotn=703595018.38945.0000; BIGipServerpool_passport=300745226.50215.0000; RAIL_EXPIRATION=1587587122006;" +
            " RAIL_DEVICEID=WaHx3lxQpHgGV7Jsw0LVPFDBhQpivsg8baQCnYGQvM8G6w5ahtLoezgy2kUcSf1oHQAX2viVJMR26CW30yl3tiu1eO50Uu1gtXkxXo" +
            "W9199Dtry27u4KFgY7yJ8hRM5SK_RgRz_ws71LR8IY5Tu5nfOupmEc9e26; route=c5c62a339e7744272a54643b3be5bf64; " +
            "_jc_save_fromDate=2020-05-01; _jc_save_wfdc_flag=dc; _jc_save_fromStation=%u957F%u6625%2CCCT; " +
            "_jc_save_toStation=%u897F%u5B89%2CXAY; _jc_save_toDate=2020-04-19";

    private static List<Station> stations = new ArrayList<>();
    static {
        initNameCode();
    }
    //创建 SingleObject 的一个对象
    private static ReadStationsServiceImpl instance = new ReadStationsServiceImpl();

    //让构造函数为 private，这样该类就不会被实例化
    private ReadStationsServiceImpl(){}

    //获取唯一可用的对象
    public static ReadStationsServiceImpl getInstance(){
        return instance==null?new ReadStationsServiceImpl():instance;
    }
    // TODO 账号登陆获取cookie

    // TODO 将stations_names存储到缓存中


    public static void main(String[] args) {
        //获取唯一可用的对象
        ReadStationsServiceImpl readStationsService = ReadStationsServiceImpl.getInstance();
        log.info(String.valueOf(readStationsService.readStations(readStationsService.train_date,readStationsService.startStationCode,
                readStationsService.arriveStationCode, readStationsService.prefer)));
    }


    public List<TicketInfo> readStations(String train_date, String startStationName, String arriveStationName, String prefer){
        String startStationCode = this.startStationCode, arriveStationCode = this.arriveStationCode;
        Station from_station = null, to_station = null;
        List<TicketInfo> ticketInfoList = new LinkedList<>();
        if(StringUtil.isNotBlank(startStationName)){
            from_station = findStation(startStationName);
            if(from_station == null){
                from_station = findStation(this.startStationCode);
            }
        }
        if(StringUtil.isNotBlank(arriveStationName)){
            to_station = findStation(arriveStationName);
            if(to_station == null){
                to_station = findStation(this.arriveStationCode);
            }
        }
        if(StringUtil.isBlank(train_date) || from_station == null || to_station == null ){
            return null;
        }else {
            // 当前起始站，到达站车票情况
            List<TicketInfo> ticketInfos = queryLeftTicket(train_date, from_station, to_station, prefer);
            List<Station> stations = null;
            for (int i = 0; i < ticketInfos.size(); i++) {
                TicketInfo ticketInfo = ticketInfos.get(i);
                if (StringUtil.isNotBlank(prefer) && prefer.equalsIgnoreCase(ticketInfo.getLieChe())) {
                    stations = stationsOnThisLieChe(ticketInfo.getTrain_no(), startStationCode, arriveStationCode, train_date);
                    ticketInfoList.addAll(queryTicketInfoListByStations(stations));
                    break;
                } else {
                    stations = stationsOnThisLieChe(ticketInfo.getTrain_no(), startStationCode, arriveStationCode, train_date);
                    ticketInfoList.addAll(queryTicketInfoListByStations(stations));
                }
            }

        }
        return ticketInfoList;
    }

    private List<TicketInfo> queryTicketInfoListByStations(List<Station> stations){
        List<TicketInfo> ticketInfoList = new LinkedList<>();
//        String arrive_time;
//        String station_name;
//        String start_time;
//        String stopover_time;
//        String station_no;
//        Boolean isEnabled;
//        log.info( "当前线路所有站点名称及顺序号，到站和发站时间: " + stations);
        int startStationIndex = 0;
        int arriveStationIndex = 0;
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            String nameCode = findNameCode(station.getStation_name());
            station.setStation_code(nameCode);
            if(startStationCode.equalsIgnoreCase(nameCode)){
                startStationIndex = i;
            }else if(arriveStationCode.equalsIgnoreCase(nameCode)){
                arriveStationIndex = i;
            }
        }
        List<Station> preStationList = stations.subList(0, startStationIndex);
        List<Station> postStationList = stations.subList(arriveStationIndex, stations.size() - 1);
        for (int i = preStationList.size() - 1; i >= 0; i--) {
            for (int i1 = 0; i1 < postStationList.size(); i1++) {
                if((preStationList.size() - 1-i) < maxRange && i1 < maxRange){
                    Station preStation = preStationList.get(i);
                    Station postStation = postStationList.get(i1);
//                    log.info(preStation.toString(),postStation.toString());
                    ticketInfoList.addAll(queryLeftTicket(train_date, preStation, postStation, prefer));
                    // 设置自然停隔时间1.2s
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(),e);
                    }
                }
            }
        }
        return ticketInfoList;
    }

    /**
     * 根据trainNo查询当前线路所有站点
     * @param train_no
     * @param startStationCode
     * @param arriveStationCode
     * @param train_date
     * @return
     */
    private List<Station> stationsOnThisLieChe(String train_no, String startStationCode, String arriveStationCode, String train_date){
        // 查询当前线路所有站点
        String params = "train_no="+ train_no +"&from_station_telecode=" + startStationCode +"&to_station_telecode="+ arriveStationCode +"&depart_date="+train_date;
        String url = "https://kyfw.12306.cn/otn/czxx/queryByTrainNo?"+ params;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder().addHeader("Cookie",Cookie).get()
                .url(url)
                .build();
        OkHttpClient eagerClient = okHttpClient.newBuilder().readTimeout(50000, TimeUnit.MILLISECONDS).build();
        List<Station> stations = null;//需要转换的json数组
        try {
            Response response = eagerClient.newCall(request).execute();
            String string = response.body().string();
            ResponseInfo2 ticketInfo = JSONObject.parseObject(string, ResponseInfo2.class);
            Map data = ticketInfo.getData();
            stations = JSONObject.parseArray(data.get("data").toString(), Station.class);
//            log.info( "当前线路所有站点名称及顺序号，到站和发站时间: " + stations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stations;
    }

    private List<TicketInfo> queryLeftTicket(String train_date, Station from_station, Station to_station, String prefer){
        List<TicketInfo> ticketInfoList = new LinkedList<>();
        // 查询起始站所有线路 // 查询有无车票 https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=2020-05-01&leftTicketDTO.from_station=TJP&leftTicketDTO.to_station=TTK&purpose_codes=ADULT
        String url = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date="+train_date
                +"&leftTicketDTO.from_station="+from_station.getStation_code()
                +"&leftTicketDTO.to_station="+to_station.getStation_code()+"&purpose_codes=ADULT";
//        String checi = "110000K12808"; // k128
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder()
//                .addHeader("Accept","/")
//                .addHeader("Accept-Encoding","gzip, deflate, br")
//                .addHeader("Accept-Language","zh-CN,zh;q=0.9")
//                .addHeader("Cache-Control","no-cache")
//                .addHeader("X-Requested-With","XMLHttpRequest")
//                .addHeader("Referer","https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc")
//                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
                .addHeader("Cookie",Cookie)
                .get()
                .url(url)
                .build();
        OkHttpClient eagerClient = okHttpClient.newBuilder().readTimeout(50000, TimeUnit.MILLISECONDS).build();
        try {
            Response response = eagerClient.newCall(request).execute();
            boolean error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            // 如果请求失败，就停一段时间再试，设置最大尝试次数
            for (int i = 1; error && i <= 5; i++){
                log.error(url + "网络异常");
                try {
                    Thread.sleep(1200*i);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(),e);
                }
                response = eagerClient.newCall(request).execute();
                error = response.request().url().toString().equalsIgnoreCase("https://www.12306.cn/mormhweb/logFiles/error.html");
            }
            if(response.body()!=null) {
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
                        String[] strings = s.split("\\|");
                        // 是否只展示有车票，即可预订的车次与站点情况
                        boolean must;
                        if(StringUtil.isNotBlank(prefer)){
                            must = strings.length > 29 && strings[11].equalsIgnoreCase("Y") && strings[3].equalsIgnoreCase(prefer);
                        }else {
                            must = strings.length > 29 && strings[11].equalsIgnoreCase("Y");
                        }
                        if (must) {
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


    //    从本地文件读取 站名与站编码
    private static void initNameCode(){
        try {
            //获取唯一可用的对象
            ReadStationsServiceImpl readStationsService = ReadStationsServiceImpl.getInstance();
            String fileRead = fileRead(readStationsService.pathname);
            String[] stationsStr = fileRead.split("@");
            // 以@分割的第一组是空，所以下标从1开始
            for (int i = 1; i < stationsStr.length; i++) {
                String stationStr = stationsStr[i];
                String[] element = stationStr.split("\\|");
                // 2-stationCode  1-stationName  3-拼音 5-index
                Station station = new Station();
                station.setStation_code(element[2]);
                station.setStation_name(element[1]);
                stations.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 站名与站编码转换
     * @param nameCodeConvert
     * @return
     */
    private static String findNameCode(String nameCodeConvert){
        for (int i = 1; i < stations.size(); i++) {
            Station station = stations.get(i);
            if(station.getStation_name().equalsIgnoreCase(nameCodeConvert)){
                return station.getStation_code();
            }else if(station.getStation_code().equalsIgnoreCase(nameCodeConvert)){
                return station.getStation_name();
            }
        }
        return null;
    }
    /**
     * 站名与站编码寻找站
     * @param nameOrCode
     * @return
     */
    private static Station findStation(String nameOrCode){
        for (int i = 1; i < stations.size(); i++) {
            Station station = stations.get(i);
            if(station.getStation_name().equalsIgnoreCase(nameOrCode) || station.getStation_code().equalsIgnoreCase(nameOrCode)){
                return station;
            }
        }
        return null;
    }


    public static String fileRead(String pathname) throws Exception {
        File file = new File(pathname);//定义一个file对象，用来初始化FileReader
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        return sb.toString();
    }

    /**
     * 将一个字符串转化为输入流
     */
    public InputStream getStringStream(String sInputString){
        if (sInputString != null && !sInputString.trim().equals("")){
            try{
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    private void readStationsMethod (){
        // 查询起始站所有线路 // 查询有无车票 https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=2020-05-01&leftTicketDTO.from_station=TJP&leftTicketDTO.to_station=TTK&purpose_codes=ADULT
        String startStationCode = "TJP";
        String arriveStationCode = "TTK";


        // 从选择的线路起始站，成圈状扩大范围，组装成待查询的参数
        // 从天津到台前，往外扩大范围




        // 查询价格  /otn/leftTicket/queryTicketPrice?train_no=110000K12808&from_station_no=12&to_station_no=15&seat_types=3411&train_date=2020-05-01
//        train_no: 110000K12808
//        from_station_no: 12
//        to_station_no: 15
//        seat_types: 3411
//        train_date: 2020-05-01




    }
}
