package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import net.somethingdreadful.MAL.IGF;
import net.somethingdreadful.MAL.api.MALApi;

public class IGFPagerAdapter extends FragmentPagerAdapter {
    FragmentManager fm;

    public IGFPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int i) {
        IGF fragment = new IGF();
        fragment.listType = i == 0 ? MALApi.ListType.ANIME : MALApi.ListType.MANGA;
        return fragment;
    }

    public Fragment getIGF(ViewPager viewPager, int position) {
        return fm.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MALApi.getListTypeString(getTag(position)).toUpperCase();
    }

    public MALApi.ListType getTag(int position) {
        switch (position) {
            case 0:
                return MALApi.ListType.ANIME;
            case 1:
                return MALApi.ListType.MANGA;
            default:
                return null;
        }
    }
}