package edu.bluejack151.occasio.Class;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;
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

import edu.bluejack151.occasio.Adapter.MyGridViewAdapter;
import edu.bluejack151.occasio.Adapter.MyRecyclerviewAdapter;
import edu.bluejack151.occasio.R;

/**
 * Created by Domus on 12/27/2015.
 */
public class CurrentMasterData {
    private static CurrentMasterData instance;
    private static Firebase myFirebaseRef;
    private static List<TimelineData> timelineDataList, browseDataList;
    private static SharedPreferences sharedPreferences;
    private static UserData userData;
    private static final String DEFAULT_VALUE = "";
    private static final int MODE_REFRESH = 2, MODE_LOADNEXT = 3, MODE_BROWSE = 7;
    private long dataCount, currentCount;
    private ProgressDialog progressDialog;
    /*   TIMELINE   */
    private static Context timelineActivity_current;
    private static MyRecyclerviewAdapter myRecyclerviewAdapter_current;
    private static RecyclerView recyclerView_current;
    private static LinearLayoutManager linearLayoutManager_current;
    /*   BROWSE   */
    private static Context browseActivity_current;
    private static GridView gridView_current;
    private static MyGridViewAdapter myGridViewAdapter_current;
    private static String lastSearchString_current;

    public static List<TimelineData> getBrowseDataList() {
        return browseDataList;
    }

    public void fnLogout_clearData() {
        timelineDataList = browseDataList = null;
    }

    public static CurrentMasterData getInstance() {
        if (instance == null) {
            instance = new CurrentMasterData();
            myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        }
        return instance;
    }

    private CurrentMasterData() {
    }

    public void tryLoad(final Context currentActivity, final MyRecyclerviewAdapter myRecyclerviewAdapter, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager) {
        timelineDataList = new ArrayList<>();
        Query queryRef = myFirebaseRef.child("images").orderByChild("timestamp").limitToLast(10);
        callQuery_withProgressBar(MODE_REFRESH, queryRef, currentActivity, myRecyclerviewAdapter, recyclerView, linearLayoutManager);
        timelineActivity_current = currentActivity;
        myRecyclerviewAdapter_current = myRecyclerviewAdapter;
        recyclerView_current = recyclerView;
        linearLayoutManager_current = linearLayoutManager;
    }

    public void firstLoad(Context currentActivity, MyRecyclerviewAdapter myRecyclerviewAdapter, RecyclerView recyclerView, LinearLayoutManager linearLayoutManager) {
        if (timelineDataList == null) {
            tryLoad(currentActivity, myRecyclerviewAdapter, recyclerView, linearLayoutManager);
        } else {
            setTimelineView(currentActivity, myRecyclerviewAdapter, recyclerView, linearLayoutManager);
        }
    }

   public void loadNextImageData(final Context currentActivity, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager) {
        Query queryRef = myFirebaseRef.child("images").orderByChild("timestamp").endAt(String.valueOf(Long.parseLong(timelineDataList.get(timelineDataList.size() - 1).getImageData().getTimestamp()) - 1)).limitToLast(10);
        callQuery_withoutProgressBar(MODE_LOADNEXT, queryRef, currentActivity, myRecyclerviewAdapter_current, recyclerView, linearLayoutManager);
    }

    public boolean loadNextImageData_withReturnValue(final Context currentActivity, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager, RelativeLayout progressBar_layout) {
        Query queryRef = myFirebaseRef.child("images").orderByChild("timestamp").endAt(String.valueOf(Long.parseLong(timelineDataList.get(timelineDataList.size() - 1).getImageData().getTimestamp()) - 1)).limitToLast(10);
        callQuery_withoutProgressBar_withReturnValue(MODE_LOADNEXT, queryRef, currentActivity, myRecyclerviewAdapter_current, recyclerView, linearLayoutManager, progressBar_layout);
        return false;
    }

    private void setTimelineView(Context currentActivity, MyRecyclerviewAdapter myRecyclerviewAdapter, RecyclerView recyclerView, LinearLayoutManager linearLayoutManager) {
        myRecyclerviewAdapter_current = new MyRecyclerviewAdapter(timelineDataList, currentActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myRecyclerviewAdapter_current);
        myRecyclerviewAdapter_current.notifyDataSetChanged();
        recyclerView_current = recyclerView;
        linearLayoutManager_current = linearLayoutManager;
    }

