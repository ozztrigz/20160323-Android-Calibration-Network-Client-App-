package ca.on.tradeport.calibrationnetwork;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


public class PagerActivity extends FragmentStatePagerAdapter {

    int mNomOfTabs;

    public PagerActivity(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNomOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {


        switch(position) {
            case 0 :
                TasksFragment tasks = new TasksFragment();
                return tasks;
            case 1 :
                JobsFragment jobs = new JobsFragment();
                return jobs;
            case 2 :
                InstrumentFragment instruments = new InstrumentFragment();
                return instruments;
            case 3 :
                SettingsFragment settings = new SettingsFragment();
                return settings;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return mNomOfTabs;
    }
}
