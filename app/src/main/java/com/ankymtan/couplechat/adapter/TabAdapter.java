package com.ankymtan.couplechat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.ankymtan.couplechat.fragment.FragmentPlugin;
import com.ankymtan.couplechat.fragment.FragmentAccount;
import com.ankymtan.couplechat.fragment.FragmentSetting;

/**
 * Created by An on 10/6/2015.
 */
public class TabAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    String[] pageTitles = {"Account","Plugin","Setting"};

    public TabAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentAccount();
            case 1:
                return new FragmentPlugin();
            case 2:
                return new FragmentSetting();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3; // Number of tabs
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

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }
}
