package com.xu.xmaster.beans;

import java.io.Serializable;

import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;

/**
 * 序列化ForecastBase
 */
public class ForecastBean extends ForecastBase implements Serializable {

    public ForecastBean() {
    }

    public ForecastBean(ForecastBase forecastBase) {
        setDate(forecastBase.getDate());
        setCond_code_d(forecastBase.getCond_code_d());//白天天气状况代码
        setCond_code_n(forecastBase.getCond_code_n());//夜间天气状况代码
        setCond_txt_d(forecastBase.getCond_txt_d());//白天天气状况描述
        setCond_txt_n(forecastBase.getCond_txt_n());//晚间天气状况描述
        setTmp_max(forecastBase.getTmp_max());//最高温度
        setTmp_min(forecastBase.getTmp_min());//最低温度
        setHum(forecastBase.getHum());//相对湿度
        setPcpn(forecastBase.getPcpn());//降水量
        setPop(forecastBase.getPop());//降水概率
        setPres(forecastBase.getPres());//大气压强
        setWind_dir(forecastBase.getWind_dir());//风向
        setWind_deg(forecastBase.getWind_deg());//风向360角度
        setWind_sc(forecastBase.getWind_sc());//风力
        setWind_spd(forecastBase.getWind_spd());//风速，公里/小时
    }
}
