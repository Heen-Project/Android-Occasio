package edu.bluejack151.occasio.Application;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Domus on 12/23/2015.
 */
public class AppFirebase extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
