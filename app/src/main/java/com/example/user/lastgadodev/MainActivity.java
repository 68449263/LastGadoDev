package com.example.user.lastgadodev;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.example.user.lastgadodev.adapters.SearchHistoryListAdapter;
import com.example.user.lastgadodev.adapters.StationScheduleAdapter;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Butter knife
import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.user.lastgadodev.NetworkChangeReceiver;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, Runnable,StationScheduleAdapter.OnTouchListener {

    public GoogleMap geoMap;
    public Button StartTrainSearchFragmentButton;
    private Button ToggleBottomSheetState;
    public Button ButtonCancelTracking;
    public Button ButtonScheduleNotification;
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
    public static Marker TrackingGeoMarker;
    public static Circle TrackingGeoCircle;
    public boolean TrackingMarkerIsAnimating = false;

    public static int selectedMarkerOnMap;// holds the index of clicked marker

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

    //---- Floating action button, shows map properties pop up when clicked
    FloatingActionButton ShowMapPropertiesFAB;


    Dialog geoRideDialog;
    Dialog geoMapProperties;

    //Geofire
    GeoFire geoFire;
    GeoQuery geoQuery;

    //Collection of Markers
    public LinkedHashMap<String, Marker> TrainMarkers = new LinkedHashMap<>();
    ArrayList<String> TrainIDs = new ArrayList<>();
    //holds all the geotrains objects from the firebase database
    ArrayList<GeoTrain> geoTrainsList = new ArrayList<>();

    private boolean mapReady = false;
    boolean activityStart;

    //possible route
    public String possibleRoute;
    //holds the current route of the tracking marker before it changes to its next scheduled route
    public String previousRouteForTrackingMarker;

    //next station
    public int StationTracker = 1;

    public FragMarkerInfo MarkerInfo;
    public GeoTrain clickedGeoTrain;
    public Marker clickedMarker;

    public static TextView previousStationValue;
    public static TextView nextStationValue;
    public TextView BottomSheetDepartureValue;
    public TextView BottomSheetDestinationValue;
    public TextView BottomSheetTrainIdValue;

    public BottomNavigationView navigation;

    public DatabaseReference reference;

    private BroadcastReceiver mNetworkReceiver;
    private static TextView tv_check_connection;

    //used to schedule station delays
    private ScheduledExecutorService stationDelay;
    private boolean isStationReached = false;

    //station notification schedule
    public RecyclerView GeoStationSchedule;
    public StationScheduleAdapter scheduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        activityStart = true; //allows new makers to be placed on the map
        TrainIDs.clear();
        geoTrainsList.clear();
        clearCachedData();

        SupportMapFragment();
        FireBaseSetup();
        AdditionalSetup(); //sets up buttons, fragments etc, and their event listeners
    }

    private void FireBaseSetup() {

        //firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference("Routes/Leralla_to_Germiston/Train_IDs/");

        geoFire = new GeoFire(reference);

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

    }

    private void SupportMapFragment() {

        /* Get the SupportMapFragment and request notification
           when the map is ready to be used. */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

                stop(false);

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
        //more marker info bottomDialog
        geoRideDialog = new Dialog(this);
        geoMapProperties = new Dialog(this);

        previousStationValue = findViewById(R.id.previousStationValueTV);
        nextStationValue = findViewById(R.id.nextStationValueTV);
        BottomSheetDepartureValue = findViewById(R.id.BottomSheetdepatureTV);
        BottomSheetDestinationValue = findViewById(R.id.BottomSheetdestinationTV);
        BottomSheetTrainIdValue = findViewById(R.id.bottomSheetTrainId);

        tv_check_connection = findViewById(R.id.tv_check_connection);
        mNetworkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

        stationDelay = Executors.newSingleThreadScheduledExecutor();//Todo: clear this if it didn't workout

        //schedule station reached notification
        GeoStationSchedule = findViewById(R.id.RV_Schedule);
        scheduleAdapter = new StationScheduleAdapter();
        scheduleAdapter.setItemsOnClickListener(this);
        GeoStationSchedule.setAdapter(scheduleAdapter);
        GeoStationSchedule.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        GeoStationSchedule.setLayoutManager(layoutManager);
        GeoStationSchedule.setVisibility(View.GONE);

        ButtonScheduleNotification = findViewById(R.id.scheduleButton);
        ButtonScheduleNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GeoStationSchedule.setVisibility(View.VISIBLE);
                prepareStationsToBeScheduled();
            }
        });

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

                    showTrainUpdatesDialog();

                    return false;

                case R.id.profile:

                    showBottomNavProfile();

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

    public void showBottomSheetDialog() {

        View view = getLayoutInflater().inflate(R.layout.frag_search_trains, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    public void showBottomNavProfile() {

        final View view = getLayoutInflater().inflate(R.layout.new_profile, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
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

    public void ShowMapPropertiesDialog() {

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

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                View v = null;
                return v;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {
                View v = null;
                try {

                    // Getting view from the layout file info_window_layout
                    v = getLayoutInflater().inflate(R.layout.marker_info_window, null);

                    TextView depatureTime = v.findViewById(R.id.DepartureTime);
                    depatureTime.setText("Departs at " + clickedGeoTrain.getDeparture_Time());

                    TextView trainIdTv = v.findViewById(R.id.ArrivalTime);
                    trainIdTv.setText("Arrives at " + clickedGeoTrain.getArrival_Time());

                } catch (Exception ev) {
                    System.out.print(ev.getMessage());
                }

                return v;
            }
        });

    }

    @Override
    public void onMapLoaded() {

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
                        TrainMarkers.put(geoTrain.getTrain_id(), geoMarker);
                        //Add the device ID so we can navigate, show data about different devices later. or search by train ID
                        TrainIDs.add(geoTrain.getTrain_id());
                    }

                    //prevents the app from reloading all markers (we only want to update markers that changed position)
                    activityStart = false;
                    //dismisses the progress dialog

                } else {
                    //Since markers are already added, we just update their location and info
                    ArrayList<GeoTrain> NewGeoTrainsList = new ArrayList<>();
                    for (DataSnapshot TrainSnapshot : snapshot.getChildren()) {

                        final GeoTrain geoTrain = TrainSnapshot.getValue(GeoTrain.class);
                        System.out.println("--------------------Updated GeoTrains---------------------");
                        System.out.println(geoTrain);

                        NewGeoTrainsList.add(geoTrain);

                        //update the hash map data
                        if (TrainMarkers.containsKey(geoTrain.getTrain_id())) {

                            System.out.println("--------------The marker exists----------------");

                            final Marker newTrain = TrainMarkers.get(geoTrain.getTrain_id());
                            // newTrain.setPosition(new LatLng(geoTrain.getCurrent_latitude(), geoTrain.getCurrent_longitude()));
                            // geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.5f);
                            //  geoQueryListener(geoQuery);


                        } else {
                            //Todo : write a function to update the hash map with the newly added train
                        }
                    }
                    //updates the existing data with new trains data, useful on getting current route.
                    geoTrainsList = NewGeoTrainsList;
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

        if (TrackingMarkerIsAnimating) {

            Toast.makeText(this,
                    String.format("Not Allowed"),
                    Toast.LENGTH_SHORT).show();

        } else {
            clickedMarker = marker;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker.getPosition())
                    .zoom(geoMap.getCameraPosition().zoom >= 9.9f ? geoMap.getCameraPosition().zoom : 9.9f)
                    .build();

            geoMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            for (int x = 0; x < TrainMarkers.keySet().size(); x++) {

                if (marker.equals(TrainMarkers.get(TrainIDs.get(x)))) {

                    marker.showInfoWindow();

                    clickedGeoTrain = geoTrainsList.get(x);

                    MarkerInfo = new FragMarkerInfo();

                    prepareUIforAnimation(clickedGeoTrain, x, false);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("geoTrain", clickedGeoTrain);
                    MarkerInfo.setArguments(bundle);
                    MarkerInfo.show(getSupportFragmentManager(), "Maker_Info");

                }
            }

        }
        return true;
    }

    public void prepareUIforAnimation(GeoTrain clickedGeoTrain, int x, boolean listItemClick) {

        //we update the local clickedGeoTrain with the one from list item
        this.clickedGeoTrain = clickedGeoTrain;

        //update the clicked maker index
        //Todo : if tracking marker is animating don't update the bottom sheet values to a non animating marker that is clicked.
        selectedMarkerOnMap = x;
        BottomSheetDepartureValue.setText(clickedGeoTrain.getDeparture());
        BottomSheetDestinationValue.setText(clickedGeoTrain.getDestination());
        BottomSheetTrainIdValue.setText(clickedGeoTrain.getTrain_id());

        previousRouteForTrackingMarker = clickedGeoTrain.getRoute_Info();

        //todo: find a better way to do this
        //try to check if the clicked train is still in the same route, if true don't call this function again
        //because the route is still the same and we only update the index
        LoadLatLngToArrayList(clickedGeoTrain.getRoute_Info());

        //Todo : if bottom dialog takes time to reveal, do this when the user clicks on view train button in (MarkerInfoFrag)
        // else keep it here, but you'll have to reset the current index if the user doesn't click on view train button
        LatLng trainIndex = new LatLng(clickedGeoTrain.getCurrent_latitude(), clickedGeoTrain.getCurrent_longitude());
        double formedHashValue = trainIndex.latitude * -trainIndex.longitude;
        getClosestPoint(formedHashValue);
        //Todo----------------------------------------------------------------------

        if (listItemClick) {

            startAnimation(true);
            toggleBottomSheet();
            StartTrainSearchFragmentButton.setVisibility(View.GONE);
        }

    }

    public void preparePathForRoute(String route) {

        LoadLatLngToArrayList(route);
    }

    public void UpdatePrevNxtStationValues(LatLng beginLatLng, LatLng endLatLng, final int index) {

        // UpdateGeoFireLocation(endLatLng, "station"); Todo : geoQuery with parameters of this function
        //Todo: from user schedules check if the coords(parameters) do match the notification station alert
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

        TrackingGeoMarker = TrainMarkers.get(TrainIDs.get(selectedMarkerOnMap));

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

    //Todo: review this function : possibly use it on marker clustering when marker is animating, use another one on stations
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

/*        reference.child(clickedGeoTrain.getTrain_id())
                .child("Current_latitude").setValue(endLatLng.latitude);

        reference.child(clickedGeoTrain.getTrain_id())
                .child("Current_longitude").setValue(endLatLng.longitude);*/

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

                String route = getLatestRouteForCurrentTrain();

                if (route.equalsIgnoreCase(previousRouteForTrackingMarker)) {

                    //todo : reset map with the following modifications:
                    /*
                     * reset tilt to zero
                     * reset zoom level to home screen zoom level*/

                } else {

                    changeRoute(route);
                }

            }

        }
    }

    public void trainReachedStation(int delayTime) {

        stationDelay.schedule(this, delayTime, TimeUnit.SECONDS);

    }

    private void timeToLeaveCurrentStation() {

         startAnimation(true);
    }

    /*
     * returns current route for tracking marker
     * this method is only called when the train reaches dead end station or trains terminal*/
    private String getLatestRouteForCurrentTrain() {

        //todo: query the database directly to get the accurate data
        String route = clickedGeoTrain.getRoute_Info();
        return route;
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

                isStationReached = true;
                //updates the floating cardView of previous next station labels
                UpdatePrevNxtStationValues(endLatLng, beginLatLng, x);

                try {
                    TimeUnit.SECONDS.sleep(3);
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

    public void stop(boolean isStationDelay) {

        if (isStationDelay){

            mHandler.removeCallbacks(this);

        }else {

            mHandler.removeCallbacks(this);
            TrackingMarkerIsAnimating = false;

            //TODO: review this functionality efficiency
            StartTrainSearchFragmentButton.setVisibility(View.VISIBLE);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            latlngPoints.clear();
            //Todo : check is stop() was called due to station delay or not, if true update the index to + 1
            //Todo: write a function to continue animation instead of using startAnimation on station delays
            currentIndex = 0;  //Todo: if animating a moving train already, set the current index to its current latlng

        }
    }

    public void reset() {
        // on reset, resume where the Marker was Todo: make it work, it stuck where it ended and begins from scratch
        currentIndex = currentIndex + 1; //Todo: resolve the current index of the selected marker
        start = SystemClock.uptimeMillis();
        endLatLng = getEndLatLng();
        beginLatLng = getBeginLatLng();
    }

    //toggles map style when you click on the FAB mini button
    public void toggleStyle(String selectedStyle) {
        if (GoogleMap.MAP_TYPE_NORMAL == geoMap.getMapType()) {
            geoMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            geoMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        switch (selectedStyle) {

            case "Default":
                geoMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satellite":
                geoMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Terrain":
                geoMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                geoMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
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
                possibleRoute = routeLatLngData.JohannesburgLerallaLatLng();
                for (int x = 0; x < possibleRoute[0].length; x++) {

                    LatLng latLng = new LatLng(possibleRoute[0][x], possibleRoute[1][x]);

                    upDateDuplicatesDetectorArray(latLng);
                    latlngPoints.add(latLng);
                }

                upDateLinkedHashMap();

                break;

            case "Leralla_to_Germiston"://Todo make it end at germiston

                ResolvedRoute = "Johannesburg_to_Leralla";
                clearCachedData();
                possibleRoute = routeLatLngData.JohannesburgLerallaLatLng();
                for (int x = 0; x < possibleRoute[0].length; x++) {

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

        DoubleDuplicatesDetector.add(hashKeyDoubleValue);
        GeoHashValueSet.add(hashKeyDoubleValue);
    }

    public void upDateLinkedHashMap() {

        double Applicable_RouteLatLng[][] = new double[0][];

        switch (ResolvedRoute) {

            case "Leralla_to_Johannesburg":

                Applicable_RouteLatLng = routeLatLngData.LerallaJohannesburgLatLng;

                break;

            case "Pretoria_to_Johannesburg":

                Applicable_RouteLatLng = routeLatLngData.PretoriaJohannesburgLatLng;

                break;

            case "Johannesburg_to_Leralla":

                Applicable_RouteLatLng = routeLatLngData.JohannesburgLerallaLatLng();

                break;

            case "Johannesburg_to_Pretoria":

                Applicable_RouteLatLng = routeLatLngData.JohannesburgPretoriaLatLng();

                break;

            default:
                Toast.makeText(this, "Database Error!", Toast.LENGTH_SHORT).show();

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

    public void getClosestPoint(Double hashValue) {

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

            List<Double> newList = DoubleDuplicatesDetector;
            int realIndex = closest(key, newList);
            currentIndex = realIndex;

            Double possibleNearestStationHashValue = latlngPoints.get(currentIndex).latitude * -latlngPoints.get(currentIndex).longitude;
            int stationIndex = stations_data.resolveStationIndex(possibleNearestStationHashValue);

            switch (ResolvedRoute) {

                case "Leralla_to_Johannesburg":

                    previousStationValue.setText(stations_data.StationNames[stationIndex]);
                    nextStationValue.setText(stations_data.StationNames[stationIndex + 1]);

                    break;

                case "Pretoria_to_Johannesburg":

                    previousStationValue.setText(stations_data.StationNames[stationIndex]);
                    nextStationValue.setText(stations_data.StationNames[stationIndex + 1]);

                    break;

                case "Johannesburg_to_Leralla":


                    break;

                default:
                    Toast.makeText(this, "Stations Error!", Toast.LENGTH_SHORT).show();
            }

        } else {

            Toast.makeText(this, "Stations Error!", Toast.LENGTH_SHORT).show();
            if (low != null || high != null) {
                res = low != null ? low.getValue() : high.getValue();
                System.out.println("=============Closest Value==========");
                System.out.println(res);
            }
        }

    }

    //finds the closest index value of the input within a give list
    public int closest(Double of, List<Double> in) {
        Double min = Double.MAX_VALUE;
        Double closest = of;

        for (Double v : in) {
            final Double diff = Math.abs(v - of);

            if (diff < min) {
                min = diff;
                closest = v;
            }
        }

        return DoubleDuplicatesDetector.indexOf(closest);
    }

    public static void clearCachedData() {

        latlngPoints.clear();
        DoubleDuplicatesDetector.clear();
        GeoLinkedHashMap.clear();
        GeoHashValueSet.clear();
    }

    private void showTrainUpdatesDialog() {

        final View TrainUpdatesView = View.inflate(this, R.layout.train_updates, null);

        final Dialog updates_dialog = new Dialog(this, R.style.MenuDialogStyle);
        updates_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updates_dialog.setContentView(TrainUpdatesView);

        ImageView imageView = updates_dialog.findViewById(R.id.closeTrainUpdatesDialog);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                animateDialogOnShow(TrainUpdatesView, false, updates_dialog);
            }
        });

        updates_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                animateDialogOnShow(TrainUpdatesView, true, null);
            }
        });

        updates_dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {

                if (i == KeyEvent.KEYCODE_BACK) {

                    animateDialogOnShow(TrainUpdatesView, false, updates_dialog);
                    return true;
                }
                return false;
            }

        });

        updates_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        updates_dialog.show();
    }

    private void showMenuDialog() {

        final View menuView = View.inflate(this, R.layout.menu_dialog, null);

        final Dialog geo_menu = new Dialog(this, R.style.MenuDialogStyle);
        // geo_menu.requestWindowFeature(Window.FEATURE_NO_TITLE);
        geo_menu.setContentView(menuView);

        ImageView imageView = geo_menu.findViewById(R.id.closeMenuDialog);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                animateDialogOnShow(menuView, false, geo_menu);
            }
        });

        geo_menu.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                animateDialogOnShow(menuView, true, null);
            }
        });

        geo_menu.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {

                if (i == KeyEvent.KEYCODE_BACK) {

                    animateDialogOnShow(menuView, false, geo_menu);
                    return true;
                }
                return false;
            }

        });

        geo_menu.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        geo_menu.show();
    }

    private void animateDialogOnShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.dialog);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(h, w);

        int cx = (int) (navigation.findViewById(R.id.main_manu).getX() + (userNameFloatingLabel.getWidth() / 2));
        int cy = (int) (navigation.findViewById(R.id.main_manu).getY()) + (userNameFloatingLabel.getHeight() + 56);


        if (b) {
            android.animation.Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

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

    //network indicator
    public static void NetworkStateIndicator(boolean value, String message) {

        if (value) {
            tv_check_connection.setText(message);
            tv_check_connection.setBackgroundColor(Color.rgb(0, 153, 51));
            tv_check_connection.setTextColor(Color.WHITE);

            Handler handler = new Handler();
            Runnable delayrunnable = new Runnable() {
                @Override
                public void run() {
                    tv_check_connection.setVisibility(View.GONE);
                }
            };
            handler.postDelayed(delayrunnable, 3000);
        } else {
            tv_check_connection.setVisibility(View.VISIBLE);
            tv_check_connection.setText(message);
            tv_check_connection.setBackgroundColor(Color.rgb(204, 0, 0));
            tv_check_connection.setTextColor(Color.WHITE);
        }
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void progressDialog(String message, boolean shouldShow) {

        ProgressDialog pd = new ProgressDialog(this);
        // Set progress dialog style spinner
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Set the progress dialog title and message
        pd.setTitle("Geometrorail");
        pd.setMessage(String.format("%s.....", message));
        // Set the progress dialog background color
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFD4D9D0")));

        Window w = pd.getWindow();
        w.setGravity(Gravity.CENTER);

        pd.setIndeterminate(false);
        pd.show();
    }

    private void prepareStationsToBeScheduled() {

        List<String> stationsNamesList = new ArrayList<>(stations_data.Leralla_to_Johannesburg.keySet());
        List<LatLng> stationsLatLngList = new ArrayList<>(stations_data.Leralla_to_Johannesburg.values());
        scheduleAdapter.populateScheduleList(stationsNamesList);
    }

    @Override
    public void onAddStationNotification(View view, int pos) {



    }

    @Override
    public void onBackPressed() {

        //TODO: handle back press events

    }

    @Override
    protected void onStop() {
        super.onStop();
      //  stop(false); //Todo: handle on resume flag
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //Todo: if TrackingMarker was animating continue where it left off
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
        mHandler.removeCallbacks(this);
    }

}

//Todo: use this function to resume animation after station delay
/*
public void scheduleTimeToLeave() {
    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(App::myTask, 0, 1, TimeUnit.SECONDS);
}

private static void myTask() {
    System.out.println("Running");
}*/