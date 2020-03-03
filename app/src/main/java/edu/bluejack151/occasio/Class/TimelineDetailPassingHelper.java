package edu.bluejack151.occasio.Class;

import java.io.Serializable;

/**
 * Created by Domus on 12/29/2015.
 */
public class TimelineDetailPassingHelper implements Serializable {
    private TimelineData timelineData;

    public TimelineDetailPassingHelper(TimelineData timelineData) {
        this.timelineData = timelineData;
    }

    public TimelineData getTimelineData() {
        return this.timelineData;
    }
}