    private void appendTimelineView(Context currentActivity, MyRecyclerviewAdapter myRecyclerviewAdapter, RecyclerView recyclerView, LinearLayoutManager linearLayoutManager) {
        if (timelineDataList.size() > 0) {
            Collections.sort(timelineDataList, new Comparator<TimelineData>() {
                @Override
                public int compare(final TimelineData object1, final TimelineData object2) {
                    return object2.getImageData().getTimestamp().compareTo(object1.getImageData().getTimestamp());
                }
            });
        }
        myRecyclerviewAdapter_current.notifyDataSetChanged();
        recyclerView_current = recyclerView;
        linearLayoutManager_current = linearLayoutManager;
    }

    public void notifyDataSetChanged_TimelineView() {
        myRecyclerviewAdapter_current.notifyDataSetChanged();
    }

    private void appendTimelineView_withReturnValue(Context currentActivity, MyRecyclerviewAdapter myRecyclerviewAdapter, RecyclerView recyclerView, LinearLayoutManager linearLayoutManager, RelativeLayout progressBar_layout) {
        if (timelineDataList.size() > 0) {
            Collections.sort(timelineDataList, new Comparator<TimelineData>() {
                @Override
                public int compare(final TimelineData object1, final TimelineData object2) {
                    return object2.getImageData().getTimestamp().compareTo(object1.getImageData().getTimestamp());
                }
            });
        }
        myRecyclerviewAdapter_current.notifyDataSetChanged();
        recyclerView_current = recyclerView;
        linearLayoutManager_current = linearLayoutManager;
        progressBar_layout.setVisibility(View.GONE);
    }

