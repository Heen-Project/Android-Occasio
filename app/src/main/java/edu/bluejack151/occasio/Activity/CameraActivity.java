package edu.bluejack151.occasio.Activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.ByteArrayOutputStream;

import edu.bluejack151.occasio.Class.ImageData;
import edu.bluejack151.occasio.Class.TimelineData;
import edu.bluejack151.occasio.Class.TimelineDetailPassingHelper;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;


public class CameraActivity extends AppCompatActivity {

    private static Toolbar toolbar;
    private Firebase myFirebaseRef;
    private ImageView imgvCamera;
    private EditText txtCaption;
    private TextView lblLocation, lblError;
    private Switch swLocation;
    private Button btnUpload;
    static final int REQUEST_IMAGE_CAPTURE = 1, PLACE_PICKER_REQUEST = 2;
    private String userId, imageString, caption, timestamp, ppAddress, ppLatLng, ppName, ppLatLngLatitude, ppLatLngLongitude, ppPhoneNumber, ppAttributions;
    private UserData userData;
    private SharedPreferences sharedPreferences;
    private final String DEFAULT_VALUE = "";
    private ProgressDialog progressDialog;
    private InputMethodManager imm;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        sharedPreferences = getSharedPreferences(String.valueOf(R.string.PREFERENCES_KEY_USER), Context.MODE_PRIVATE);
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        imgvCamera = (ImageView) findViewById(R.id.imgvCaptureImage_Camera);
        txtCaption = (EditText) findViewById(R.id.txtCaption_Camera);
        lblLocation = (TextView) findViewById(R.id.lblLocation_Camera);
        lblError = (TextView) findViewById(R.id.lblError_Camera);
        swLocation = (Switch) findViewById(R.id.swLocation_Camera);
        btnUpload = (Button) findViewById(R.id.btnUpload_Camera);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("Upload");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");
        lblLocation.setVisibility(View.INVISIBLE);
        lblError.setVisibility(View.INVISIBLE);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        layout = (LinearLayout)findViewById(R.id.ly_camera_layout);

        initUserData();

        swLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        PlacePicker.IntentBuilder intentBuilder =
                                new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(CameraActivity.this);
                        startActivityForResult(intent, PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                    lblLocation.setVisibility(View.VISIBLE);
                } else {
                    lblLocation.setVisibility(View.INVISIBLE);
                    ppName = null;
                    ppAddress = null;
                    ppLatLng = null;
                    ppPhoneNumber = null;
                    ppAttributions = null;
                }
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtCaption.getText().equals("")) {
                    lblError.setText("Caption Must be Filled");
                    lblError.setVisibility(View.VISIBLE);
                } else {
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    progressDialog.show();
                    lblError.setText("");
                    lblError.setVisibility(View.INVISIBLE);
                    try {
                        String generatedKey = myFirebaseRef.child("images").push().getKey();
                        userId = userData.getUserId().toString();
                        Long timeStringLong = System.currentTimeMillis();
                        timestamp = timeStringLong.toString();
                        caption = txtCaption.getText().toString();
                        final ImageData image = new ImageData(generatedKey, userId, imageString, caption, timestamp, ppAddress, ppLatLng, ppLatLngLatitude, ppLatLngLongitude, ppName, ppPhoneNumber, ppAttributions);
                        myFirebaseRef.child("images").child(generatedKey).setValue(image, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError == null) {
                                    startActivity(new Intent(CameraActivity.this, MasterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("refresh", true));
                                    notification(image);
                                    progressDialog.dismiss();
                                    finish();
                                    Toast.makeText(CameraActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CameraActivity.this, "Retry Upload", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap tempimageBitmap = Bitmap.createScaledBitmap((Bitmap) extras.get("data"), 500, 500, false);
                    Bitmap imageBitmap;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if (tempimageBitmap.getWidth() >= tempimageBitmap.getHeight()) {
                        imageBitmap = Bitmap.createBitmap(
                                tempimageBitmap,
                                tempimageBitmap.getWidth() / 2 - tempimageBitmap.getHeight() / 2,
                                0,
                                tempimageBitmap.getHeight(),
                                tempimageBitmap.getHeight()
                        );
                    } else {
                        imageBitmap = Bitmap.createBitmap(
                                tempimageBitmap,
                                0,
                                tempimageBitmap.getHeight() / 2 - tempimageBitmap.getWidth() / 2,
                                tempimageBitmap.getWidth(),
                                tempimageBitmap.getWidth()
                        );
                    }
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    imageString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                    imgvCamera.setImageBitmap(imageBitmap);
                } else {
                    onBackPressed();
                }
                break;
            case PLACE_PICKER_REQUEST:
                if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
                    Place place = PlacePicker.getPlace(data, this);
                    ppName = place.getName().toString();
                    ppAddress = place.getAddress().toString();
                    ppLatLng = place.getLatLng().toString();
                    ppLatLngLatitude = place.getLatLng().latitude + "";
                    ppLatLngLongitude = place.getLatLng().longitude + "";
                    ppPhoneNumber = place.getPhoneNumber().toString();
                    ppAttributions = PlacePicker.getAttributions(data);
                    if (ppAttributions == null) {
                        ppAttributions = "";
                    }
                    lblLocation.setText(ppName + " \n" + ppAddress);
                } else {
                    swLocation.setChecked(false);
                }
                break;
        }
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
            dispatchTakePictureIntent();
        } else {
            startActivity(new Intent(CameraActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

    private void notification(ImageData image) {
        int mId = 1;
        byte[] decodedString = Base64.decode(image.getImageString(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setLargeIcon(decodedByte)
                        .setContentTitle("Image Uploaded Successfully")
                        .setOnlyAlertOnce(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setAutoCancel(true);
        if (image.getPpName().equals("")) {
            mBuilder.setContentText(image.getCaption());
        } else {
            mBuilder.setContentText(image.getCaption() + "\n, " + image.getPpName());
        }
        TimelineData timelineData = new TimelineData();
        timelineData.setTime_imageId(image.getImageId());
        timelineData.setImageData(image);
        timelineData.setTime_userId(userData.getUserId());
        timelineData.setUserData(userData);
        timelineData.setLikeCount(0);
        timelineData.setCommentCount(0);
        timelineData.setCurrentUserLike(false);

        Intent resultIntent = new Intent(CameraActivity.this, DetailActivity.class);
        Bundle bundle = new Bundle();
        Bundle bundle_src = new Bundle();
        bundle_src.putString("source", "camerauinotification");
        Bundle bundle_dataChange = new Bundle();
        bundle_src.putBoolean("dataChange", false);
        bundle.putSerializable("timelineDataDetail", new TimelineDetailPassingHelper(timelineData));
        resultIntent.putExtras(bundle);
        resultIntent.putExtras(bundle_src);
        resultIntent.putExtras(bundle_dataChange);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MasterActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
