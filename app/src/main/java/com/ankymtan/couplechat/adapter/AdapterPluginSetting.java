package com.ankymtan.couplechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

import java.util.ArrayList;

/**
 * Created by ankym on 16/8/2015.
 */
public class AdapterPluginSetting extends ArrayAdapter{
    Context context;

    public AdapterPluginSetting(Context context){
        super(context, R.layout.setting_row_style_1);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.setting_row_style_1, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imIcon = (ImageView) convertView.findViewById(R.id.iv_setting_icon);
            viewHolder.tvSettingName = (TextView) convertView.findViewById(R.id.tv_setting_name);
            viewHolder.tvSettingDetail = (TextView) convertView.findViewById(R.id.tv_setting_detail);
            viewHolder.settingSwitch = (Switch) convertView.findViewById(R.id.switch_setting);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (position){
            case 0:
                viewHolder.setIcon(R.drawable.power);
                viewHolder.setTvSettingName("Power");
                viewHolder.setTvSettingDetail("Turn on/off the plugin");
                viewHolder.showSwitch();
                break;

            case 1:
                viewHolder.setIcon(R.drawable.add);
                viewHolder.setTvSettingName("Choose affected friends");
                viewHolder.setTvSettingDetail("Pick friends in your friend list to be applied this plugin");
                viewHolder.hideSwitch();
                break;

            case 2:
                viewHolder.setIcon(R.drawable.agent);
                viewHolder.setTvSettingName("Choose Plugin");
                viewHolder.setTvSettingDetail("Choose Plugin in the list");
                viewHolder.hideSwitch();
                break;

            case 3:
                viewHolder.setIcon(R.drawable.hello);
                viewHolder.setTvSettingName("Say hello");
                viewHolder.setTvSettingDetail("Say hello every period of time");
                viewHolder.showSwitch();
                break;

            case 4:
                viewHolder.setIcon(R.drawable.birthday);
                viewHolder.setTvSettingName("Happy birthday");
                viewHolder.setTvSettingDetail("Remind on friends' birthdays");
                viewHolder.showSwitch();
                break;

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return 5;
    }

    private class ViewHolder {
        ImageView imIcon;
        TextView tvSettingName, tvSettingDetail;
        Switch settingSwitch;
        public void setIcon(int iconId){
            imIcon.setImageResource(iconId);
        }

        public void setTvSettingName(String settingName){
            tvSettingName.setText(settingName);
        }

        public void setTvSettingDetail(String settingDetail){
            tvSettingDetail.setText(settingDetail);
        }

        public void hideSwitch(){
            settingSwitch.setVisibility(View.INVISIBLE);
        }

        public void showSwitch(){
            settingSwitch.setVisibility(View.VISIBLE);
        }
    }
}