    private void callQuery_withoutProgressBar(final int mode, Query queryRef, final Context currentActivity, final MyRecyclerviewAdapter myRecyclerviewAdapter, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager) {
        currentCount = 0;
        sharedPreferences = currentActivity.getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", Context.MODE_PRIVATE);
        userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataCount = dataSnapshot.getChildrenCount();
                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                    final String imageId = tempDataSnapshot.getKey();
                    final TimelineData tempTimeLine = new TimelineData();
                    timelineDataList.add(tempTimeLine);
                    tempTimeLine.setCurrentUserLike(false);
                    timelineDataList.get(timelineDataList.size() - 1).setTime_imageId(imageId);
                    timelineDataList.get(timelineDataList.size() - 1).setImageData(tempDataSnapshot.getValue(ImageData.class));
                    myFirebaseRef.child("images/" + imageId + "/userId").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setTime_userId(dataSnapshot.getValue().toString());
                            myFirebaseRef.child("users/" + dataSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setUserData(dataSnapshot.getValue(UserData.class));
                                    myFirebaseRef.child("likes").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                try {
                                                    if (tempDataSnapshot.child("userId").getValue().equals(userData.getUserId())) {
                                                        timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setCurrentUserLike(true);
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("SYSO ERR CHECK : " + tempDataSnapshot);
                                                }
                                            }
                                            timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setLikeCount((int) dataSnapshot.getChildrenCount());
                                            myFirebaseRef.child("comments").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setCommentCount((int) dataSnapshot.getChildrenCount());
                                                    currentCount++;
                                                    if (mode == MODE_LOADNEXT) {
                                                        if (currentCount >= dataCount) {
                                                            appendTimelineView(currentActivity, myRecyclerviewAdapter, recyclerView, linearLayoutManager);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                    Toast.makeText(currentActivity, "Error 5: " + firebaseError, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            Toast.makeText(currentActivity, "Error 4: " + firebaseError, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Toast.makeText(currentActivity, "Error 3: " + firebaseError, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(currentActivity, "Error 2: " + firebaseError, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(currentActivity, "Error 1: " + firebaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callQuery_withProgressBar(final int mode, Query queryRef, final Context currentActivity, final MyRecyclerviewAdapter myRecyclerviewAdapter, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager) {
        progressDialog = new ProgressDialog(currentActivity, R.style.AppTheme_Dark_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
        currentCount = 0;
        sharedPreferences = currentActivity.getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", Context.MODE_PRIVATE);
        userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataCount = dataSnapshot.getChildrenCount();
                if (dataSnapshot.getChildrenCount() == 0) {
                    progressDialog.dismiss();
                }
                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                    final String imageId = tempDataSnapshot.getKey();
                    final TimelineData tempTimeLine = new TimelineData();
                    timelineDataList.add(tempTimeLine);
                    tempTimeLine.setCurrentUserLike(false);
                    timelineDataList.get(timelineDataList.size() - 1).setTime_imageId(imageId);
                    timelineDataList.get(timelineDataList.size() - 1).setImageData(tempDataSnapshot.getValue(ImageData.class));
                    myFirebaseRef.child("images/" + imageId + "/userId").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setTime_userId(dataSnapshot.getValue().toString());
                            myFirebaseRef.child("users/" + dataSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setUserData(dataSnapshot.getValue(UserData.class));
                                    myFirebaseRef.child("likes").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                try {
                                                    if (tempDataSnapshot.child("userId").getValue().equals(userData.getUserId())) {
                                                        timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setCurrentUserLike(true);
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("SYSO ERR CHECK : " + tempDataSnapshot);
                                                }
                                            }
                                            timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setLikeCount((int) dataSnapshot.getChildrenCount());
                                            myFirebaseRef.child("comments").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setCommentCount((int) dataSnapshot.getChildrenCount());
                                                    currentCount++;
                                                    if (mode == MODE_REFRESH) {
                                                        if (currentCount >= dataCount) {
                                                            setTimelineView(currentActivity, myRecyclerviewAdapter, recyclerView, linearLayoutManager);
                                                            progressDialog.dismiss();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                    Toast.makeText(currentActivity, "Error 5: " + firebaseError, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            Toast.makeText(currentActivity, "Error 4: " + firebaseError, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Toast.makeText(currentActivity, "Error 3: " + firebaseError, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(currentActivity, "Error 2: " + firebaseError, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(currentActivity, "Error 1: " + firebaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callQuery_withoutProgressBar_withReturnValue(final int mode, Query queryRef, final Context currentActivity, final MyRecyclerviewAdapter myRecyclerviewAdapter, final RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager, final RelativeLayout progressBar_layout) {
        currentCount = 0;
        sharedPreferences = currentActivity.getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", Context.MODE_PRIVATE);
        userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataCount = dataSnapshot.getChildrenCount();
                if (dataSnapshot.getChildrenCount() == 0) {
                    progressBar_layout.setVisibility(View.GONE);
                }
                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                    final String imageId = tempDataSnapshot.getKey();
                    final TimelineData tempTimeLine = new TimelineData();
                    timelineDataList.add(tempTimeLine);
                    tempTimeLine.setCurrentUserLike(false);
                    timelineDataList.get(timelineDataList.size() - 1).setTime_imageId(imageId);
                    timelineDataList.get(timelineDataList.size() - 1).setImageData(tempDataSnapshot.getValue(ImageData.class));
                    myFirebaseRef.child("images/" + imageId + "/userId").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setTime_userId(dataSnapshot.getValue().toString());
                            myFirebaseRef.child("users/" + dataSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setUserData(dataSnapshot.getValue(UserData.class));
                                    myFirebaseRef.child("likes").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                try {
                                                    if (tempDataSnapshot.child("userId").getValue().equals(userData.getUserId())) {
                                                        timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setCurrentUserLike(true);
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("SYSO ERR CHECK : " + tempDataSnapshot);
                                                }
                                            }
                                            timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setLikeCount((int) dataSnapshot.getChildrenCount());
                                            myFirebaseRef.child("comments").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    timelineDataList.get(timelineDataList.indexOf(tempTimeLine)).setCommentCount((int) dataSnapshot.getChildrenCount());
                                                    currentCount++;
                                                    if (mode == MODE_LOADNEXT) {
                                                        if (currentCount >= dataCount) {
                                                            appendTimelineView_withReturnValue(currentActivity, myRecyclerviewAdapter, recyclerView, linearLayoutManager, progressBar_layout);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                    Toast.makeText(currentActivity, "Error 5: " + firebaseError, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            Toast.makeText(currentActivity, "Error 4: " + firebaseError, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Toast.makeText(currentActivity, "Error 3: " + firebaseError, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(currentActivity, "Error 2: " + firebaseError, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(currentActivity, "Error 1: " + firebaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void rebrowseBrowseView() {
        browseDataList = new ArrayList<>();
        browse_tryLoad(browseActivity_current, gridView_current, myGridViewAdapter_current, lastSearchString_current);
    }


    private void setBrowseView(Context currentActivity, GridView gridView, MyGridViewAdapter myGridViewAdapter, String searchString) {
        myGridViewAdapter = new MyGridViewAdapter(currentActivity, browseDataList);
        gridView.setAdapter(myGridViewAdapter);
    }

    public void browse_tryLoad(final Context currentActivity, GridView gridView, MyGridViewAdapter myGridViewAdapter, String searchString) {
        browseDataList = new ArrayList<>();
        Query queryRef = myFirebaseRef.child("images").orderByChild("timestamp");
        browseActivity_current = currentActivity;
        gridView_current = gridView;
        myGridViewAdapter_current = myGridViewAdapter;
        lastSearchString_current = searchString;
        callQuery_browse_withProgressBar(MODE_BROWSE, queryRef, currentActivity, gridView, myGridViewAdapter, searchString);
    }

    public void callQuery_browse_withProgressBar(final int mode, Query queryRef, final Context currentActivity, final GridView gridView, final MyGridViewAdapter myGridViewAdapter, final String searchString) {
        //Progress Dialog
        progressDialog = new ProgressDialog(currentActivity, R.style.AppTheme_Dark_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Search data...");
        progressDialog.show();
        currentCount = 0;
        sharedPreferences = currentActivity.getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", Context.MODE_PRIVATE);
        userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
        currentCount = dataCount = 0;
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long loopCount = 0;
                long dataGet = dataSnapshot.getChildrenCount();
                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                    final String imageId = tempDataSnapshot.getKey();
                    final TimelineData tempBrowse = new TimelineData();
                    tempBrowse.setCurrentUserLike(false);
                    loopCount++;
                    if (loopCount > dataGet && dataCount == 0) {
                        progressDialog.dismiss();
                        setBrowseView(currentActivity, gridView, myGridViewAdapter, searchString);
                        Toast.makeText(currentActivity, "No Data Found", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (String.valueOf(tempDataSnapshot.child("caption").getValue()).trim().toLowerCase().contains(searchString.trim().toLowerCase())) {
                            dataCount++;
                            browseDataList.add(tempBrowse);
                            browseDataList.get(browseDataList.size() - 1).setTime_imageId(imageId);
                            browseDataList.get(browseDataList.size() - 1).setImageData(tempDataSnapshot.getValue(ImageData.class));
                            myFirebaseRef.child("images/" + imageId + "/userId").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    browseDataList.get(browseDataList.indexOf(tempBrowse)).setTime_userId(dataSnapshot.getValue().toString());
                                    myFirebaseRef.child("users/" + dataSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            browseDataList.get(browseDataList.indexOf(tempBrowse)).setUserData(dataSnapshot.getValue(UserData.class));
                                            myFirebaseRef.child("likes").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                        try {
                                                            if (tempDataSnapshot.child("userId").getValue().equals(userData.getUserId())) {
                                                                browseDataList.get(browseDataList.indexOf(tempBrowse)).setCurrentUserLike(true);
                                                            }
                                                        } catch (Exception e) {
                                                            System.out.println("SYSO ERR CHECK : " + tempDataSnapshot);
                                                        }
                                                    }
                                                    browseDataList.get(browseDataList.indexOf(tempBrowse)).setLikeCount((int) dataSnapshot.getChildrenCount());
                                                    myFirebaseRef.child("comments").orderByChild("imageId").equalTo(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            browseDataList.get(browseDataList.indexOf(tempBrowse)).setCommentCount((int) dataSnapshot.getChildrenCount());
                                                            currentCount++;
                                                            if (mode == MODE_BROWSE) {
                                                                if (currentCount >= dataCount) {
                                                                    setBrowseView(currentActivity, gridView, myGridViewAdapter, searchString);
                                                                    progressDialog.dismiss();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(FirebaseError firebaseError) {
                                                            Toast.makeText(currentActivity, "Error 5: " + firebaseError, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                    Toast.makeText(currentActivity, "Error 4: " + firebaseError, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            Toast.makeText(currentActivity, "Error 3: " + firebaseError, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Toast.makeText(currentActivity, "Error 2: " + firebaseError, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
