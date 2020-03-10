package com.xu.xmaster.beans;

import java.io.Serializable;

import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.HourlyBase;

public class HourlyBean extends HourlyBase implements Serializable {

    public HourlyBean() {
    }

    public HourlyBean(HourlyBase hourlyBase) {
        setTime(hourlyBase.getTime());
        setTmp(hourlyBase.getTmp());
        setCond_code(hourlyBase.getCond_code());
        setCond_txt(hourlyBase.getCond_txt());
        setHum(hourlyBase.getHum());//相对湿度
        setDew(hourlyBase.getDew());//露点温度
        setPop(hourlyBase.getPop());//降水概率
        setPres(hourlyBase.getPres());//大气压强
        setWind_dir(hourlyBase.getWind_dir());//风向
        setWind_deg(hourlyBase.getWind_deg());//风向360角度
        setWind_sc(hourlyBase.getWind_sc());//风力
        setWind_spd(hourlyBase.getWind_spd());//风速，公里/小时
    }

}
