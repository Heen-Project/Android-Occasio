package edu.bluejack151.occasio.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.bluejack151.occasio.Class.TimelineData;

public class MyGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<TimelineData> browseDataList;
    private int pos;

    public MyGridViewAdapter(Context context, List<TimelineData> imageDataLists) {
        if (imageDataLists.size() > 0) {
            Collections.sort(imageDataLists, new Comparator<TimelineData>() {
                @Override
                public int compare(final TimelineData object1, final TimelineData object2) {
                    return object2.getImageData().getTimestamp().compareTo(object1.getImageData().getTimestamp());
                }
            });
        }
        this.context = context;
        this.browseDataList = imageDataLists;
    }

    @Override
    public int getCount() {
        return browseDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        pos = position;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(width / 3, width / 3));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(convertToBitmap(browseDataList.get(position).getImageData().getImageString()));
        return imageView;
    }

    private Bitmap convertToBitmap(String picString) {
        byte[] decodedString = Base64.decode(picString, Base64.DEFAULT);
        Bitmap bitmapHomePic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bitmapHomePic;
    }

}
