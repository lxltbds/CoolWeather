package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.coolweather.db.County;
import com.example.coolweather.db.Focus;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class FocusActivity extends AppCompatActivity {

    private List<Focus> focusList = new ArrayList<>();
    private List<County> weatherIdList = new ArrayList<>();
    private Button search;
    private EditText search_text;
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        //初始化数据
        initFocus();
        FocusAdapter adapter = new FocusAdapter(FocusActivity.this,R.layout.focus_item,focusList);
        ListView listView = (ListView) findViewById(R.id.focus_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Focus focus = focusList.get(position);
                String weather_ID = focus.getWeatherId();
                Intent intent = new Intent(FocusActivity.this,WeatherActivity.class);
                intent.putExtra("weather_id",weather_ID);
               // Log.d("FocusActivity","weather_ID : "+weather_ID);
                startActivity(intent);
                FocusActivity.this.finish();

            }
        });
        search_text = (EditText)findViewById(R.id.city_select);
        search = (Button) findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = search_text.getText().toString();
                weatherIdList = DataSupport.findAll(County.class);
                for(County c:weatherIdList) {
                    if(text.equals(c.getWeatherId())) {
                        flag = 1;
                    }
                }
                if(flag == 1) {
                    Intent intent = new Intent(FocusActivity.this,WeatherActivity.class);
                    intent.putExtra("weather_id",text);
                   // Log.d("FocusActivity","text: "+text);
                    startActivity(intent);
                    FocusActivity.this.finish();
                } else {
                    Toast.makeText(FocusActivity.this, "城市不存在", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //将Focus表中数据全部获取
    private void initFocus() {
        focusList = DataSupport.findAll(Focus.class);
    }
}