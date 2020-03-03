package edu.bluejack151.occasio.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.bluejack151.occasio.Adapter.MyCommentListAdapter;
import edu.bluejack151.occasio.Class.CommentData;
import edu.bluejack151.occasio.Class.ImageComment;
import edu.bluejack151.occasio.Class.TimelineData;
import edu.bluejack151.occasio.Class.TimelineDetailPassingHelper;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;

public class ListCommentActivity extends AppCompatActivity implements View.OnClickListener {

    private static Toolbar toolbar;
    private static List<CommentData> commentDataList;
    private Button btnSend;
    private LinearLayout ltBtnSend;
    private RelativeLayout progressBar;
    private static Firebase myFirebaseRef;
    private static TimelineData timelineData;
    private static SharedPreferences sharedPreferences;
    private static UserData userData;
    private static final String DEFAULT_VALUE = "";
    private static TimelineDetailPassingHelper passingData;
    private long dataCount, currentCount;
    private EditText txtAddComment;
    private boolean dataChange;
    /* Adapter */
    private static MyCommentListAdapter myCommentListAdapter;
    private static RecyclerView recyclerView;
    private static LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataChange = false;
        setContentView(R.layout.activity_list_comment);
        initMustFirst();
        btnSend = (Button) findViewById(R.id.btn_adding_send_detail);
        ltBtnSend = (LinearLayout) findViewById(R.id.lt_adding_send_detail);
        txtAddComment = (EditText) findViewById(R.id.txtAdding_comment);
        progressBar = (RelativeLayout) findViewById(R.id.load_comment);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("Comment");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        sharedPreferences = getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", Context.MODE_PRIVATE);
        userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
        passingData = (TimelineDetailPassingHelper) this.getIntent().getExtras().getSerializable("timelineDataDetail");
        timelineData = passingData.getTimelineData();
        loadCommentFn(myCommentListAdapter, recyclerView, linearLayoutManager);
        btnSend.setOnClickListener(this);
        ltBtnSend.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        dataChange = true;
        switch (v.getId()) {
            case R.id.lt_adding_send_detail:
                sendCommentFn();
                break;
            case R.id.btn_adding_send_detail:
                sendCommentFn();
                break;
        }
    }

    private void sendCommentFn() {
        if (!txtAddComment.getText().toString().trim().equals("")) {
            String commentKey = myFirebaseRef.child("comments").push().getKey();
            final ImageComment newImageComment = new ImageComment(commentKey, timelineData.getTime_imageId(), userData.getUserId(), txtAddComment.getText().toString().trim(), String.valueOf(System.currentTimeMillis()));
            myFirebaseRef.child("comments").child(commentKey).setValue(newImageComment, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Toast.makeText(ListCommentActivity.this, "Please try Again", Toast.LENGTH_SHORT).show();
                    } else {
                        CommentData tempCommentData = new CommentData();
                        tempCommentData.setImageId(timelineData.getTime_imageId());
                        tempCommentData.setUserData(userData);
                        tempCommentData.setImageComment(newImageComment);
                        commentDataList.add(tempCommentData);
                        timelineData.setCommentCount(timelineData.getCommentCount() + 1);
                        myCommentListAdapter.notifyDataSetChanged();
                        txtAddComment.setText("");
                        Toast.makeText(ListCommentActivity.this, "Comment Sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void initMustFirst() {
        recyclerView = (RecyclerView) findViewById(R.id.comment_list);
        commentDataList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(ListCommentActivity.this);
        myCommentListAdapter = new MyCommentListAdapter(commentDataList, ListCommentActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myCommentListAdapter);
    }

    private void loadCommentFn(final MyCommentListAdapter myCommentListAdapter, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager) {
        progressBar.setVisibility(View.VISIBLE);
        currentCount = 0;
        Query queryRef = myFirebaseRef.child("comments").orderByChild("imageId").equalTo(timelineData.getTime_imageId().toString());
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataCount = dataSnapshot.getChildrenCount();
                if (dataCount <= 0) {
                    progressBar.setVisibility(View.GONE);
                }
                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                    final CommentData tempCommentData = new CommentData();
                    tempCommentData.setImageId(timelineData.getTime_imageId().toString());
                    ImageComment tempImageComment = new ImageComment(tempDataSnapshot.child("commentId").getValue().toString(), tempDataSnapshot.child("imageId").getValue().toString(), tempDataSnapshot.child("userCommentatorId").getValue().toString(), tempDataSnapshot.child("comment").getValue().toString(), tempDataSnapshot.child("time").getValue().toString());
                    tempCommentData.setImageComment(tempImageComment);
                    myFirebaseRef.child("users").orderByChild("userId").equalTo(tempImageComment.getUserCommentatorId().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                UserData tempUserData = new UserData(tempDataSnapshot.child("userId").getValue().toString(), tempDataSnapshot.child("provider").getValue().toString(), tempDataSnapshot.child("username").getValue().toString(), tempDataSnapshot.child("email").getValue().toString(), tempDataSnapshot.child("profileImageURL").getValue().toString(), tempDataSnapshot.child("imageString").getValue().toString(), tempDataSnapshot.child("password").getValue().toString());
                                tempCommentData.setUserData(tempUserData);
                                commentDataList.add(tempCommentData);
                                currentCount++;
                                if (currentCount >= dataCount) {
                                    sortCommentDataAsc(commentDataList);
                                    myCommentListAdapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            intentDetail();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                intentDetail();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void intentDetail() {
        Intent intent = new Intent(ListCommentActivity.this, DetailActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = new Bundle();
        Bundle bundle_src = new Bundle();
        bundle_src.putString("source", "comment");
        Bundle bundle_dataChange = new Bundle();
        bundle_src.putBoolean("dataChange", dataChange);
        bundle.putSerializable("timelineDataDetail", new TimelineDetailPassingHelper(timelineData));
        intent.putExtras(bundle);
        intent.putExtras(bundle_src);
        intent.putExtras(bundle_dataChange);
        startActivity(intent);
        finish();
    }

    private void sortCommentDataAsc(List<CommentData> commentDataList) {
        if (commentDataList.size() > 0) {
            Collections.sort(commentDataList, new Comparator<CommentData>() {
                @Override
                public int compare(final CommentData object1, final CommentData object2) {
                    return object1.getImageComment().getTime().compareTo(object2.getImageComment().getTime());
                }
            });
        }
    }

    public static void removeThenNotify(int position) {
        commentDataList.remove(position);
        timelineData.setCommentCount(timelineData.getCommentCount() - 1);
        myCommentListAdapter.notifyDataSetChanged();
    }

}
