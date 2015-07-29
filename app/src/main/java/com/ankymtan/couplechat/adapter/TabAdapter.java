package com.ankymtan.couplechat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.ankymtan.couplechat.activity.ActivityChat;
import com.ankymtan.couplechat.fragment.FragmentPlugin;
import com.ankymtan.couplechat.fragment.FragmentSetting;

/**
 * Created by An on 10/6/2015.
 */
public class TabAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public TabAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentSetting();
            case 1:
                return new FragmentPlugin();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2; // Number of tabs
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //registeredFragments.remove(position);
        //super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position){
        return registeredFragments.get(position);
    }
}
