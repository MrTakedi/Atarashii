package net.somethingdreadful.MAL.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.profile.ProfileDetailsAL;
import net.somethingdreadful.MAL.profile.ProfileDetailsMAL;
import net.somethingdreadful.MAL.profile.ProfileFriends;

public class ProfilePagerAdapter extends FragmentPagerAdapter {
    ProfileActivity activity;

    public ProfilePagerAdapter(FragmentManager fm, ProfileActivity activity) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (AccountService.isMAL())
                    return new ProfileDetailsMAL();
                else
                    return new ProfileDetailsAL();
            case 1:
                return new ProfileFriends();
            default:
                return new ProfileDetailsMAL();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return activity.getString(R.string.tab_name_details);
            case 1:
                return AccountService.isMAL() ? activity.getString(R.string.tab_name_friends) : activity.getString(R.string.tab_name_following);
            default:
                return null;
        }
    }
}