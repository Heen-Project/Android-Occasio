package edu.bluejack151.occasio.Class;

/**
 * Created by Domus on 1/1/2016.
 */
public class CommentData {
    String imageId;
    UserData userData;
    ImageComment imageComment;

    public CommentData() {
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public ImageComment getImageComment() {
        return imageComment;
    }

    public void setImageComment(ImageComment imageComment) {
        this.imageComment = imageComment;
    }
}
