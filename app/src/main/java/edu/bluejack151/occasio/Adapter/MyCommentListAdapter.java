package edu.bluejack151.occasio.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.net.URL;
import java.util.List;

import edu.bluejack151.occasio.Activity.ListCommentActivity;
import edu.bluejack151.occasio.Class.CommentData;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;

public class MyCommentListAdapter extends RecyclerView.Adapter<MyCommentListAdapter.CommentViewHolder> {

    private View v;
    private static List<CommentData> commentDataList;
    private static Context currentActivity;
    private static Firebase myFirebaseRef;
    private static SharedPreferences sharedPreferences;
    private static UserData userData;
    private static final String DEFAULT_VALUE = "";

    public MyCommentListAdapter(List<CommentData> commentDataList, Context currentActivity) {
        this.commentDataList = commentDataList;
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
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_list, parent, false);
        CommentViewHolder holder = new CommentViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(final CommentViewHolder holder, final int position) {
        if (commentDataList.get(position).getUserData().getProvider().equals("password"))
            holder.cmtPic.setImageBitmap(convertToBitmap(commentDataList.get(position).getUserData().getImageString()));
        else if (commentDataList.get(position).getUserData().getProvider().equals("facebook"))
            holder.cmtPic.setImageBitmap(convertToBitmapUrl(commentDataList.get(position).getUserData().getProfileImageURL()));
        holder.cmtUsername.setText(commentDataList.get(position).getUserData().getUsername());
        holder.cmtTime.setText(convertMilistoDateDiffforHumans(commentDataList.get(position).getImageComment().getTime()));
        if (commentDataList.get(position).getImageComment().getUserCommentatorId().equals(userData.getUserId())) {
            holder.deleteIco.setVisibility(View.VISIBLE);
        } else {
            holder.deleteIco.setVisibility(View.GONE);
        }
        holder.cmtText.setText(commentDataList.get(position).getImageComment().getComment());
    }

    @Override
    public int getItemCount() {
        return commentDataList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView cmtPic, deleteIco;
        TextView cmtUsername, cmtTime, cmtText;

        public CommentViewHolder(View itemView) {
            super(itemView);
            deleteIco = (ImageView) itemView.findViewById(R.id.comment_delete_button);
            cmtPic = (ImageView) itemView.findViewById(R.id.comment_profile_pic);
            cmtUsername = (TextView) itemView.findViewById(R.id.comment_username);
            cmtTime = (TextView) itemView.findViewById(R.id.comment_time);
            cmtText = (TextView) itemView.findViewById(R.id.comment_text);

            deleteIco.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(commentDataList.get((int) getAdapterPosition()), (int) getAdapterPosition());
                }
            });
        }

        private void createDialog(final CommentData commentData, final int position) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentActivity);
            alertDialogBuilder.setMessage("Delete this comment?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deletePost(commentData, position);
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialogBuilder.create().show();
        }

        private void deletePost(final CommentData commentData, final int position) {
            myFirebaseRef.child("comments").child(commentData.getImageComment().getCommentId().toString()).setValue(null, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError == null) {
                        ListCommentActivity.removeThenNotify(position);
                        Toast.makeText(currentActivity, "Comment Remove", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
