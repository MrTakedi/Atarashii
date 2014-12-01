package net.somethingdreadful.MAL;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import net.somethingdreadful.MAL.account.AccountService;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    PrefManager Prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        context = getActivity().getApplicationContext();
        Prefs = new PrefManager(context);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String Auth = AccountService.getAuth();
        Bundle bundle = new Bundle();
        int interval = Prefs.getSyncTime() * 60;
        if (key.equals("synchronisation_time")) {
            ContentResolver.removePeriodicSync(AccountService.getAccount(context), Auth, bundle);
            ContentResolver.addPeriodicSync(AccountService.getAccount(context), Auth, bundle, interval);
        } else if (key.equals("synchronisation")) {
            if (Prefs.getSyncEnabled()) {
                ContentResolver.setSyncAutomatically(AccountService.getAccount(context), Auth, true);
                ContentResolver.addPeriodicSync(AccountService.getAccount(context), Auth, bundle, interval);
            } else {
                ContentResolver.removePeriodicSync(AccountService.getAccount(context), Auth, bundle);
                ContentResolver.setSyncAutomatically(AccountService.getAccount(context), Auth, false);
            }
        } else if (key.equals("locale")) {
            sharedPreferences.edit().commit();
            Intent Home = new Intent(context, Home.class);
            startActivity(Home);
            System.exit(0);
        }
    }
}