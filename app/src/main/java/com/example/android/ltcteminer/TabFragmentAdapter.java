package com.example.android.ltcteminer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Tal on 28/07/2017.
 */

public class TabFragmentAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 4;
    private Context mContext;
    private String tabTitles[];

    public TabFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        tabTitles = new String[] { context.getResources().getString(R.string.settings_tab_title) , context.getResources().getString(R.string.rates_tab_title) ,
                context.getResources().getString(R.string.mine_tab_title) ,context.getResources().getString(R.string.about_tab_title) };
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return SettingsFragment.newInstance(position+1);
        }
        else if (position == 1) {
            return RatesFragment.newInstance(position+1);
        }
        else if (position == 2) {
            return MineFragment.newInstance(position+1);
        }
        else {
            return AboutFragment.newInstance(position+1);
        }
            //        switch (position) {
//            case 0:
//                return new SettingsFragment();
//            case 1:
//                return new MineFragment();
//            case 2:
//                return new RatesFragment();
//            case 3:
//                return new AboutFragment();
//            default:
//                return new SettingsFragment();
//        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
