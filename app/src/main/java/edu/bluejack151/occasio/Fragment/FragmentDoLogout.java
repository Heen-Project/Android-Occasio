package edu.bluejack151.occasio.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;

import edu.bluejack151.occasio.Activity.LoginActivity;
import edu.bluejack151.occasio.Class.CurrentMasterData;
import edu.bluejack151.occasio.R;

public class FragmentDoLogout extends Fragment {

    private Firebase myFirebaseRef;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrentMasterData.getInstance().fnLogout_clearData();
        sharedPreferences = this.getActivity().getSharedPreferences(String.valueOf(R.string.PREFERENCES_KEY_USER), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
        LoginManager.getInstance().logOut();
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        myFirebaseRef.unauth();
        Intent intent = new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
