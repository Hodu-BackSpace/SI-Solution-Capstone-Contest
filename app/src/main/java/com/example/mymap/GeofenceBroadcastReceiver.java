package com.example.mymap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";
    public static boolean  IN_AREA = false;//어린이 보호구역에 들어와있는지를 체크하는 변수.
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        int currentArrayRoom = -1;
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
            currentArrayRoom = Integer.parseInt(geofence.getRequestId());
        }

        //Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                IN_AREA = true;
                MapsActivity.setTextViewCurrentLocation(MapsActivity.ChildrenList.get(currentArrayRoom).getLocation());
                Toast.makeText(context, "어린이 보호구역에 진입하였습니다.", Toast.LENGTH_SHORT).show();
                MapsActivity.currentLocation =MapsActivity.ChildrenList.get(currentArrayRoom).getLocation();
                notificationHelper.sendHighPriorityNotification("어린이 보호구역에 들어왔습니다.", MapsActivity.currentLocation+" 존에 들어왔습니다.", MapsActivity.class);
                MapsActivity.startHandler();


                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                //Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("어린이 보호구역에 위치하고있습니다.", "yaflix", MapsActivity.class);
                System.out.println("sdgsdaklgjsadlkgjsadlkjglksadjflksdajlfsjdfpoweiurpoweiproiewpriewop");

            break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                IN_AREA = false;
                Toast.makeText(context, "어린이 보호구역에서 벗어났습니다.", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("어린이 보호구역에서 나왔습니다.", "yaflix", MapsActivity.class);

                MapsActivity.finishHandler();

                break;
        }

    }
}