package app.coolweather.com.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import app.coolweather.com.coolweather.model.City;
import app.coolweather.com.coolweather.model.CoolWeatherDB;
import app.coolweather.com.coolweather.model.County;
import app.coolweather.com.coolweather.model.Province;

/**
 * Created by Administrator on 2016/10/21.
 */

public class Utility {

    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
        if (!TextUtils.isEmpty(response)){

            String[] allProvinces=response.split(",");
            if (allProvinces!=null&&allProvinces.length>0){
                for (String p:allProvinces){
                    String[] array=p.split(":");
                    Province province=new Province();
                    province.setProvinceCode(array[0].replace("\"","").replace("{",""));
                    province.setProvinceName(array[1].replace("\"","").replace("}",""));
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }

        }
        return false;

    }

    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if (!TextUtils.isEmpty(response)){

            String[] allCities=response.split(",");
            if (allCities!=null&&allCities.length>0){
                for (String c:allCities){
                    String[] array=c.split(":");
                    City city=new City();
                    city.setCityCode(array[0].replace("\"","").replace("{",""));
                    city.setCityName(array[1].replace("\"","").replace("}",""));
                    city.setProvindeId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if (!TextUtils.isEmpty(response)){

            String[] allCounties=response.split(",");
            if (allCounties!=null&&allCounties.length>0){
                for (String c:allCounties){
                    String[] array=c.split(":");
                    County county=new County();
                    county.setCountyCode(array[0].replace("\"","").replace("{",""));
                    county.setCountyName(array[1].replace("\"","").replace("}",""));
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }

        }
        return  false;
    }

}
