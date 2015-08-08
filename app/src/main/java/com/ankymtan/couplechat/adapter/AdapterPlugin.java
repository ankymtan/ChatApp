package com.ankymtan.couplechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

import java.util.ArrayList;

/**
 * Created by ankym on 1/8/2015.
 */
public class AdapterPlugin extends ArrayAdapter<String> {

    ArrayList<String> pluginNameList = new ArrayList<>();
    Context context;

    public AdapterPlugin(ArrayList<String> pluginNameList, Context context){
        super(context, R.layout.item_plugin_info);
        this.pluginNameList = pluginNameList;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_plugin_info, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvPluginName = (TextView) convertView.findViewById(R.id.tv_plugin_name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvPluginName.setText(getItem(position));

        return convertView;
    }

    @Override
    public String getItem(int position) {
        return pluginNameList.get(position);
    }

    @Override
    public int getCount() {
        return pluginNameList.size();
    }

    private class ViewHolder{
        TextView tvPluginName;
    }

}
