package edu.bluejack151.occasio.Class;

/**
 * Created by Domus on 12/30/2015.
 */
public class ImageComment {
    String commentId;
    String imageId;
    String userCommentatorId;
    String comment;
    String time;

    public ImageComment(String commentId, String imageId, String userCommentatorId, String comment, String time) {
        this.commentId = commentId;
        this.imageId = imageId;
        this.userCommentatorId = userCommentatorId;
        this.comment = comment;
        this.time = time;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setUserCommentatorId(String userCommentatorId) {
        this.userCommentatorId = userCommentatorId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageId() {
        if (imageId != null)
            return imageId;
        else return "";
    }

    public String getUserCommentatorId() {
        if (userCommentatorId != null)
            return userCommentatorId;
        else return "";
    }

    public String getCommentId() {
        if (commentId != null)
            return commentId;
        else return "";
    }

    public String getComment() {
        if (comment != null)
            return comment;
        else return "";
    }

    public String getTime() {
        if (time != null)
            return time;
        else return "";
    }


}
