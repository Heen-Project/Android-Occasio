package edu.bluejack151.occasio.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.bluejack151.occasio.Adapter.MyNavListAdapter;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.Fragment.FragmentDoLogout;
import edu.bluejack151.occasio.Fragment.FragmentPageMaster;
import edu.bluejack151.occasio.Fragment.FragmentPageProfile;
import edu.bluejack151.occasio.Model.NavItem;
import edu.bluejack151.occasio.R;

public class MasterActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RelativeLayout drawerPane;
    private ListView listView;

    private List<NavItem> listNavItems;
    private List<Fragment> listFragments;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;

    private Firebase myFirebaseUserRef;
    private Firebase myFirebaseRef;
    private AuthData authData;
    private UserData userData;
    private SharedPreferences sharedPreferences;
    private final String DEFAULT_VALUE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        sharedPreferences = getSharedPreferences(String.valueOf(R.string.PREFERENCES_KEY_USER), Context.MODE_PRIVATE);
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        myFirebaseUserRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/users");
        authData = myFirebaseUserRef.getAuth();
        initUserData();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("Occasio");
        setSupportActionBar(toolbar);

        initNavDrawer();

    }

    private void initUserData() {
        if (myFirebaseRef.getAuth() != null) {
            userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
            TextView txtUsername = (TextView) findViewById(R.id.profile_username);
            TextView txtEmail = (TextView) findViewById(R.id.profile_email);
            ImageView profilePic = (ImageView) findViewById(R.id.profile_picture);
            String encodedImage = userData.getImageString();
            Bitmap bitmapProfilePic = null;

            if (authData.getProvider().equals("password")) {
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                bitmapProfilePic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            } else {
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    URL url = new URL(userData.getProfileImageURL());
                    bitmapProfilePic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            profilePic.setImageBitmap(bitmapProfilePic);
            txtUsername.setText(userData.getUsername());
            txtEmail.setText(userData.getEmail());
        } else {
            startActivity(new Intent(MasterActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

    private void initNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerPane = (RelativeLayout) findViewById(R.id.drawer_pane);
        listView = (ListView) findViewById(R.id.nav_list);

        listNavItems = new ArrayList<>();
        listNavItems.add(new NavItem("Occasio", R.drawable.ic_home));
        listNavItems.add(new NavItem("Profile", R.drawable.ic_user));
        listNavItems.add(new NavItem("Logout", R.drawable.ic_exit));

        MyNavListAdapter myNavListAdapter = new MyNavListAdapter(MasterActivity.this,
                R.layout.item_nav_list, listNavItems);
        listView.setAdapter(myNavListAdapter);

        listFragments = new ArrayList<>();
        listFragments.add(new FragmentPageMaster());
        listFragments.add(new FragmentPageProfile());
        listFragments.add(new FragmentDoLogout());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content,
                listFragments.get(0)).commit();
        setTitle(listNavItems.get(0).getTitle());
        listView.setItemChecked(0, true);
        drawerLayout.closeDrawer(drawerPane);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_content,
                        listFragments.get(position)).commit();
                setTitle(listNavItems.get(position).getTitle());
                listView.setItemChecked(position, true);
                drawerLayout.closeDrawer(drawerPane);
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_opened, R.string.drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}
