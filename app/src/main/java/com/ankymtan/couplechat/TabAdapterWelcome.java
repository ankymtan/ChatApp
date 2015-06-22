package com.ankymtan.couplechat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * Created by An on 20/6/2015.
 */
public class TabAdapterWelcome extends FragmentPagerAdapter{
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public TabAdapterWelcome(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FragmentWelcome();
            case 1:
                return new FragmentLogin();
            case 2:
                return new FragmentRegister();
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
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position){
        return registeredFragments.get(position);
    }
}
