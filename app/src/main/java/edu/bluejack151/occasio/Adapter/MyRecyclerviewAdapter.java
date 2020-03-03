package edu.bluejack151.occasio.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.bluejack151.occasio.Activity.DetailActivity;
import edu.bluejack151.occasio.Class.CurrentMasterData;
import edu.bluejack151.occasio.Class.ImageLike;
import edu.bluejack151.occasio.Class.TimelineData;
import edu.bluejack151.occasio.Class.TimelineDetailPassingHelper;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;

public class MyRecyclerviewAdapter extends RecyclerView.Adapter<MyRecyclerviewAdapter.HomeViewHolder> {

    private Context currentActivity;
    private View v;
    private List<TimelineData> timelineDataList;
    private static Firebase myFirebaseRef;
    private static SharedPreferences sharedPreferences;
    private static UserData userData;
    private static final String DEFAULT_VALUE = "";
    private long checkLikes = 0, totalLikes = 0, checkComments = 0, totalComments = 0;

    public MyRecyclerviewAdapter(List<TimelineData> timelineDataListParam, Context currentActivity) {
        this.timelineDataList = timelineDataListParam;
        this.currentActivity = currentActivity;
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        if (myFirebaseRef.getAuth() != null) {
            sharedPreferences = currentActivity.getSharedPreferences(R.string.PREFERENCES_KEY_USER + "", Context.MODE_PRIVATE);
            userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
        }
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (timelineDataList.size() > 0) {
            Collections.sort(timelineDataList, new Comparator<TimelineData>() {
                @Override
                public int compare(final TimelineData object1, final TimelineData object2) {
                    return object2.getImageData().getTimestamp().compareTo(object1.getImageData().getTimestamp());
                }
            });
        }
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_list, parent, false);
        HomeViewHolder holder = new HomeViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {


        String location = timelineDataList.get(position).getImageData().getPpName();
        holder.profileName.setText(timelineDataList.get(position).getUserData().getUsername());
        holder.postTime.setText(convertMilistoDateDiffforHumans(timelineDataList.get(position).getImageData().getTimestamp()));
        holder.caption.setText(timelineDataList.get(position).getImageData().getCaption());
        holder.homePic.setImageBitmap(convertToBitmap(timelineDataList.get(position).getImageData().getImageString()));
        if (timelineDataList.get(position).getUserData().getProvider().equals("password")) {
            holder.profilePic.setImageBitmap(convertToBitmap(timelineDataList.get(position).getUserData().getImageString()));
        } else {
            holder.profilePic.setImageBitmap(convertToBitmapUrl(timelineDataList.get(position).getUserData().getProfileImageURL()));
        }
        if (location.trim().equals("")) {
            holder.locationIco.setVisibility(View.GONE);
            holder.location.setVisibility(View.GONE);
        } else {
            holder.location.setText(location);
        }
        holder.likeIco.setVisibility(View.GONE);
        if (timelineDataList.get(position).getLikeCount() == 0) {
            holder.likeCounter.setVisibility(View.GONE);
        } else {
            holder.likeCounter.setVisibility(View.VISIBLE);
            if (timelineDataList.get(position).getLikeCount() == 1) {
                if (timelineDataList.get(position).isCurrentUserLike()) {
                    holder.likeIco.setVisibility(View.VISIBLE);
                    holder.likeCounter.setText(timelineDataList.get(position).getLikeCount() + " like");
                } else {
                    holder.likeCounter.setText(timelineDataList.get(position).getLikeCount() + " like");
                }
            } else {
                if (timelineDataList.get(position).isCurrentUserLike()) {
                    holder.likeIco.setVisibility(View.VISIBLE);
                    holder.likeCounter.setText(timelineDataList.get(position).getLikeCount() + " likes");
                } else {
                    holder.likeCounter.setText(timelineDataList.get(position).getLikeCount() + " likes");
                }
            }
        }
        if (timelineDataList.get(position).getImageData().getCaption().equals("")){
            holder.caption.setVisibility(View.GONE);
        }

        if (timelineDataList.get(position).getCommentCount() == 0) {
            holder.commentCounter.setVisibility(View.GONE);
        } else {
            holder.commentCounter.setVisibility(View.VISIBLE);
            if (timelineDataList.get(position).getCommentCount() == 1) {
                holder.commentCounter.setText(timelineDataList.get(position).getCommentCount() + " comment");
            } else {
                holder.commentCounter.setText(timelineDataList.get(position).getCommentCount() + " comments");
            }
        }
        if (timelineDataList.get(position).getImageData().getUserId().equals(userData.getUserId())) {
            holder.deleteIco.setVisibility(View.VISIBLE);
        } else {
            holder.deleteIco.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return timelineDataList.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView profilePic, homePic, locationIco, likeIco, deleteIco;
        TextView profileName, location, postTime, caption, likeCounter, commentCounter;

        boolean doubleClick = false, eventDone = false;
        android.os.Handler doubleHandler;
        android.os.Handler singleHandler;

        public HomeViewHolder(View itemView) {
            super(itemView);
            likeIco = (ImageView) itemView.findViewById(R.id.home_like_button);
            deleteIco = (ImageView) itemView.findViewById(R.id.home_delete_button);
            profilePic = (ImageView) itemView.findViewById(R.id.home_profile_pic);
            homePic = (ImageView) itemView.findViewById(R.id.home_picture);
            locationIco = (ImageView) itemView.findViewById(R.id.home_location_ico);
            profileName = (TextView) itemView.findViewById(R.id.home_profile_name);
            location = (TextView) itemView.findViewById(R.id.home_location_name);
            caption = (TextView) itemView.findViewById(R.id.home_caption_image);
            postTime = (TextView) itemView.findViewById(R.id.home_time);
            likeCounter = (TextView) itemView.findViewById(R.id.home_like_count);
            commentCounter = (TextView) itemView.findViewById(R.id.home_comment_count);
            homePic.setOnClickListener(this);
            location.setOnClickListener(this);
            deleteIco.setOnClickListener(this);
            doubleHandler = new android.os.Handler();
            singleHandler = new android.os.Handler();
        }

        @Override
        public void onClick(View v) {
            final View view = v;
            final int position = getAdapterPosition();
            switch (v.getId()) {
                case R.id.home_picture:

                    if (doubleClick) {
                        eventDone = true;
                        toggleLikeFn(position);
                    } else {
                        doubleClick = true;
                        eventDone = false;
                        doubleHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(doubleClick && !eventDone){
                                    Intent intent = new Intent(view.getContext(), DetailActivity.class);
                                    Bundle bundle = new Bundle();
                                    Bundle bundle_src = new Bundle();
                                    bundle_src.putString("source", "timeline");
                                    bundle.putSerializable("timelineDataDetail", new TimelineDetailPassingHelper(timelineDataList.get(position)));
                                    intent.putExtras(bundle);
                                    intent.putExtras(bundle_src);
                                    view.getContext().startActivity(intent);
                                }
                                doubleClick = false;
                            }
                        }, 300);
                    }

                    break;
                case R.id.home_location_name:
                    try {
                        Uri gmmIntentUri = Uri.parse("geo:" + timelineDataList.get(position).getImageData().getPpLatLngLatitude() + "," + timelineDataList.get(position).getImageData().getPpLatLngLatitude() + "?q=" + timelineDataList.get(position).getImageData().getPpName());
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
                case R.id.home_delete_button:
                    createDialog(timelineDataList.get(position), position);
                    break;
            }

        }

