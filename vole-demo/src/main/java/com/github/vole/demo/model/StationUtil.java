package com.github.vole.demo.model;

import com.github.vole.demo.service.impl.ReadStationsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class StationUtil {

    public static String pathname = "D:/12306/station_names.txt";
    //    public static String pathname = "/home/vole/static/12306/station_names.txt";

    private Integer maxRange = 20;// 最大查询前后20站


    private static List<Station> stations = new ArrayList<>();

    static {
        initNameCode();
    }




    // 某车次可能的起始站
    public List<Train> queryTicketInfoListByStations(Train originalTrain, List<Station> stations, Station from_station, Station to_station) {
        List<Train> trainList = new LinkedList<>();
        if(stations != null){
            trainList.add(originalTrain);
            int startStationIndex = 0;
            int arriveStationIndex = 0;
            for (int i = 0; stations !=null && i < stations.size(); i++) {
                Station station = stations.get(i);
                String stationCode = findNameCode(station.getStation_name());
                station.setStation_code(stationCode);
                if (from_station.getStation_code().equalsIgnoreCase(stationCode)) {
                    startStationIndex = i;
                } else if (to_station.getStation_code().equalsIgnoreCase(stationCode)) {
                    arriveStationIndex = i;
                }
            }
            List<Station> preStationList = stations.subList(0, startStationIndex);
            List<Station> postStationList = stations.subList(arriveStationIndex, stations.size() - 1);
            for (int i = preStationList.size() - 1; i >= 0; i--) {
                for (int i1 = 0; i1 < postStationList.size(); i1++) {
                    if ((preStationList.size() - 1 - i) < maxRange && i1 < maxRange) {
                        Train train = new Train();
                        train.setTrainName(originalTrain.getTrainName());
                        train.setTrainNo(originalTrain.getTrainNo());
                        train.setFrom_station(preStationList.get(i));
                        train.setTo_station(postStationList.get(i));
                        trainList.add(train);
                    }
                }
            }

        }
        return trainList;
    }



    //    从本地文件读取 站名与站编码
    private static void initNameCode() {
        try {
            String fileRead = fileRead(pathname);
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
     *
     * @param nameCodeConvert
     * @return
     */
    public String findNameCode(String nameCodeConvert) {
        for (int i = 1; i < stations.size(); i++) {
            Station station = stations.get(i);
            if (station.getStation_name().equalsIgnoreCase(nameCodeConvert)) {
                return station.getStation_code();
            } else if (station.getStation_code().equalsIgnoreCase(nameCodeConvert)) {
                return station.getStation_name();
            }
        }
        return null;
    }

    /**
     * 站名与站编码寻找站
     *
     * @param nameOrCode
     * @return
     */
    public Station findStation(String nameOrCode) {
        for (int i = 1; i < stations.size(); i++) {
            Station station = stations.get(i);
            if (station.getStation_name().equalsIgnoreCase(nameOrCode) || station.getStation_code().equalsIgnoreCase(nameOrCode)) {
                return station;
            }
        }
        return null;
    }


    private static String fileRead(String pathname) throws Exception {
        File file = new File(pathname);//定义一个file对象，用来初始化InputStreamReader
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader bReader = new BufferedReader(inputStreamReader);//new一个BufferedReader对象，将文件内容读取到缓存
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
    private InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private void readStationsMethod() {
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
