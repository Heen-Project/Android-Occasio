package edu.bluejack151.occasio.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.bluejack151.occasio.Model.NavItem;
import edu.bluejack151.occasio.R;

public class MyNavListAdapter extends ArrayAdapter<NavItem> {

    Context context;
    int resLayout;
    List<NavItem> navItemList;

    public MyNavListAdapter(Context context, int resLayout, List<NavItem> navItemList) {
        super(context, resLayout, navItemList);

        this.context = context;
        this.resLayout = resLayout;
        this.navItemList = navItemList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = View.inflate(context, resLayout, null);

        TextView textView = (TextView) v.findViewById(R.id.nav_title);
        ImageView imageView = (ImageView) v.findViewById(R.id.nav_icon);

        NavItem navItem = navItemList.get(position);

        textView.setText(navItem.getTitle());
        imageView.setImageResource(navItem.getResIcon());

        return v;
    }
}