        private void createDialog(final TimelineData timelineData, final int position) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentActivity);
            alertDialogBuilder.setMessage("Delete this post?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deletePost(timelineData, position);
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialogBuilder.create().show();
        }

        private void deletePost(final TimelineData timelineData, final int position) {
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
                                    fnCheckSuccessRemove(position);
                                }
                                fnCheckSuccessRemove(position);
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
                                    fnCheckSuccessRemove(position);
                                }
                                fnCheckSuccessRemove(position);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(currentActivity, "Delete Post Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void fnCheckSuccessRemove(int position) {
            if (checkLikes >= totalLikes && checkComments >= totalComments) {
                Toast.makeText(currentActivity, "Post Removed", Toast.LENGTH_SHORT).show();
                timelineDataList.remove(position);
                CurrentMasterData.getInstance().notifyDataSetChanged_TimelineView();
            }
        }


        private void toggleLikeFn(final int pos) {
            if (timelineDataList.get(pos).isCurrentUserLike()) {
                myFirebaseRef.child("likes").orderByChild("userId").equalTo(userData.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                            if (tempDataSnapshot.child("imageId").getValue().equals(timelineDataList.get(pos).getTime_imageId())) {
                                myFirebaseRef.child("likes").child(tempDataSnapshot.getKey()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                likeIco.setVisibility(View.GONE);
                timelineDataList.get(pos).setCurrentUserLike(!timelineDataList.get(pos).isCurrentUserLike());
                timelineDataList.get(pos).setLikeCount(timelineDataList.get(pos).getLikeCount() - 1);
                if (timelineDataList.get(pos).getLikeCount() == 0) {
                    likeCounter.setVisibility(View.GONE);
                    likeCounter.setText("be the first");
                } else {
                    likeCounter.setVisibility(View.VISIBLE);
                    if (timelineDataList.get(pos).getLikeCount() == 1) {
                        likeCounter.setText(timelineDataList.get(pos).getLikeCount() + " like");
                    } else {
                        likeCounter.setText(timelineDataList.get(pos).getLikeCount() + " likes");
                    }
                }

            } else {
                String likesKey = myFirebaseRef.child("likes").push().getKey();
                myFirebaseRef.child("likes").child(likesKey).setValue(new ImageLike(likesKey, timelineDataList.get(pos).getTime_imageId(), userData.getUserId()));
                timelineDataList.get(pos).setCurrentUserLike(!timelineDataList.get(pos).isCurrentUserLike());
                timelineDataList.get(pos).setLikeCount(timelineDataList.get(pos).getLikeCount() + 1);
                likeCounter.setVisibility(View.VISIBLE);
                likeIco.setVisibility(View.VISIBLE);
                if (timelineDataList.get(pos).getLikeCount() == 1) {
                    likeCounter.setText(timelineDataList.get(pos).getLikeCount() + " like");
                } else {
                    likeCounter.setText(timelineDataList.get(pos).getLikeCount() + " likes");
                }
            }

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
}
