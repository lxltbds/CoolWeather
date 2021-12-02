package com.example.coolweather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.coolweather.db.Focus;

import java.util.List;


public class FocusAdapter extends ArrayAdapter<Focus> {
    private int resourceId;

    public FocusAdapter(Context context, int textViewResourceId, List<Focus> objects) {
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取当前项的实例
        Focus focus = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView address = (TextView) view.findViewById(R.id.address);
        TextView weatherId = (TextView) view.findViewById(R.id.weatherId);
        address.setText(focus.getAddress());
        weatherId.setText(focus.getWeatherId());
        return view;
    }
}
