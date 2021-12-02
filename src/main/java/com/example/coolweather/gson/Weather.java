package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;
//返回数据的具体内容包括五个部分，抽象成五个类

public class Weather {
    public String status;    //返回状态
    public Basic basic;      //基本信息
    public AQI aqi;         //空气质量
    public Now now;      //现在的气温等信息
    public Suggestion suggestion;    //生活建议

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
