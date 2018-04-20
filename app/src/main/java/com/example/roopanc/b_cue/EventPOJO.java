package com.example.roopanc.b_cue;

/**
 * Created by RadhikaRanganathan on 27/02/2018.
 */

public class EventPOJO {

    String eventID, eventName, eventType, eventDate, addInfo, priority;

    public EventPOJO()
    {
        //required
    }

    public EventPOJO(String eventID, String eventName, String eventType, String eventDate, String addInfo, String priority)
    {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.addInfo = addInfo;
        this.priority = priority;

    }

    public String getEventName() {
        return eventName;
    }

    public String getAddInfo() {
        return addInfo;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventID() {
        return eventID;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPriority() {
        return priority;
    }

}
