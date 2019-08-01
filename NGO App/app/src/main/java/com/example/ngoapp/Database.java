package com.example.ngoapp;

import android.content.Context;

import com.example.ngoapp.DataClass.EventDetails;
import com.example.ngoapp.DataClass.NgoDetails;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static Database instance;

    private Database() {}

    public static Database getInstance() {
        if(instance==null)
            instance = new Database();
        return instance;
    }

//--------------------------------------------------------------------------------------------------//

    private NgoDetails mNgo = null;

    public NgoDetails getNgo() {
        return mNgo;
    }

    public void setNgo(NgoDetails mNgo) {
        this.mNgo = mNgo;
    }

//--------------------------------------------------------------------------------------------------//

    private ArrayList<EventDetails> allEvents;
    private ArrayList<String> allEventsKey;

    public ArrayList<EventDetails> getAllEvents() {
        return allEvents;
    }

    public ArrayList<String> getAllEventsKey() {
        return allEventsKey;
    }

    public void setAllEvents(ArrayList<EventDetails> allEvents, ArrayList<String> allEventsKey) {
        this.allEvents = allEvents;
        this.allEventsKey = allEventsKey;
    }

//--------------------------------------------------------------------------------------------------//

    private EventDetails editEventDetails;
    private String editNgoEventKey;

    public EventDetails getEditEventDetails() {
        return editEventDetails;
    }

    public String getEditNgoEventKey() {
        return editNgoEventKey;
    }

    public void setEditNgoEvent(EventDetails editEventDetails, String editNgoEventKey) {
        this.editEventDetails = editEventDetails;
        this.editNgoEventKey = editNgoEventKey;
    }

//--------------------------------------------------------------------------------------------------//

    public void initialiseUserApp(Context context) {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBtq4NLM9EtvbYZbZF2OsPy4DSELR8eHl8")
                .setApplicationId("com.example.ngoapp")
                .setDatabaseUrl("https://user-details-73186.firebaseio.com/")
                .build();

        boolean hasBeenInitialized=false;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps(context);
        for(FirebaseApp app : firebaseApps){
            if(app.getName().equals("userApp")){
                hasBeenInitialized=true;
            }
        }

        if(hasBeenInitialized==false)
            FirebaseApp.initializeApp(context, options, "userApp");
    }

    public void initialiseNgoApp(Context context) {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBXoG-eZ5p0Y4BtcfaLkjfTqmRHTVPM-ZI")
                .setApplicationId("com.example.ngoapp")
                .setDatabaseUrl("https://ngo-details.firebaseio.com/")
                .build();

        boolean hasBeenInitialized=false;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps(context);
        for(FirebaseApp app : firebaseApps){
            if(app.getName().equals("ngoApp")){
                hasBeenInitialized=true;
            }
        }

        if(hasBeenInitialized==false)
            FirebaseApp.initializeApp(context, options, "ngoApp");
    }
}
