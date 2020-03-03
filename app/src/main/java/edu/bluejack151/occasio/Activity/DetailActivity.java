package edu.bluejack151.occasio.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.net.URL;

import edu.bluejack151.occasio.Class.CurrentMasterData;
import edu.bluejack151.occasio.Class.ImageLike;
import edu.bluejack151.occasio.Class.TimelineData;
import edu.bluejack151.occasio.Class.TimelineDetailPassingHelper;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView profilePic, homePic, locationIco, likeIco, btnDelete;
    private TextView profileName, location, postTime, caption, likeCounter, seeComment;
    private LinearLayout lt_like;
    private TimelineData timelineData;
    private Toolbar toolbar;
    private boolean dataChange;
    private static Firebase myFirebaseRef;
    private SharedPreferences sharedPreferences;
    private UserData userData;
    private final String DEFAULT_VALUE = "";
    private GestureDetector gestureDetector;
    private CurrentMasterData currentMasterData;
    private TimelineDetailPassingHelper passingData;
    private final static int SOURCE_TIMELINE = 1, SOURCE_BROWSE = 2, SOURCE_COMMENT = 3, SOURCE_CAMERA_UI_NOTIFICATION = 4;
    private int dataSource;
    private long currentCount_fromNotification, totalCount_fromNotification;
    private long checkLikes = 0, totalLikes = 0, checkComments = 0, totalComments = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataChange = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        passingData = (TimelineDetailPassingHelper) this.getIntent().getExtras().getSerializable("timelineDataDetail");
        currentMasterData = CurrentMasterData.getInstance();
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        timelineData = passingData.getTimelineData();

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString("source").equals("browse")) {
                dataSource = SOURCE_BROWSE;
            } else if (getIntent().getExtras().getString("source").equals("timeline")) {
                dataSource = SOURCE_TIMELINE;
            } else if (getIntent().getExtras().getString("source").equals("comment")) {
                dataSource = SOURCE_COMMENT;
                dataChange = getIntent().getExtras().getBoolean("dataChange");
            } else if (getIntent().getExtras().getString("source").equals("camerauinotification")) {
                dataSource = SOURCE_CAMERA_UI_NOTIFICATION;
                dataChange = getIntent().getExtras().getBoolean("dataChange");
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
        if (dataSource == SOURCE_CAMERA_UI_NOTIFICATION) {
            loadData_fromNotification();
        } else {
            initComponent();
        }
    }

    private Bitmap convertToBitmap(String picString) {
        byte[] decodedString = Base64.decode(picString, Base64.DEFAULT);
        Bitmap bitmapHomePic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bitmapHomePic;
    }

    private Bitmap convertToBitmapUrl(String picUrl) {
        Bitmap bitmapProfilePic = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL(picUrl);
            bitmapProfilePic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmapProfilePic;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            try {
                NavUtils.navigateUpFromSameTask(this);
            } catch (Exception e) {
                if (!dataChange) {
                    if (dataSource == SOURCE_COMMENT) {
                        intentMaster_withNoChange();
                    } else {
                        onBackPressed();
                    }
                } else {
                    if (dataSource == SOURCE_BROWSE) {
                        currentMasterData.rebrowseBrowseView();
                        onBackPressed();
                    } else if (dataSource == SOURCE_TIMELINE || dataSource == SOURCE_COMMENT || dataSource == SOURCE_CAMERA_UI_NOTIFICATION) {
                        intentMaster();
                    }
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                try {
                    NavUtils.navigateUpFromSameTask(this);
                } catch (Exception e) {
                    if (!dataChange) {
                        if (dataSource == SOURCE_COMMENT) {
                            intentMaster_withNoChange();
                        } else {
                            onBackPressed();
                        }
                    } else {
                        if (dataSource == SOURCE_BROWSE) {
                            currentMasterData.rebrowseBrowseView();
                            onBackPressed();
                        } else if (dataSource == SOURCE_TIMELINE || dataSource == SOURCE_COMMENT || dataSource == SOURCE_CAMERA_UI_NOTIFICATION) {
                            intentMaster();
                        }
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        dataChange = true;
        switch (v.getId()) {
            case R.id.lt_likegroup_detail:
                toggleLikeFn();
                break;
            case R.id.home_location_name:
                try {
                    Uri gmmIntentUri = Uri.parse("geo:" + timelineData.getImageData().getPpLatLngLatitude() + "," + timelineData.getImageData().getPpLatLngLatitude() + "?q=" + timelineData.getImageData().getPpName());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    v.getContext().startActivity(mapIntent);
                } catch (Exception e) {
                    Intent chooser;
                    Uri googleMapsUri = Uri.parse("market://details?id=com.google.android.apps.maps");
                    Intent googleAppIntent = new Intent(Intent.ACTION_VIEW, googleMapsUri);
                    chooser = Intent.createChooser(googleAppIntent, "Google Maps not found, Download appropriate the application first");
                    v.getContext().startActivity(chooser);
                }
                break;
            case R.id.see_more_comment:
                Intent intent = new Intent(DetailActivity.this, ListCommentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("timelineDataDetail", new TimelineDetailPassingHelper(timelineData));
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.detail_delete_button:
                createDialog();
                break;
        }
    }

    private void createDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Delete this post?");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePost();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.create().show();
    }

    private void deletePost() {
        checkLikes = checkComments = 0;
        myFirebaseRef.child("images").child(timelineData.getTime_imageId()).setValue(null, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    myFirebaseRef.child("likes").orderByChild("imageId").equalTo(timelineData.getTime_imageId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            totalLikes = dataSnapshot.getChildrenCount();
                            for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                myFirebaseRef.child("likes").child(tempDataSnapshot.getKey()).setValue(null);
                                checkLikes++;
                                fnCheckSuccessRemove();
                            }
                            fnCheckSuccessRemove();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                    myFirebaseRef.child("comments").orderByChild("imageId").equalTo(timelineData.getTime_imageId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            totalComments = dataSnapshot.getChildrenCount();
                            for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                myFirebaseRef.child("comments").child(tempDataSnapshot.getKey()).setValue(null);
                                checkComments++;
                                fnCheckSuccessRemove();
                            }
                            fnCheckSuccessRemove();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                } else {
                    Toast.makeText(DetailActivity.this, "Delete Post Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fnCheckSuccessRemove() {
        if (checkLikes >= totalLikes && checkComments >= totalComments) {
            Toast.makeText(DetailActivity.this, "Post Removed", Toast.LENGTH_SHORT).show();
            intentMaster();
        }
    }

    private void toggleLikeFn() {
        if (timelineData.isCurrentUserLike()) {
            myFirebaseRef.child("likes").orderByChild("userId").equalTo(userData.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                        if (tempDataSnapshot.child("imageId").getValue().equals(timelineData.getTime_imageId())) {
                            myFirebaseRef.child("likes").child(tempDataSnapshot.getKey()).removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            if(timelineData.getImageData().getCaption().equals("")){
                caption.setVisibility(View.GONE);
            }
            timelineData.setCurrentUserLike(!timelineData.isCurrentUserLike());
            timelineData.setLikeCount(timelineData.getLikeCount() - 1);
            likeIco.setImageResource(R.drawable.ic_like);
            if (timelineData.getLikeCount() == 0) {
                likeCounter.setText("be the first");
            } else {
                if (timelineData.getLikeCount() == 1) {
                    likeCounter.setText(timelineData.getLikeCount() + " like");
                } else {
                    likeCounter.setText(timelineData.getLikeCount() + " likes");
                }
            }

        } else {
            String likesKey = myFirebaseRef.child("likes").push().getKey();
            myFirebaseRef.child("likes").child(likesKey).setValue(new ImageLike(likesKey, timelineData.getTime_imageId(), userData.getUserId()));
            timelineData.setCurrentUserLike(!timelineData.isCurrentUserLike());
            timelineData.setLikeCount(timelineData.getLikeCount() + 1);
            likeIco.setImageResource(R.drawable.ic_like_green);
            likeCounter.setText(timelineData.getLikeCount() + " likes");
        }

    }

    private void initComponent() {
        if (myFirebaseRef.getAuth() != null) {
            sharedPreferences = getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", MODE_PRIVATE);
            userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
            if (timelineData != null) {
                String locationStr = timelineData.getImageData().getPpName();
                lt_like = (LinearLayout) findViewById(R.id.lt_likegroup_detail);
                likeIco = (ImageView) findViewById(R.id.home_like_button);
                profilePic = (ImageView) findViewById(R.id.home_profile_pic);
                homePic = (ImageView) findViewById(R.id.home_picture);
                locationIco = (ImageView) findViewById(R.id.home_location_ico);
                profileName = (TextView) findViewById(R.id.home_profile_name);
                location = (TextView) findViewById(R.id.home_location_name);
                caption = (TextView) findViewById(R.id.home_caption_image);
                postTime = (TextView) findViewById(R.id.home_time);
                likeCounter = (TextView) findViewById(R.id.home_like_count);
                seeComment = (TextView) findViewById(R.id.see_more_comment);
                btnDelete = (ImageView) findViewById(R.id.detail_delete_button);

                profileName.setText(timelineData.getUserData().getUsername());
                homePic.setImageBitmap(convertToBitmap(timelineData.getImageData().getImageString()));
                postTime.setText(convertMilistoDateDiffforHumans(timelineData.getImageData().getTimestamp()));
                caption.setText(timelineData.getImageData().getCaption());
                homePic.setImageBitmap(convertToBitmap(timelineData.getImageData().getImageString()));
                if (timelineData.getUserData().getProvider().equals("password")) {
                    profilePic.setImageBitmap(convertToBitmap(timelineData.getUserData().getImageString()));
                } else {
                    profilePic.setImageBitmap(convertToBitmapUrl(timelineData.getUserData().getProfileImageURL()));
                }
                if (locationStr.trim().equals("")) {
                    location.setVisibility(View.GONE);
                    locationIco.setVisibility(View.GONE);
                } else {
                    locationIco.setVisibility(View.VISIBLE);
                    location.setText(locationStr);
                }
                if (timelineData.getLikeCount() == 0) {
                    likeCounter.setText("be the first");
                } else {
                    if (timelineData.getLikeCount() == 1) {
                        likeCounter.setText(timelineData.getLikeCount() + " like");
                    } else {
                        likeCounter.setText(timelineData.getLikeCount() + " likes");
                    }
                }
                if (timelineData.isCurrentUserLike()) {
                    likeIco.setVisibility(View.VISIBLE);
                    likeIco.setImageResource(R.drawable.ic_like_green);
                } else {
                    likeIco.setVisibility(View.VISIBLE);
                    likeIco.setImageResource(R.drawable.ic_like);
                }
                if (timelineData.getCommentCount() == 0) {
                    seeComment.setText("Add comment");
                } else {
                    if (timelineData.getCommentCount() == 1) {
                        seeComment.setText("View " + timelineData.getCommentCount() + " comment");
                    } else {
                        seeComment.setText("View All " + timelineData.getCommentCount() + " comments");
                    }
                }
                if (timelineData.getImageData().getUserId().equals(userData.getUserId())) {
                    btnDelete.setVisibility(View.VISIBLE);
                } else {
                    btnDelete.setVisibility(View.GONE);
                }

                gestureDetector = new GestureDetector(DetailActivity.this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        dataChange = true;
                        toggleLikeFn();
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onDoubleTapEvent(MotionEvent e) {
                        return false;
                    }
                });
                homePic.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }
                });
                location.setOnClickListener(this);
                lt_like.setOnClickListener(this);
                seeComment.setOnClickListener(this);
                btnDelete.setOnClickListener(this);
            } else {
                onBackPressed();
            }
        } else {
            onBackPressed();
        }
    }

    private String convertMilistoDateDiffforHumans(String systemMilis) {
        String timeString;
        Long milis = (System.currentTimeMillis() - Long.parseLong(systemMilis)) / 1000;
        Long years, weeks, days, hours, minutes, seconds;
        seconds = (milis) % 60;
        minutes = (milis / 60) % 60;
        hours = (milis / (60 * 60)) % 24;
        days = (milis / (60 * 60 * 24)) % 7;
        weeks = (milis / (60 * 60 * 24 * 7)) % 52;
        years = (milis / (60 * 60 * 24 * 52));
        if (years != 0) timeString = years + "y " + weeks + "w";
        else if (weeks != 0) timeString = weeks + "w " + days + "d";
        else if (days != 0) timeString = days + "d " + hours + "h";
        else if (hours != 0) timeString = hours + "h " + minutes + "m";
        else if (minutes != 0) timeString = minutes + "m " + seconds + "s";
        else timeString = seconds + "s";
        return timeString;
    }

    public void intentMaster() {
        startActivity(new Intent(DetailActivity.this, MasterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("refresh", true));
        finish();
    }

    public void intentMaster_withNoChange() {
        startActivity(new Intent(DetailActivity.this, MasterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void loadData_fromNotification() {
        currentCount_fromNotification = 0;
        myFirebaseRef.child("comments").orderByChild("imageId").equalTo(timelineData.getTime_imageId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timelineData.setCommentCount((int) dataSnapshot.getChildrenCount());
                myFirebaseRef.child("likes").orderByChild("imageId").equalTo(timelineData.getTime_imageId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        timelineData.setLikeCount((int) dataSnapshot.getChildrenCount());
                        totalCount_fromNotification = dataSnapshot.getChildrenCount();
                        if (dataSnapshot.getChildrenCount() == 0) {
                            initComponent();
                        }
                        for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                            currentCount_fromNotification++;
                            try {
                                if (tempDataSnapshot.child("userId").getValue().equals(userData.getUserId())) {
                                    timelineData.setCurrentUserLike(true);
                                }
                            } catch (Exception e) {
                            }
                            if (currentCount_fromNotification >= totalCount_fromNotification) {
                                initComponent();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

}
