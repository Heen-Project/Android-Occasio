package edu.bluejack151.occasio.Fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import edu.bluejack151.occasio.Adapter.MyFragmentPagerAdapter;
import edu.bluejack151.occasio.R;

public class FragmentPageMaster extends Fragment {

    View v;
    TabLayout tabLayout;
    ViewPager viewPager;
    int[] tabIcons = {R.drawable.ic_home_white, R.drawable.ic_browse_white, R.drawable.ic_camera_white};

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.tab_view_pager, container, false);

        initViewPager();
        initTabIcons();

        return v;
    }

    private void initViewPager() {
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new FragmentPageHome());
        fragmentList.add(new FragmentPageBrowse());
        fragmentList.add(new FragmentPageCamera());

        MyFragmentPagerAdapter myFragmentPagerAdapter = new MyFragmentPagerAdapter(
                getChildFragmentManager(), fragmentList);

        viewPager.setAdapter(myFragmentPagerAdapter);
    }

    private void initTabIcons() {
        tabLayout = (TabLayout) v.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);

    }

}
