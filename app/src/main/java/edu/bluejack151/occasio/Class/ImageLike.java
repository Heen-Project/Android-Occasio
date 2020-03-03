package edu.bluejack151.occasio.Class;

import java.io.Serializable;

/**
 * Created by Domus on 12/29/2015.
 */
public class ImageLike implements Serializable {
    String likeId;
    String imageId;
    String userId;

    public ImageLike(String likeId, String imageId, String userId) {
        this.likeId = likeId;
        this.imageId = imageId;
        this.userId = userId;
    }

    public String getLikeId() {
        if (likeId != null)
            return likeId;
        else return "";
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getImageId() {
        if (imageId != null)
            return imageId;
        else return "";
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUserId() {
        if (userId != null)
            return userId;
        else return "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
