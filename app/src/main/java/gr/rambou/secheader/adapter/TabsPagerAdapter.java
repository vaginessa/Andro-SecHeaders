package gr.rambou.secheader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import gr.rambou.secheader.tabs.AboutFragment;
import gr.rambou.secheader.tabs.ScannerFragment;
import gr.rambou.secheader.tabs.StatsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Scanner fragment activity
                return new ScannerFragment();
            case 1:
                // Statistics fragment activity
                return new StatsFragment();
            case 2:
                // About fragment activity
                return new AboutFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}
