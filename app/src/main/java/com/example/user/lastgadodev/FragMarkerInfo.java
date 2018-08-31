package com.example.user.lastgadodev;


import android.content.DialogInterface;
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
    TextView TravelState;
    Button TrackSelectedMarker;

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

        GeoTrain  clickedTrain = (GeoTrain) data;

        TravelState = view.findViewById(R.id.B_travel_state);
        TrackSelectedMarker = view.findViewById(R.id.TrackSelectedMarkerId);
        departure = view.findViewById(R.id.depatureTV);
        destination = view.findViewById(R.id.destinationTV);
        train_id = view.findViewById(R.id.trainID_TV);
        speed = view.findViewById(R.id.speedTV);

        assert clickedTrain != null;
        departure.setText(clickedTrain.getDeparture());
        destination.setText(clickedTrain.getDestination());
        train_id.setText(clickedTrain.getTrain_id());
        speed.setText(Double.toString(clickedTrain.getCurrent_Speed())+" km/h");

        if(clickedTrain.getTravel_State().equalsIgnoreCase("Express")){

            //TODO: show a list of skipped stations
            TravelState.setText("Express");

        }else {

            TravelState.setText(clickedTrain.getTravel_State());
        }

        TrackSelectedMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity) getActivity()).startAnimation(true);
                ((MainActivity) getActivity()).toggleBottomSheet();
                ((MainActivity) getActivity()).StartTrainSearchFragmentButton.setVisibility(View.GONE);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ((MainActivity) getActivity()).clickedMarker.hideInfoWindow();
        ((MainActivity) getActivity()).clickedMarker = null;
        //Todo: do additional stuff if required
    }
}
