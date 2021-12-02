package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Focus;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
//显示天气信息的基本控件
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
//每日一图
    private ImageView bingPicImg;

//刷新
    public Button refresh;
    public SwipeRefreshLayout swipeRefresh;
    public String mWeatherId;
//切换城市菜单
    public DrawerLayout drawerLayout;
    private Button navButton;
//关注
    private Button focus;
    private List<Focus> focusList;
    private List<County> countyList;
//查找
    private Button check;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将背景图和状态栏融合在一起
        //这个功能只有版本号大于等于21才能使用
        if(Build.VERSION.SDK_INT >= 21) {
            //获取当前活动的DecorView
            View decorView = getWindow().getDecorView();
            //改变UI显示，下面两个参数表示活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            //将状态栏设置为透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        setContentView(R.layout.activity_weather);

        //初始化各显示信息控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
    //每日一图
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
    //下拉刷新
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary);

    //关注
        focus = (Button)findViewById(R.id.focus);
    //查找城市和显示关注城市的界面
        check = (Button)findViewById(R.id.check_button);
    //切换城市菜单
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开滑动菜单,也可通过滑动左侧来打开菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    //读取数据库中的天气信息
        SharedPreferences prefs = getSharedPreferences(String.valueOf(this),MODE_PRIVATE);
        String weatherString = prefs.getString("weather",null);
    //每日一图
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        if(weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气*/
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        //下拉刷新的监听器,一旦下拉直接去请求天气信息
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        //关注
        focus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //查找关注表中是否有此城市的信息
                focusList = DataSupport.where("weatherId = ?",mWeatherId).find(Focus.class);
                //没有则添加，有则删除
                if(focusList.isEmpty()) {
                    Focus focus = new Focus();
                    focus.setWeatherId(mWeatherId);
                    Log.d("WeatherActivity","focus.weatherId:"+focus.getWeatherId());
                    //初始化县表数据
                    countyList = DataSupport.findAll(County.class);
                    for(County c :countyList) {
                        if(c.getWeatherId().equals(focus.getWeatherId())) {
                            focus.setAddress(c.getCountyName());
                            Log.d("WeatherActivity","focus.addressName:"+focus.getAddress());
                            break;
                        }
                    }
                    focus.save();
                    Toast.makeText(WeatherActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                } else {
                    DataSupport.deleteAll(Focus.class,"weatherId = ?",mWeatherId);
                    Toast.makeText(WeatherActivity.this, "取消关注", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //显示关注和可以查找的活动界面
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this,FocusActivity.class);
                startActivity(intent);
                WeatherActivity.this.finish();
            }
        });

        refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestWeather(mWeatherId);
                Toast.makeText(WeatherActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //根据天气id请求城市天气信息
    public void requestWeather(String weatherId) {

        //Log.d("WeatherActivity","mWeather : "+mWeatherId);
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        mWeatherId = weatherId;
        //Log.d("WeatherActivity","WEATHERyy = "+weatherId);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        //表示刷新事件结束
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //返回的数据
                final String responseText = response.body().string();
                //解析数据
                final Weather weather = Utility.handleWeatherResponse(responseText);
                //开启子进程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //判断是否成功获取到天气信息
                        if(weather != null && "ok".equals(weather.status)) {
                            //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            //存到数据库
                            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(this),MODE_PRIVATE).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        //表示刷新事件结束
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

        });
        //加载每日一图
        loadBingPic();
    }

    //下载必应每日一图
    private void loadBingPic() {
        //获得图片链接
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //将图片链接存入数据库
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                //开启子进程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //用Glide加载图片
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }

    //处理Weather实体类中的数据,输出在屏幕上
    private void showWeatherInfo(Weather weather) {
        //获取天气信息
        String cityName =weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;

        //输出在对应的控件中
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        //未来几天的天气情况
        for(Forecast forecast:weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        //如果空气信息不为空，显示
        if(weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        //显示生活建议
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        //激活服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

    }

}