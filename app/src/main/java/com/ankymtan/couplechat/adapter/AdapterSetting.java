package com.ankymtan.couplechat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ankymtan.couplechat.framework.UserLocal;
import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by ankym on 16/8/2015.
 */
public class AdapterSetting extends RecyclerView.Adapter<AdapterSetting.ViewHolder>{

    UserLocal userLocal;

    public AdapterSetting(Context context){
        userLocal = new UserLocal(context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (position){
            case 0:
                holder.setIcon(R.drawable.background);
                holder.setTvSettingName("Enable Background");
                holder.setTvSettingDetail("Enable/Disable wallpaper and animation");
                holder.settingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        userLocal.setEnableBackground(isChecked);
                    }
                });
                break;
            case 1:
                holder.setIcon(R.drawable.animation);
                holder.setTvSettingName("Enable Animation");
                holder.setTvSettingDetail("Enable/Disable animation");
                holder.settingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        userLocal.setEnableAnimation(isChecked);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_row_style_1, parent, false);
        return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imIcon;
        TextView tvSettingName, tvSettingDetail;
        Switch settingSwitch;

        public ViewHolder(View itemView){
            super(itemView);
            imIcon = (ImageView) itemView.findViewById(R.id.iv_setting_icon);
            tvSettingName = (TextView) itemView.findViewById(R.id.tv_setting_name);
            tvSettingDetail = (TextView) itemView.findViewById(R.id.tv_setting_detail);
            settingSwitch = (Switch) itemView.findViewById(R.id.switch_setting);
        }

        public void setIcon(int iconId){
            imIcon.setImageResource(iconId);
        }

        public void setTvSettingName(String settingName){
            tvSettingName.setText(settingName);
        }

        public void setTvSettingDetail(String settingDetail){
            tvSettingDetail.setText(settingDetail);
        }
    }
}
