package com.example.mymap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.minew.beaconset.BluetoothState;
import com.minew.beaconset.ConnectionState;
import com.minew.beaconset.MinewBeacon;
import com.minew.beaconset.MinewBeaconConnection;
import com.minew.beaconset.MinewBeaconConnectionListener;
import com.minew.beaconset.MinewBeaconManager;
import com.minew.beaconset.MinewBeaconManagerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

import app.akexorcist.bluetotohspp.library.DeviceList;

import static com.minew.beaconset.MinewBeaconManager.scannedBeacons;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    TextView t2;
    Button bt1;

    // 감지된 비콘들을 임시로 담을 리스트
    //private static List<Beacon> beaconList = new ArrayList<>();
    private static TextView textView;
    private Switch switch1;
    public static ArrayList<Children> ChildrenList = new ArrayList<Children>();
    public static String currentLocation = null;//현재 위치를 GeofenceBroadcastReceiver
    private static boolean beaconController = false;
    private static final String TAG = "MapsActivity";
    private static final String GEOFENCE_ID = "junzzi";
    private static TextView t1;
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private float GEOFENCE_RADIUS = 100;
    private boolean GeofenceFull = false;
    private BluetoothSPP bt;
    public static MapsActivity temp;
    public static Boolean chk = false;
    public static Ringtone rt;
    public static Context cont;
    public static Boolean alrim;
    public static MinewBeaconManager mMinewBeaconManager;
    public static  List<MinewBeacon> mMinewBeacons = new ArrayList<>();
    public static UserRssi comp = new UserRssi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        temp = this;
        //beaconManager = BeaconManager.getInstanceForApplication(this);

        // 여기가 중요한데, 기기에 따라서 setBeaconLayout 안의 내용을 바꿔줘야 하는듯 싶다.
        // 필자의 경우에는 아래처럼 하니 잘 동작했음.
        /*
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind((BeaconConsumer) temp);
        */


        mMinewBeaconManager = MinewBeaconManager.getInstance(this);



        alrim = false;
        t1 = findViewById(R.id.t1);
        textView = (TextView) findViewById(R.id.textView);
        t1.setText("어린이 보호구역에 위치하지않습니다.");
        textView.setText("어린이 보호구역에 위치하지않습니다.");

        switch1 = findViewById(R.id.switch1);
        String json = getJsonString();
        jsonParsing(json);
        cont = getApplicationContext();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switch1.isChecked()) {
                    switch1.setText("현재 상태 ON");
                    textView.setText("어린이 탐색이 시작되었습니다.");
                    // 비콘 탐지를 시작한다. 실제로는 서비스를 시작하는것.
                   // beaconManager.bind((BeaconConsumer) temp);
                    try {
                        mMinewBeaconManager.startScan();
                    }catch(Error e){

                    }

                    chk= false;
                } else {
                    switch1.setText("현재 상태 OFF");
                    //beaconManager.unbind((BeaconConsumer) temp);
                    mMinewBeaconManager.stopScan();
                    textView.setText("어린이 탐색이 중지되었습니다.");
                    chk = true;


                }
            }
        });

        if (GeofenceFull == false) {
            GeofenceFull = true;
            for (int check = 0; check < 100; check++) {
                addGeofence(Integer.toString(check), new LatLng(ChildrenList.get(check).getLatitude(), ChildrenList.get(check).getLongitude()), GEOFENCE_RADIUS);

            }
        }

    }

    public static void finishHandler() {//EXIT시 호출되는 함수
        beaconController = false;
        textView.setText("어린이 보호구역에 위치하지않습니다.");
    }

    public static void startHandler() {//ENTER시 호출되는 함수
        beaconController = true;
        textView.setText("어린이 보호구역에 위치하지않습니다.");
        if(!chk)
            handler.sendEmptyMessage(0);
        else
            return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public static void setTextViewCurrentLocation(String currentLocation) {
        t1.setText(currentLocation);

    }

    /*
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }
*/


    // 버튼이 클릭되면 textView 에 비콘들의 정보를 뿌린다.
    public void OnButtonClicked(View view) {
        // 아래에 있는 handleMessage를 부르는 함수. 맨 처음에는 0초간격이지만 한번 호출되고 나면
        // 1초마다 불러온다.
        //handler.sendEmptyMessage(0);
    }
    static boolean here = false;
    @SuppressLint("HandlerLeak")
    static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            MinewBeacon mMinewBeacon;
            int i=0,j=0;
            if (beaconController == true) {
                textView.setText("");

                // 비콘의 아이디와 거리를 측정하여 textView에 넣는다.
                for (MinewBeacon beacon : scannedBeacons) {

                    if(beacon.getName().equals("MiniBeacon_34791")){
                        j=i;
                        mMinewBeacon = scannedBeacons.get(j);
                        String format = String.format("Distance: %.3f M",
                                mMinewBeacon.getDistance());
                        //float test = beacon.getDistance();
                        //textView.append(Float.toString(test));
                        textView.append(format);

                        if(beacon.getDistance() >= 30 && beacon.getDistance() < 40) {
                            if(alrim)
                                rt.stop();
                            Uri noti = Uri.parse("android.resource://com.example.mymap/raw/" + R.raw.type_3);
                            rt = RingtoneManager.getRingtone(cont, noti);
                            rt.play();
                            alrim = true;
                        } else if(beacon.getDistance() >= 10 && beacon.getDistance() < 30){
                            if(alrim)
                                rt.stop();
                            Uri noti2 = Uri.parse("android.resource://com.example.mymap/raw/" + R.raw.type_2);
                            rt = RingtoneManager.getRingtone(cont, noti2);
                            rt.play();
                            alrim = true;
                        } else if(beacon.getDistance() >= 0 && beacon.getDistance() < 10){
                            if(alrim)
                                rt.stop();
                            Uri noti3 = Uri.parse("android.resource://com.example.mymap/raw/" + R.raw.type_1);
                            rt = RingtoneManager.getRingtone(cont, noti3);
                            rt.play();
                            alrim = true;
                        } else {
                            rt.stop();
                        }
                        if(chk) {
                            if(alrim)
                                rt.stop();
                            textView.setText("어린이 탐색이 중지되었습니다.");
                        }
                        break;
                    }
                    i++;



                        // TO DO : 거리에 따른 알람 설정




                }

                // 자기 자신을 1초마다 호출
                handler.sendEmptyMessageDelayed(0, 300);
            }

        }
    };


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private String getJsonString() {
        String json = "";

        try {
            InputStream is = getAssets().open("children.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return json;
    }

    private void jsonParsing(String json) {
        try {

            JSONObject jsonObject = new JSONObject(json);

            JSONArray ChildrenArray = jsonObject.getJSONArray("records");

            for (int i = 0; i < ChildrenArray.length(); i++) {
                JSONObject ChildObject = ChildrenArray.getJSONObject(i);

                Children Child = new Children();
                Child.setLocation(ChildObject.getString("location"));
                Child.setLatitude(Double.parseDouble(ChildObject.getString("latitude")));
                Child.setLongitude(Double.parseDouble(ChildObject.getString("longitude")));

                ChildrenList.add(Child);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng myhome = new LatLng(37.6281424, 127.080144);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myhome, 16));
        enableUserLocation();
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //퍼미션 물어보기
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                mMap.setMyLocationEnabled(true);
            } else {
                //We do not have the permission..
                //We do not have the permission..
            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void addGeofence(String ID, LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }

}