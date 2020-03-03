package edu.bluejack151.occasio.Fragment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Random;

import edu.bluejack151.occasio.Activity.CameraActivity;
import edu.bluejack151.occasio.R;

public class FragmentPageCamera extends Fragment implements View.OnClickListener {

    ImageView imageView;
    ImageButton btnCapture;
    View v;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_page_camera, container, false);
        btnCapture = (ImageButton) v.findViewById(R.id.btnCapture_fCamera);
        imageView = (ImageView) v.findViewById(R.id.header);
        imageView.setImageResource(randomImage());
        btnCapture.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCapture_fCamera:
                Intent intent = new Intent(getActivity().getApplicationContext(), CameraActivity.class);
                startActivity(intent);
                break;
        }
    }

    public int randomImage() {
        TypedArray images = getResources().obtainTypedArray(R.array.array_images);
        Random random = new Random();
        int rnd = random.nextInt(images.length());
        int resId = images.getResourceId(rnd, 0);
        return resId;
    }
}
