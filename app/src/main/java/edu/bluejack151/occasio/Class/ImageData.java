package edu.bluejack151.occasio.Class;

import java.io.Serializable;

/**
 * Created by Domus on 12/24/2015.
 */
public class ImageData implements Serializable {
    String imageId;
    String userId;
    String imageString;
    String caption;
    String timestamp;
    String ppAddress;
    String ppLatLng;
    String ppLatLngLatitude;
    String ppLatLngLongitude;
    String ppName;
    String ppPhoneNumber;
    String ppAttributions;

    public ImageData() {
    }

    public ImageData(String imageId, String userId, String imageString, String caption, String timestamp, String ppAddress, String ppLatLng, String ppLatLngLatitude, String ppLatLngLongitude, String ppName, String ppPhoneNumber, String ppAttributions) {
        this.imageId = imageId;
        this.userId = userId;
        this.imageString = imageString;
        this.caption = caption;
        this.timestamp = timestamp;
        this.ppAddress = ppAddress;
        this.ppLatLng = ppLatLng;
        this.ppLatLngLatitude = ppLatLngLatitude;
        this.ppLatLngLongitude = ppLatLngLongitude;
        this.ppName = ppName;
        this.ppPhoneNumber = ppPhoneNumber;
        this.ppAttributions = ppAttributions;
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

    public String getImageString() {
        if (imageString != null)
            return imageString;
        else return "";
    }

    public void setImageString(String imageString) {
        this.imageString = imageString;
    }

    public String getCaption() {
        if (caption != null)
            return caption;
        else return "";
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTimestamp() {
        if (timestamp != null)
            return timestamp;
        else return "";
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPpAddress() {
        if (ppAddress != null)
            return ppAddress;
        else return "";
    }

    public void setPpAddress(String ppAddress) {
        this.ppAddress = ppAddress;
    }

    public String getPpLatLng() {
        if (ppLatLng != null)
            return ppLatLng;
        else return "";
    }

    public void setPpLatLng(String ppLatLng) {
        this.ppLatLng = ppLatLng;
    }

    public String getPpLatLngLatitude() {
        if (ppLatLngLatitude != null)
            return ppLatLngLatitude;
        else return "";
    }

    public void setPpLatLngLatitude(String ppLatLngLatitude) {
        this.ppLatLngLatitude = ppLatLngLatitude;
    }

    public String getPpLatLngLongitude() {
        if (ppLatLngLongitude != null)
            return ppLatLngLongitude;
        else return "";
    }

    public void setPpLatLngLongitude(String ppLatLngLongitude) {
        this.ppLatLngLongitude = ppLatLngLongitude;
    }

    public String getPpName() {
        if (ppName != null)
            return ppName;
        else return "";
    }

    public void setPpName(String ppName) {
        this.ppName = ppName;
    }

    public String getPpPhoneNumber() {
        if (ppPhoneNumber != null)
            return ppPhoneNumber;
        else return "";
    }

    public void setPpPhoneNumber(String ppPhoneNumber) {
        this.ppPhoneNumber = ppPhoneNumber;
    }

    public String getPpAttributions() {
        if (ppAttributions != null)
            return ppAttributions;
        else return "";
    }

    public void setPpAttributions(String ppAttributions) {
        this.ppAttributions = ppAttributions;
    }
}
