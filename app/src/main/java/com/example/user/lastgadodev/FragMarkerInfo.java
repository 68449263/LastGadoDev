package com.example.user.lastgadodev;


import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.user.lastgadodev.Models.GeoTrain;

import java.io.Serializable;


public class FragMarkerInfo extends BottomSheetDialogFragment {

    TextView departure;
    TextView destination;
    TextView train_id;
    TextView speed;
    TextView Travelstate;
    Button TrackSelectedMarker;
    private GeoTrain clickedTrain;

    //holds the train route
   private String Route;



    public FragMarkerInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Serializable data = getArguments().getSerializable("geoTrain");
        System.out.println("--------------data--------------------------------");
        System.out.println(data);
        View view = inflater.inflate(R.layout.frag_marker_info, container, false);

        clickedTrain = (GeoTrain) data;

        Travelstate = view.findViewById(R.id.B_travel_state);
        TrackSelectedMarker = view.findViewById(R.id.TrackSelectedMarkerId);
        departure = view.findViewById(R.id.depatureTV);
        destination = view.findViewById(R.id.destinationTV);
        train_id = view.findViewById(R.id.trainID_TV);
        speed = view.findViewById(R.id.speedTV);

        departure.setText(clickedTrain.getDeparture());
        destination.setText(clickedTrain.getDestination());
        train_id.setText(clickedTrain.getTrain_id());
        speed.setText(Double.toString(clickedTrain.getCurrent_Speed())+" km/h");

        if(clickedTrain.getTravel_State().equalsIgnoreCase("Express")){

            //TODO: show a list of skipped stations
            Travelstate.setText("Show List Of skipped stations");

        }else {

            Travelstate.setText(clickedTrain.getTravel_State());
        }

        TrackSelectedMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(clickedTrain.getDeparture().equalsIgnoreCase("Leralla")){

                    Route = "Leralla_to_Johannesburg";

                }else {

                    Route = "Pretoria_to_Johannesburg";
                }

                System.out.println("==========Resolve My current Index and Start animating==========");
                //clears the current route path LatLng that is stored
               // ((MainActivity) getActivity()).latlngPoints.clear();
                //todo:fix this glitch
               // ((MainActivity) getActivity()).LoadLatLngForClickedHistoryItem(Route);
                //---
                ((MainActivity) getActivity()).startAnimation(true);
                ((MainActivity) getActivity()).toggleBottomSheet();
                ((MainActivity) getActivity()).StartTrainSearchFragmentButton.setVisibility(View.GONE);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //Todo : reset bottom sheet values if the track train button was not pressed. else don't reset the values, the marker is tracking/animating
    }
}
