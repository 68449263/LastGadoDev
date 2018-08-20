package com.example.user.lastgadodev;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import android.support.design.widget.BottomSheetBehavior;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.lastgadodev.Models.GeoTrain;
import com.example.user.lastgadodev.data.RouteLatLngData;
import com.example.user.lastgadodev.data.StationsData;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

// Butter knife
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, Runnable {

    //TODO : LOAD OTHER VIEWS ON SLASH SCREEN TO FREE WORK HANDLED BY MAIN ACTIVITY, E.g. GET THE USERS LOCATION FROM THERE, CHECK FOR CONNECTIONS, PERMISSIONS ETC.
    public GoogleMap geoMap;
    public Button StartTrainSearchFragmentButton;
    private Button ToggleBottomSheetState;
    public Button ButtonCancelTracking;
    public static StationsData stations_data = new StationsData();
    public static RouteLatLngData routeLatLngData = new RouteLatLngData();

    //internal animation testing variables and properties
    public final Handler mHandler = new Handler();
    public static int TRAIN_SPEED = 1500;
    private static final int CAMERA_ANIMATE_SPEED = 1000;
    private static final int BEARING_OFFSET = 20;
    private final Interpolator interpolator = new LinearInterpolator();
    public static int currentIndex = 0;
    float tilt = 90; //Todo : try to get tilt from user gesture
    long start = SystemClock.uptimeMillis();
    public LatLng endLatLng = null;
    public LatLng beginLatLng = null;

    boolean showPolyline = false;
    public Marker TrackingGeoMarker;
    private Circle TrackingGeoCircle;
    public boolean TrackingMarkerIsAnimating = false;

    public int selectedMarkerOnMap;// holds the index of clicked marker

    public static List<LatLng> latlngPoints = new ArrayList();
    //---------------------------------------------------

    //-------------For resolving nearest point and current index of Marker on map------------
    public static TreeMap<Double, LatLng> GeoTreeMap = new TreeMap<>();
    public static ArrayList<Double> DoubleDuplicatesDetector = new ArrayList<>();
    public static LinkedHashMap<Double, LatLng> GeoLinkedHashMap = new LinkedHashMap<>();
    // used to hold hashValues generated from LatLng
    public static Set<Double> GeoHashValueSet = new HashSet<>();

    public static String ResolvedRoute;

    //----------------------------------------------------------------------------------------

    //---- Butter knife binding
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;

    //---- Bottom sheet ------
    BottomSheetBehavior sheetBehavior;

    //floating prev nxt stations labels
    CardView prevNxtStation;

    //username floating label
    CardView userNameFloatingLabel;

    //---- Floating action button, shows map properties dialog when clicked
    FloatingActionButton ShowMapPropertiesFAB;


    Dialog geoRideDialog;
    Dialog geoMapProperties;

    //Geofire
    GeoFire geoFire;
    GeoQuery geoQuery;

    //Collection of Markers
    private HashMap<String, Marker> TrainMakers = new HashMap<>();
    ArrayList<String> TrainIDs = new ArrayList<>();
    //holds all the geotrains objects from the firebase database
    ArrayList<GeoTrain> geoTrainsList = new ArrayList<>();

    private boolean mapReady = false;
    boolean activityStart;

    //possible route
    public String possibleRoute;

    //next station
    public int StationTracker = 1;

    public FragMarkerInfo MarkerInfo;
    public GeoTrain clickedTrain;

    public static TextView previousStationValue;
    public static TextView nextStationValue;
    public TextView BottomSheetDepartureValue;
    public TextView BottomSheetDestinationValue;
    public TextView BottomSheetTrainIdValue;

    public BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //allows new makers to be placed on the map
        activityStart = true;
        TrainIDs.clear();
        geoTrainsList.clear();
        clearCachedData();

        /* Get the SupportMapFragment and request notification
           when the map is ready to be used. */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Routes/Leralla_to_Germiston/Train_IDs/");

        /*Todo:Geofire  ????? review the effect of this reference,
          try to set reference to tracking/selected marker.
          don't forget to remove it on database after completion
        */
        geoFire = new GeoFire(reference);

        ArrayList<GeoTrain> geoTrainsList = new ArrayList<>();
        geoTrainsList.clear();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    onLocationChangeUpdater(dataSnapshot);

                } else {
                    Toast.makeText(getApplicationContext(), "Database Error!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Terminating Process!", Toast.LENGTH_SHORT).show();
            }
        });

        //sets animator interface to this activity
        // animator.TrainPositions(this);

        AdditionalSetup(); //sets up buttons, fragments etc, and their event listeners
    }

    private void AdditionalSetup() {

        //Bottom navigation
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.map);

        prevNxtStation = findViewById(R.id.previousNxtStopID);
        userNameFloatingLabel = findViewById(R.id.username_card_label);
        userNameFloatingLabel.setVisibility(View.VISIBLE);

        //plus - minus image button for expanding and collapsing bottom sheet
        ToggleBottomSheetState = findViewById(R.id.toggleBottomSheetState);
        ToggleBottomSheetState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ToggleBottomSheetState.getText().toString().equalsIgnoreCase("Collapse")) {

                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {

                    if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }

            }
        });

        ButtonCancelTracking = findViewById(R.id.cancelTracking);
        ButtonCancelTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();

            }
        });

        StartTrainSearchFragmentButton = findViewById(R.id.StartButton);
        StartTrainSearchFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showBottomSheetFragment();

            }
        });

        //first clears hash map then Load stations to their routes
        stations_data.clearStationsFromHashMap();
        stations_data.LoadStationsToRoutes();
        //--

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        bottomsheetcallback();
        //---initially hiding the bottom sheet that shows more info when the tracking marker is moving
        //---We assume that the app wont start by animating the marker,
        //---unless if the app was launched via the push notification button click
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        // Setup the FAB button
        initializeFAB();
        //more marker info dialog
        geoRideDialog = new Dialog(this);
        geoMapProperties = new Dialog(this);

        previousStationValue = findViewById(R.id.previousStationValueTV);
        nextStationValue = findViewById(R.id.nextStationValueTV);
        BottomSheetDepartureValue = findViewById(R.id.BottomSheetdepatureTV);
        BottomSheetDestinationValue = findViewById(R.id.BottomSheetdestinationTV);
        BottomSheetTrainIdValue = findViewById(R.id.bottomSheetTrainId);

    }

    //Bottom navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.map:

                    return true;

                case R.id.updates:

                    showBottomSheetDialog();

                    return false;

                case R.id.profile:

                    showBottomSheetProfile();

                    return false;

                case R.id.free_geo_taxi_ride:

                    ShowFreeTaxiRideDialog();

                    return false;

                case R.id.main_manu:
                    showMenuDialog();
                    return false;
            }

            return false;
        }
    };

    private void showMenuDialog() {

        final View dialogView = View.inflate(this,R.layout.menu_dialog,null);

        final Dialog dialog = new Dialog(this,R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        ImageView imageView = dialog.findViewById(R.id.closeMenuDialog);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                animateDialogOnShow(dialogView, false, dialog);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                animateDialogOnShow(dialogView, true, null);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {

                if (i == KeyEvent.KEYCODE_BACK){

                    animateDialogOnShow(dialogView, false, dialog);
                    return true;
                }
                return false;
            }

        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    private void animateDialogOnShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.dialog);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(h, w);

        int cx = (int) (navigation.findViewById(R.id.main_manu).getX() + (userNameFloatingLabel.getWidth()/2));
        int cy = (int) (navigation.findViewById(R.id.main_manu).getY())+ (userNameFloatingLabel.getHeight() + 56);


        if(b){
            android.animation.Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx,cy, 0, endRadius);

            revealAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    view.setVisibility(View.VISIBLE);
                }
            });

            revealAnimator.setDuration(1500);
            revealAnimator.start();

        } else {

            android.animation.Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(700);
            anim.start();
        }

    }


    public void showBottomSheetDialog() {

        View view = getLayoutInflater().inflate(R.layout.frag_search_trains, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    public void showBottomSheetProfile() {

        View view = getLayoutInflater().inflate(R.layout.frag_profile, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    //free geo local taxi ride
    public void ShowFreeTaxiRideDialog() {

        Window window = geoRideDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        TextView txtclose;
        geoRideDialog.setContentView(R.layout.frag_geo_ride);
        txtclose = geoRideDialog.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoRideDialog.dismiss();
            }
        });

        Objects.requireNonNull(geoRideDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        geoRideDialog.show();
    }

    public void ShowMapPropertiesDialog(){

        Window window = geoMapProperties.getWindow();
        window.setGravity(Gravity.CENTER);
        TextView txtclose;
        geoMapProperties.setContentView(R.layout.map_properties);
        txtclose = geoMapProperties.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoMapProperties.dismiss();
            }
        });

        Objects.requireNonNull(geoMapProperties.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        geoMapProperties.show();

    }

    // setting  up the FAB buttons and its actions
    public void initializeFAB() {

        ShowMapPropertiesFAB = findViewById(R.id.fab);

        ShowMapPropertiesFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShowMapPropertiesDialog();
                //toggleStyle(); Todo toggle style from the pop up
            }
        });

    }

    // calls the bottom sheet modal that is used to search for a train
    private void showBottomSheetFragment() {

        FragSearchTrains ModalBottomSheet = new FragSearchTrains();
        ModalBottomSheet.show(getSupportFragmentManager(), "Search_for_train_tag");
    }


    // Bottom sheet callback
    private void bottomsheetcallback() {

        // bottom sheet state change listener

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN: {

                        prevNxtStation.setVisibility(View.GONE);
                        userNameFloatingLabel.setVisibility(View.VISIBLE);
                        //this condition ensures the visibility of bottom sheet when the marker is animating
                        if (TrackingMarkerIsAnimating) {

                            toggleBottomSheet();
                            userNameFloatingLabel.setVisibility(View.GONE);
                            prevNxtStation.setVisibility(View.VISIBLE);

                        }
                    }
                    break;
                    case BottomSheetBehavior.STATE_EXPANDED: {

                        ToggleBottomSheetState.setBackgroundResource(R.drawable.ic_close);

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        ToggleBottomSheetState.setBackgroundResource(R.drawable.ic_plus);
                        if (TrackingMarkerIsAnimating) {

                            userNameFloatingLabel.setVisibility(View.GONE);
                            prevNxtStation.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    @Override
    public void onBackPressed() {

        //TODO: handle back press events

    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //Todo: if TrackingMarker was animating continue where it left off
    }

    // calls bottom sheet, only when the tracking marker is in motion, to show more info about the marker
    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        geoMap = googleMap;
        geoMap.setOnMarkerClickListener(this);
        mapReady = true;
        updateGeoMapUI();

        //TODO: make a class for getting user location LatLng, and use emulator to set far away distance to check the effect
        //move the camera to the users nearby station
        LatLng userLocation = new LatLng(-26.00584, 28.24866);
        geoMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        googleMap.setMinZoomPreference(9.9f);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setOnMapClickListener(this);

    }

    private void updateGeoMapUI() {

        List<LatLng> Pathlatlng = new ArrayList();

        for (int x = 0; x < routeLatLngData.PretoriaJohannesburgLatLng[0].length; x++) {

            LatLng latLng = new LatLng(routeLatLngData.PretoriaJohannesburgLatLng[0][x],
                    routeLatLngData.PretoriaJohannesburgLatLng[1][x]);

            Pathlatlng.add(latLng);
        }

        //adds poly line coordinates from johannesburg to Leralla
        for (int x = 0; x < routeLatLngData.LerallaJohannesburgLatLng[0].length; x++) {

            LatLng latLng = new LatLng(routeLatLngData.LerallaJohannesburgLatLng[0][routeLatLngData.LerallaJohannesburgLatLng[0].length - (x + 1)],
                    routeLatLngData.LerallaJohannesburgLatLng[1][routeLatLngData.LerallaJohannesburgLatLng[0].length - (x + 1)]);

            Pathlatlng.add(latLng);

        }

        //TODO : Add a thin green line from Departure to Destination. when tracking marker is active
        // adds a thin YELLOW line on train path


        for (int p = 0; p < Pathlatlng.size() - 1; p++) {

            geoMap.addPolyline(new PolylineOptions()
                    .add(Pathlatlng.get(p), Pathlatlng.get(p + 1))
                    .width(8)
                    .color(Color.rgb(255, 187, 0)));

        }


        // Adding shaded circles to all stations
        for (int i = 0; i < stations_data.Stations.length; i++) {

            geoMap.addCircle(new CircleOptions()
                    .center(stations_data.Stations[i])
                    .radius(300)
                    .strokeWidth(15)
                    .strokeColor(Color.rgb(255, 187, 0))
                    .fillColor(Color.rgb(245, 245, 239))
                    .clickable(true));
            //light blue Color.argb(128, 55, 174, 240)
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {

        //TODO: ON MAP Click get the location from the LatLng parameter above, and search for train that will pass by the location
    }


    public void onLocationChangeUpdater(DataSnapshot snapshot) {
        //Only manipulate the map, when it is ready else it will throw an exception
        if (mapReady) {

            if (snapshot != null) {

                if (activityStart) {

                    for (DataSnapshot Trainsnapshot : snapshot.getChildren()) {

                        final GeoTrain geoTrain = Trainsnapshot.getValue(GeoTrain.class);
                        geoTrainsList.add(geoTrain);

                        final LatLng latLng = new LatLng(geoTrain.getCurrent_latitude(), geoTrain.getCurrent_longitude());

                        geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.5f);
                        geoQueryListener(geoQuery);

                        System.out.println(latLng);

                        Marker geoMarker = geoMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(geoTrain.getTrain_id())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon1)));

                        //add the trains in to the markers hash map.
                        TrainMakers.put(geoTrain.getTrain_id(), geoMarker);
                        //Add the device ID so we can navigate, show data about different devices later. or search by train ID
                        TrainIDs.add(geoTrain.getTrain_id());
                    }

                    //prevents the app from reloading all markers (we only want to update markers that changed position)
                    activityStart = false;

                } else {
                    //Since markers are already added, we just update their location and info
                    for (DataSnapshot TrainSnapshot : snapshot.getChildren()) {

                        final GeoTrain geoTrain = TrainSnapshot.getValue(GeoTrain.class);
                        System.out.println("--------------------Updated GeoTrains---------------------");
                        System.out.println(geoTrain);
                        final LatLng latLng = new LatLng(geoTrain.getCurrent_latitude(), geoTrain.getCurrent_longitude());


                        //update the hash map data
                        if (TrainMakers.containsKey(geoTrain.getTrain_id())) {

                            System.out.println("--------------------Key was Found-----------");

                            // FOR DEBBUGING PURPOSES can be removed
                            //current position of a marker before it is updated
                            Marker currentMaker = TrainMakers.get(geoTrain.getTrain_id());
                            //current LatLng for marker (CPFMaker)
                            LatLng CPFMaker = new LatLng(currentMaker.getPosition().latitude, currentMaker.getPosition().longitude);
                            System.out.println("-------Current Position---------");
                            System.out.println(CPFMaker.latitude);
                            //---

                            final Marker newTrain = TrainMakers.get(geoTrain.getTrain_id());
                            newTrain.setPosition(new LatLng(geoTrain.getCurrent_latitude(), geoTrain.getCurrent_longitude()));
                            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.5f);
                            geoQueryListener(geoQuery);

                        } else {

                            //Todo : write a function to update the hash map with the newly added train
                            //A new Train was added on the Database, therefore we should update our hash map
                            System.out.println("--------------------New Train was added to hash map-----------");
                            //add the trains in to the markers hashmap.
                            //TrainMakers.put(geoTrain.getTrain_id(), );
                        }
                    }
                }


            } else {

                Toast.makeText(getApplicationContext(), "No data found on server", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(getApplicationContext(), "Map is not ready yet. Please wait...", Toast.LENGTH_LONG).show();
        }

    }

    //sets the location to next station of a selected (tracking/animating) marker
    private void UpdateGeoFireLocation(LatLng geoPos, String station) {

        //TODO: remove the previous location before setting new one
        geoQuery = geoFire.queryAtLocation(new GeoLocation(geoPos.latitude, geoPos.longitude), 0.1f); //100 meters
        // geoQueryListener(geoQuery);

    }

    private void geoQueryListener(GeoQuery query) {

        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                sendNotification("GeoMetro", String.format("%s station", key));
                //TODO: DISPLAY Ad
            }

            @Override
            public void onKeyExited(String key) {

                sendNotification("GeoMetro", String.format("%s exited station", key));
                if (TrackingMarkerIsAnimating) {
                    StationTracker++; // moves to next station within a route
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                sendNotification("GeoMetro", String.format("%s exited station", key));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("GeoQuery has loaded and fired all other events for initial data");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                Log.e("Error", "" + error);
            }
        });
    }

    private void sendNotification(String title, String content) {

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_geonotification)
                .setContentTitle(title)
                .setContentText(content);
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(), notification);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        for (int x = 0; x < TrainMakers.keySet().size(); x++) {

            if (marker.equals(TrainMakers.get(TrainIDs.get(x)))) {
                System.out.println("---------clicked Train marker--------");
                System.out.println(TrainMakers.get(TrainIDs.get(x)));

                clickedTrain = geoTrainsList.get(x);

                //update the clicked maker index
                //Todo : if tracking marker is animating don't update the bottom sheet values to a non animating marker that is clicked.
                selectedMarkerOnMap = x;
                BottomSheetDepartureValue.setText(clickedTrain.getDeparture());
                BottomSheetDestinationValue.setText(clickedTrain.getDestination());
                BottomSheetTrainIdValue.setText(clickedTrain.getTrain_id());

                MarkerInfo = new FragMarkerInfo();

                Bundle bundle = new Bundle();
                bundle.putSerializable("geoTrain", clickedTrain);
                MarkerInfo.setArguments(bundle);
                MarkerInfo.show(getSupportFragmentManager(), "Maker_Info");

                //todo: find a better way to do this
                //try to check if the clicked train is still in the same route, if true don't call this function again
                //because the route is still the same and we only update the index
                LoadLatLngForClickedHistoryItem(clickedTrain.getRoute_Info());

                //Todo : if bottom dialog takes time to reveal, do this when the user clicks on view train button in (MarkerInfoFrag)
                // else keep it here, but you'll have to reset the current index if the user doesn't click on view train button
                LatLng trainIndex = new LatLng(clickedTrain.getCurrent_latitude(), clickedTrain.getCurrent_longitude());
                double formedHashValue = trainIndex.latitude * -trainIndex.longitude;
                getClosestPoint(formedHashValue);
                //Todo----------------------------------------------------------------------

            }
        }

        return true;
    }

    public void preparePathForRoute(String route){

        LoadLatLngToArrayList(route);
    }

    // executed when the history list item is clicked, for debugging it also executes when marker is clicked on map
    public String LoadLatLngForClickedHistoryItem(String route) {

        System.out.println("================Loaded Route=================");
        System.out.println(route);
        System.out.println("=============================================");
        if (route.equalsIgnoreCase("Pretoria_to_Johannesburg")) {

            LoadLatLngToArrayList("Pretoria_to_Johannesburg");

        }

        if (route.equalsIgnoreCase("Leralla_to_Johannesburg")) {

            LoadLatLngToArrayList("Leralla_to_Johannesburg");

        }

        return route;
    }

    public void UpdatePrevNxtStationValues(LatLng beginLatLng, LatLng endLatLng, final int index) {

        // UpdateGeoFireLocation(endLatLng, "station"); Todo : geoQuery with parameters of this function
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                previousStationValue.setText(stations_data.StationNames[index]);
                nextStationValue.setText(stations_data.StationNames[index + 1]);
            }
        }, 2000);
    }


    public void startAnimation(boolean showPolyLineToDestination) {
        if (latlngPoints.size() > 2) {
            initialize(showPolyLineToDestination);
            //Todo:don't forget to change the state to false, on destination reached or animation stopped
            TrackingMarkerIsAnimating = true;
        }
    }

    public void initialize(boolean showPolyLineToDestination) {

        reset();
        if (showPolyLineToDestination) {

            //Todo: show polyline from departure to destination
        }

        LatLng markerPos1 = latlngPoints.get(currentIndex);
        LatLng markerPos2 = latlngPoints.get(currentIndex + 1);
        setupCameraPositionForMovement(markerPos1, markerPos2);
    }

    private void setupCameraPositionForMovement(LatLng markerPos, LatLng secondPos) {
        float bearing = bearingBetweenLatLngs(markerPos, secondPos);

        TrackingGeoMarker = TrainMakers.get(TrainIDs.get(selectedMarkerOnMap));

        TrackingGeoCircle = geoMap.addCircle(new CircleOptions()
                .center(markerPos)
                .radius(150)
                .strokeWidth(6)
                .strokeColor(Color.rgb(255, 204, 0))
                .fillColor(Color.argb(128, 212, 212, 212))
                .clickable(true));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(markerPos)
                .bearing(bearing + BEARING_OFFSET)
                .tilt(90)
                .zoom(geoMap.getCameraPosition().zoom >= 15 ? geoMap.getCameraPosition().zoom : 15)
                .build();

        geoMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                CAMERA_ANIMATE_SPEED,
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {

                        //Todo : destination is reached or tracking is cancelled, do something
                        reset();
                        Handler handler = new Handler();
                        handler.post(MainActivity.this);
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("cancelling camera animation");
                    }
                });
    }

    //Todo: review this function
    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    public float bearingBetweenLatLngs(LatLng begin, LatLng end) {
        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);
        return beginL.bearingTo(endL);
    }

    @Override
    public void run() {

        long elapsed = SystemClock.uptimeMillis() - start;
        double t = interpolator.getInterpolation((float) elapsed / TRAIN_SPEED);
        Log.w("interpolator", t + "");
        double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
        double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
        LatLng newPosition = new LatLng(lat, lng);
        Log.w("newPosition", newPosition + "");

        TrackingGeoCircle.setCenter(newPosition);
        TrackingGeoMarker.setPosition(newPosition);

        if (showPolyline) {
            // Todo: updatePolyLine(newPosition), if the departure to destination is highlighted with a different color
        }

        if (t < 1) {
            mHandler.postDelayed(this, 16);
        } else {
            System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + latlngPoints.size());

            if (currentIndex < latlngPoints.size() - 2) {

                currentIndex++;
                animateCamera();

            } else {

                String route = "Johannesburg_to_Leralla"; //Todo: swipe around departure and destination if train returns to initial station
                changeRoute(route);
            }

        }
    }

    private void changeRoute(String route) {
        //Todo: add the sleep function until the train departs
        LoadLatLngToArrayList(route);

        //Todo : sleep until next departure time unless if data changed somehow, then start animation
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        currentIndex = 2;
        startAnimation(true);
    }

    public void animateCamera() {

        endLatLng = getEndLatLng();
        beginLatLng = getBeginLatLng();

        start = SystemClock.uptimeMillis();
        LatLng begin = getBeginLatLng();
        LatLng end = getEndLatLng();

        float bearingL = bearingBetweenLatLngs(begin, end);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(end)
                .bearing(bearingL + BEARING_OFFSET)
                .tilt(tilt)
                .zoom(geoMap.getCameraPosition().zoom)
                .build();

        geoMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                CAMERA_ANIMATE_SPEED,
                null
        );

        start = SystemClock.uptimeMillis();
        mHandler.postDelayed(this, 16);
    }

    public LatLng getEndLatLng() {

        //Todo: take this loop logic to its own separate function to be called every time we delay train due to station stop
        for (int x = 0; x < stations_data.Stations.length; x++) {

            if (latlngPoints.get(currentIndex).latitude == stations_data.Stations[x].latitude || latlngPoints.get(currentIndex).longitude == stations_data.Stations[x].longitude) {

                final int index = x;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        nextStationValue.setText(stations_data.StationNames[index + 1]);
                    }
                }, 2000);

                //updates the floating cardView of previous next station labels
                UpdatePrevNxtStationValues(endLatLng, beginLatLng, x);

                try {
                    TimeUnit.SECONDS.sleep(6);
                    //    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return latlngPoints.get(currentIndex + 1);
    }

    public LatLng getBeginLatLng() {

        return latlngPoints.get(currentIndex);
    }

    //-------------------------------- very important make use of it ------------------------------------
    public void stop() {

        mHandler.removeCallbacks(this);
        TrackingMarkerIsAnimating = false;

        //TODO: review this functionality efficiency
        StartTrainSearchFragmentButton.setVisibility(View.VISIBLE);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        latlngPoints.clear();
        currentIndex = 0;  //Todo: if animating a moving train already, set the current index to its current latlng

    }

    public void reset() {
        // on reset, resume where the Marker was Todo: make it work, it stuck where it ended and begins from scratch
        currentIndex = currentIndex + 1; //Todo: resolve the current index of the selected marker
        start = SystemClock.uptimeMillis();
        endLatLng = getEndLatLng();
        beginLatLng = getBeginLatLng();
    }

    //toggles map style when you click on the FAB mini button
    public void toggleStyle() {
        if (GoogleMap.MAP_TYPE_NORMAL == geoMap.getMapType()) {
            geoMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            geoMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public String LoadLatLngToArrayList(String route) {

        double possibleRoute[][];

        switch (route) {

            case "Leralla_to_Johannesburg":

                ResolvedRoute = "Leralla_to_Johannesburg";
                clearCachedData();
                possibleRoute = routeLatLngData.LerallaJohannesburgLatLng;
                for (int x = 0; x < possibleRoute[0].length; x++) {

                    LatLng latLng = new LatLng(possibleRoute[0][x], possibleRoute[1][x]);

                    upDateDuplicatesDetectorArray(latLng);
                    latlngPoints.add(latLng);
                }

                upDateLinkedHashMap();

                break;

            case "Pretoria_to_Johannesburg":

                ResolvedRoute = "Pretoria_to_Johannesburg";
                clearCachedData();
                possibleRoute = routeLatLngData.PretoriaJohannesburgLatLng;
                for (int x = 0; x < possibleRoute[0].length; x++) {

                    LatLng latLng = new LatLng(possibleRoute[0][x], possibleRoute[1][x]);
                    upDateDuplicatesDetectorArray(latLng);
                    latlngPoints.add(latLng);
                }

                upDateLinkedHashMap();

                break;

            case "Johannesburg_to_Leralla":

                ResolvedRoute = "Johannesburg_to_Leralla";
                clearCachedData();
                possibleRoute = routeLatLngData.LerallaJohannesburgLatLng;
                for (int x = possibleRoute[0].length - 1; x >= 0; x--) {

                    LatLng latLng = new LatLng(possibleRoute[0][x], possibleRoute[1][x]);

                    upDateDuplicatesDetectorArray(latLng);
                    latlngPoints.add(latLng);
                }

                upDateLinkedHashMap();

                break;

            default:

                return route;
        }

        return route;

    }

    public static void upDateDuplicatesDetectorArray(LatLng coordinates) {

        DecimalFormat df = new DecimalFormat("00.0000000000000");
        df.setRoundingMode(RoundingMode.CEILING);

        String hashkey = df.format(coordinates.latitude * -coordinates.longitude);

        double hashKeyDoubleValue = Double.parseDouble(hashkey);

        //double type values
        DoubleDuplicatesDetector.add(hashKeyDoubleValue);
        GeoHashValueSet.add(hashKeyDoubleValue);

        System.out.println(hashKeyDoubleValue + " === " + GeoHashValueSet.contains(hashKeyDoubleValue));
    }

    public static void upDateLinkedHashMap() {

        double Applicable_RouteLatLng[][];

        if (ResolvedRoute.equalsIgnoreCase("Leralla_to_Johannesburg")) {

            Applicable_RouteLatLng = routeLatLngData.LerallaJohannesburgLatLng;

        } else {

            Applicable_RouteLatLng = routeLatLngData.PretoriaJohannesburgLatLng;
        }

        for (int i = 0; i < DoubleDuplicatesDetector.size(); i++) {

            LatLng currentCoordinates = new LatLng(Applicable_RouteLatLng[0][i], Applicable_RouteLatLng[1][i]);

            if (GeoLinkedHashMap.containsKey(DoubleDuplicatesDetector.get(i))) {

                GeoLinkedHashMap.put(DoubleDuplicatesDetector.get(i), currentCoordinates);
            } else {
                GeoLinkedHashMap.put(DoubleDuplicatesDetector.get(i), currentCoordinates);
            }

            System.out.println(GeoLinkedHashMap.get(DoubleDuplicatesDetector.get(i)));
        }

        GeoTreeMap.putAll(GeoLinkedHashMap);
    }

    public static void getClosestPoint(Double hashValue) {

        double key = hashValue;

        Map.Entry<Double, LatLng> low = GeoTreeMap.floorEntry(key);
        Map.Entry<Double, LatLng> high = GeoTreeMap.ceilingEntry(key);

        LatLng res;

        if (low != null && high != null) {
            res = Math.abs(key - low.getKey()) < Math.abs(key - high.getKey())
                    ? low.getValue()
                    : high.getValue();

            System.out.println("===============Input================");
            System.out.println(key);
            System.out.println("========Compared Values============");
            System.out.println(low.getKey());
            System.out.println(high.getKey());
            System.out.println("=============Closest Value Coordinates==========");
            System.out.println("Latitude = " + res.latitude + "  Longitude = " + res.longitude);

            System.out.println("==============Index============");
            LatLng markerIndex = new LatLng(res.latitude, res.longitude);
            System.out.println(DoubleDuplicatesDetector.indexOf(markerIndex.latitude * -markerIndex.longitude));

            currentIndex = DoubleDuplicatesDetector.indexOf(markerIndex.latitude * -markerIndex.longitude);

        } else {
            if (low != null || high != null) {
                res = low != null ? low.getValue() : high.getValue();
                System.out.println("=============Closest Value==========");
                System.out.println(res);
            }
        }

    }

    public static void clearCachedData() {

        latlngPoints.clear();
        DoubleDuplicatesDetector.clear();
        GeoLinkedHashMap.clear();
        GeoHashValueSet.clear();

    }

}

//Todo : add stations to array list with respect to their route and keep iterating through them when the initially implemented
//Todo .... conditions are true. eg if the station is found after for loop iteration, we increase or decrease the station index
//Todo .... this could possibly solve the initial station issue and open more flexible functionality
                      /*      if (geoTrain.getAvailability().equalsIgnoreCase("Faulty")) {

                            //sets a red icon for faulty trains
                            geoMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(geoTrain.getDeparture() + " to " + geoTrain.getDestination())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_faulty_train)));
                        } else {

                            if (geoTrain.getAvailability().equalsIgnoreCase("Canceled")) {

                                // sets icon for canceled trains
                                geoMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(geoTrain.getDeparture() + " to " + geoTrain.getDestination())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_canceled_train)));

                            } else {

                                //sets a normal yellow icon for active trains excluding business express
                                geoMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(geoTrain.getDeparture() + " to " + geoTrain.getDestination())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon1)));

                            }
                        }*/


