package com.example.musicalplayer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentPageAdapter extends FragmentPagerAdapter {
    private int tabCount;

    public FragmentPageAdapter(@NonNull FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentCurrentSong();
            case 1:
                return new FragmentAllMusic();
            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
