package app.coolweather.com.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import app.coolweather.com.coolweather.R;
import app.coolweather.com.coolweather.model.City;
import app.coolweather.com.coolweather.model.CoolWeatherDB;
import app.coolweather.com.coolweather.model.County;
import app.coolweather.com.coolweather.model.Province;
import app.coolweather.com.coolweather.util.HttpCallbackListener;
import app.coolweather.com.coolweather.util.HttpUtil;
import app.coolweather.com.coolweather.util.Utility;

/**
 * Created by Administrator on 2016/10/21.
 */

public class ChooseAreaActivity extends Activity {

    public static final int LEVERL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> datalist=new ArrayList<String>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;

    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        coolWeatherDB=CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentLevel==LEVERL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();

                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
            }
        });
        queryProvinces();
    }



    private void queryProvinces() {
        provinceList=coolWeatherDB.loadProvince();
        if (provinceList.size()>0){
            datalist.clear();
            for (Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel=LEVERL_PROVINCE;
        }else {
            queryFromServer(null,null,"province");
        }
    }

    private void queryCities() {
        cityList=coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size()>0){
            datalist.clear();
            for (City city:cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),null,"city");
        }
    }
    private void queryCounties() {
        countyList=coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0){
            datalist.clear();
            for (County county:countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),selectedCity.getCityCode(),"county");
        }
    }


    private void queryFromServer(final String Province_code,final String City_code, final String type) {
        String address = null;
        if (!TextUtils.isEmpty(Province_code)){
            if("city".equals(type)){
                address="http://www.weather.com.cn/data/city3jdata/provshi/"+Province_code+".html";
            }else if ("county".equals(type)){
                address="http://www.weather.com.cn/data/city3jdata/station/"+Province_code+City_code+".html";
            }

        }else {
            address="http://www.weather.com.cn/data/city3jdata/china.html";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvincesResponse(coolWeatherDB,response);

                }else if ("city".equals(type)){
                    result=Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载.....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel==LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }
}
