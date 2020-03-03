package edu.bluejack151.occasio.Class;

import java.io.Serializable;

/**
 * Created by Domus on 12/29/2015.
 */
public class TimelineData implements Serializable {
    String time_imageId;
    String time_userId;
    boolean currentUserLike;
    int likeCount;
    int commentCount;
    ImageData imageData;
    UserData userData;

    public TimelineData() {
    }

    public String getTime_imageId() {
        return time_imageId;
    }

    public String getTime_userId() {
        return time_userId;
    }

    public boolean isCurrentUserLike() {
        return currentUserLike;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setTime_imageId(String time_imageId) {
        this.time_imageId = time_imageId;
    }

    public void setTime_userId(String time_userId) {
        this.time_userId = time_userId;
    }

    public void setCurrentUserLike(boolean currentUserLike) {
        this.currentUserLike = currentUserLike;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }
}
