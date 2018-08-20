package com.example.user.lastgadodev;


import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.example.user.lastgadodev.data.RouteLatLngData;
import com.example.user.lastgadodev.data.StationsData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnimatorOld implements Runnable {

    public GoogleMap geoMap;
    public final Handler mHandler = new Handler();
    public static int TRAIN_SPEED = 1500;
    private static final int CAMERA_ANIMATE_SPEED = 1000;
    private static final int BEARING_OFFSET = 20;
    private final Interpolator interpolator = new LinearInterpolator();
    int currentIndex = 0;
    float tilt = 90; //Todo : try to get tilt from user gesture
    long start = SystemClock.uptimeMillis();
    public LatLng endLatLng = null;
    public LatLng beginLatLng = null;

    boolean showPolyline = false;
    public Marker trackingMarker;
    public Marker TrackingGeoMarker;
    private Circle TrackingGeoCircle;

    public StationsData all_stations_data = new StationsData();

    // used by other classes/Activities to get the state
    public boolean TrackingMarkerIsAnimating = false;

    //route LatLng
    RouteLatLngData routeLatLngData = new RouteLatLngData();

    public List<LatLng> latlngPoints = new ArrayList();

    public interface UpdateTrainStation{
        void TwoLatLng(LatLng beginLatLng , LatLng endLatLng,int index);
    }

    private UpdateTrainStation mLatLngPoints;

    public void TrainPositions(UpdateTrainStation station){
        this.mLatLngPoints = station;
    }

    public void LoadLatLngToArrayList(String route) {

        switch (route) {

            case "Leralla_to_Johannesburg":

                for (int x = 0; x < routeLatLngData.LerallaJohannesburgLatLng[0].length; x++) {

                    LatLng latLng = new LatLng(routeLatLngData.LerallaJohannesburgLatLng[0][x],
                            routeLatLngData.LerallaJohannesburgLatLng[1][x]);

                    latlngPoints.add(latLng);
                }
                break;

            case "Pretoria_to_Johannesburg":

                for (int x = 0; x < routeLatLngData.PretoriaJohannesburgLatLng[0].length; x++) {

                    LatLng latLng = new LatLng(routeLatLngData.PretoriaJohannesburgLatLng[0][x],
                            routeLatLngData.PretoriaJohannesburgLatLng[1][x]);

                    latlngPoints.add(latLng);
                }
                break;
            default:
                return;
        }

        System.out.println("----------------ADDED ROUTE LatLng--------------");
    }

    public void reset() {
        // on reset, resume where the Marker was Todo: make it work, it stuck where it ended and begins from scratch
        currentIndex = currentIndex + 1; //Todo: resolve the current index of the selected marker
        start = SystemClock.uptimeMillis();
        endLatLng = getEndLatLng();
        beginLatLng = getBeginLatLng();
    }

    //-------------------------------- very important make use of it ------------------------------------
    public void stop() {
        mHandler.removeCallbacks(this);
        TrackingMarkerIsAnimating = false;
    }

    public void initialize(boolean showPolyLineToDestination) {
        reset();

        if(showPolyLineToDestination){

           //Todo: show polyline from departure to destination
        }

        LatLng markerPos1 = latlngPoints.get(0);
        LatLng markerPos2 = latlngPoints.get(1);
        setupCameraPositionForMovement(markerPos1, markerPos2);
    }

    private void setupCameraPositionForMovement(LatLng markerPos, LatLng secondPos) {
        float bearing = bearingBetweenLatLngs(markerPos, secondPos);
        trackingMarker = geoMap.addMarker(new MarkerOptions()
                .position(markerPos)
                .title("title")
                .snippet("snippet").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon1)));

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

                        ///Todo: destination is reached / tracking might be cancelled, do something
                        System.out.println("finished camera");
                        Log.e("animator before reset", AnimatorOld.this.toString() + "");
                        reset();
                        Log.e("animator after reset", AnimatorOld.this.toString() + "");
                        Handler handler = new Handler();
                        handler.post(AnimatorOld.this);
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

    //toggles map style when you click on the FAB mini button
    public void toggleStyle() {
        if (GoogleMap.MAP_TYPE_NORMAL == geoMap.getMapType()) {
            geoMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            geoMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void startAnimation(boolean showPolyLineToDestination) {
        if (latlngPoints.size() > 2) {
            initialize(showPolyLineToDestination);
            //Todo:don't forget to change the state to false, on destination reached or animation stopped
            TrackingMarkerIsAnimating = true;
        }
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

     //   trackingMarker.setPosition(newPosition);
     //   TrackingGeoCircle.setCenter(newPosition);

        if (showPolyline) {
            // Todo: updatePolyLine(newPosition), if the departure to destination is highlighted with a different color
        }

        if (t < 1) {
            mHandler.postDelayed(this, 16);
        } else {
            System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + latlngPoints.size());
            if (currentIndex < latlngPoints.size() - 2) {

                currentIndex++;
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

            } else {
                currentIndex++;
            }
        }
    }

    public LatLng getEndLatLng() {

        //Todo: take this loop logic to its own separate function to be called every time we delay train due to station stop
        for (int x = 0; x < all_stations_data.Stations.length; x++) {

            if (latlngPoints.get(currentIndex).latitude == all_stations_data.Stations[x].latitude || latlngPoints.get(currentIndex).longitude == all_stations_data.Stations[x].longitude) {

                System.out.println("===========Current Station=============" + all_stations_data.StationNames[x]);
                mLatLngPoints.TwoLatLng(endLatLng,beginLatLng,x);

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

}
