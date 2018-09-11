package com.example.user.lastgadodev;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.lastgadodev.HTTPCalls.GetDataFromFirebase;
import com.example.user.lastgadodev.Models.GeoTrain;
import com.example.user.lastgadodev.adapters.AutoCompleteTextInputAdapter;
import com.example.user.lastgadodev.adapters.SearchHistoryListAdapter;
import com.example.user.lastgadodev.adapters.SearchResultsListAdapter;
import com.example.user.lastgadodev.data.StationsData;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class FragSearchTrains extends BottomSheetDialogFragment implements SearchResultsListAdapter.OnItemClickListener, SearchHistoryListAdapter.OnItemClickListener {

    AutoCompleteTextView DepartureACTV;
    AutoCompleteTextView DestinationACTV;
    TextView possibleRoute;
    String headerText = "Home";
    String possible_route;
    DatabaseReference reference;
    FirebaseDatabase database;
    CardView searchFieldsCard;
    //declared to set onClick listener on it
    LinearLayout showSearchHistoryLayout;

    public RecyclerView GeoTrainsRecyclerView;
    public RecyclerView GeoSearchHistory;
    private SearchResultsListAdapter mSearchResultsAdapter;
    private SearchHistoryListAdapter searchHistoryListAdapter;
    ArrayList<GeoTrain> geoTrainsList = new ArrayList<>();
    public List<String> SearchedPossibleRoutes = new ArrayList<>();
    public List<String> Leralla_to_JHB_and_SubRoutes = new ArrayList<>();
    public List<String> Pretoria_to_JHB_and_SubRoutes = new ArrayList<>();

    //GeoTrains arrays in specific route and sub-routes
    public List<GeoTrain> GeoTrains_Leralla_to_Johannesburg = new ArrayList<>();
    public List<GeoTrain> GeoTrains_Leralla_to_Germiston = new ArrayList<>();
    public List<GeoTrain> GeoTrains_Leralla_to_Elandsfontein = new ArrayList<>();

    public List<GeoTrain> GeoTrains_Pretoria_to_Johannesburg = new ArrayList<>();
    public List<GeoTrain> GeoTrains_Pretoria_to_Germiston = new ArrayList<>();
    public List<GeoTrain> GeoTrains_Pretoria_to_Elandsfontein = new ArrayList<>();

    //route history hash map
    public LinkedHashMap<String, String> RouteHash = new LinkedHashMap<>();

    public StationsData stationsData = new StationsData();

    //sectioned recycler view properties
    private SectionedRecyclerViewAdapter sectionAdapter;
    public RecyclerView recyclerView;

    public FragSearchTrains() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_search_trains, container, false);

        searchFieldsCard = view.findViewById(R.id.searchFields);
        searchFieldsCard.setVisibility(View.VISIBLE);

        possibleRoute = view.findViewById(R.id.history_tv);
        GeoTrainsRecyclerView = view.findViewById(R.id.RVTrain_results_list);
        mSearchResultsAdapter = new SearchResultsListAdapter();
        mSearchResultsAdapter.setItemsOnClickListener(this);
        GeoTrainsRecyclerView.setAdapter(mSearchResultsAdapter);
        GeoTrainsRecyclerView.setHasFixedSize(true);
        GeoTrainsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));


        GeoSearchHistory = view.findViewById(R.id.Geo_Search_History);
        searchHistoryListAdapter = new SearchHistoryListAdapter();
        searchHistoryListAdapter.setItemsOnClickListener(this);
        GeoSearchHistory.setAdapter(searchHistoryListAdapter);
        GeoSearchHistory.setHasFixedSize(true);
        GeoSearchHistory.setLayoutManager(new LinearLayoutManager(view.getContext()));
        GeoSearchHistory.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.RVTrain_results_list);
        showSearchHistoryLayout = view.findViewById(R.id.ShowSearchHistoryLayout);
        showSearchHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeoSearchHistory.setVisibility(View.VISIBLE);
            }
        });

        setupSearchHistory();
        prepareSubRoutes();

        new GetDataFromFirebase().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//restart this process using activity as reference

        database = FirebaseDatabase.getInstance();


        initSimpleAutoCompleteTextView(view);

        initCustomAutoCompleteTextView(view);

        final CircularProgressButton button = view.findViewById(R.id.search_trains);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (DepartureACTV.getText().toString().equalsIgnoreCase("")||  DestinationACTV.getText().toString().equalsIgnoreCase("")){

                    Toast.makeText(getContext(),
                            String.format("Insert all values."),
                            Toast.LENGTH_SHORT).show();

                }else {
                    button.startAnimation();
                    //clears the current route path that is stored
                    ((MainActivity) getActivity()).latlngPoints.clear();
                    //gets the possible route based on user inputs
                    FindPossibleRoute();
                }

                    button.stopAnimation();

            }
        });

        DestinationACTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (DepartureACTV.getText().toString().equalsIgnoreCase("")||  DestinationACTV.getText().toString().equalsIgnoreCase("")){

                        Toast.makeText(getContext(),
                                String.format("Insert all values."),
                                Toast.LENGTH_SHORT).show();

                    }else {
                        button.startAnimation();
                        //clears the current route path that is stored
                        ((MainActivity) getActivity()).latlngPoints.clear();
                        //gets the possible route based on user inputs
                        FindPossibleRoute();
                    }

                    button.stopAnimation();
                    hideSoftKeyBoard();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(DestinationACTV.getWindowToken(), 0);
        }
    }


    private void prepareSubRoutes() {

        Leralla_to_JHB_and_SubRoutes.add("Leralla_to_Elandsfontein");//sub-route
        Leralla_to_JHB_and_SubRoutes.add("Leralla_to_Germiston");//sub-route
        Leralla_to_JHB_and_SubRoutes.add("Leralla_to_Johannesburg");//main-route

        Pretoria_to_JHB_and_SubRoutes.add("Pretoria_to_Elandsfontein");//sub-route
        Pretoria_to_JHB_and_SubRoutes.add("Pretoria_to_Germiston");//sub-route
        Pretoria_to_JHB_and_SubRoutes.add("Pretoria_to_Johannesburg");//main-route
    }

    private void setupSearchHistory() {

        //TODO: Make a different class that will handle all the history interactions, e.g filter etc

        //TODO: get the values from user searched routes, on production release
        SearchedPossibleRoutes.add("Leralla_to_Johannesburg");
        SearchedPossibleRoutes.add("Leralla_to_Elandsfontein");
        SearchedPossibleRoutes.add("Leralla_to_Germiston");

        SearchedPossibleRoutes.add("Pretoria_to_Johannesburg");
        SearchedPossibleRoutes.add("Pretoria_to_Germiston");
        SearchedPossibleRoutes.add("Pretoria_to_Elandsfontein");

/*        RouteHash.put("Leralla to Johannesburg","Leralla_to_Johannesburg");
        RouteHash.put("Pretoria to Johannesburg","Pretoria_to_Johannesburg");
        SearchedPossibleRoutes = RouteHash.keySet();*/

/*        SearchedPossibleRoutes.add("Leralla_to_Isando");
        SearchedPossibleRoutes.add("Leralla_to_KemptonPark");
        SearchedPossibleRoutes.add("Pretoria_to_Johannesburg");
        SearchedPossibleRoutes.add("Pretoria_to_Germiston");
        SearchedPossibleRoutes.add("Pretoria_to_Elandsfontein");*/
        searchHistoryListAdapter.populateSearchHistory(SearchedPossibleRoutes);

    }


    private void FindPossibleRoute() {

        String departure = DepartureACTV.getText().toString();
        String destination = DestinationACTV.getText().toString();

        possible_route = stationsData.ResolveRoute(departure, destination);
        ((MainActivity) getActivity()).preparePathForRoute(possible_route);

        //Queries Firebase Database
        getAvailableTrains(possible_route);
    }

    private void getAvailableTrains(final String possible_route) {

        //todo: this is only used for MTN awards to work with one route instead, remove it once everything is resolved accordingly
       final String RouteForMTN = "Leralla_to_Germiston";//use possible route afterwards release

        reference = database.getReference("Routes/" + RouteForMTN + "/Train_IDs/");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    prepareListOfAvailableTrains(dataSnapshot);
                    //hides the text input fields when the results are returned from the database
                    searchFieldsCard.setVisibility(View.GONE);

                } else {

                    //Todo : data snapshot is never null, only the value is null. therefore handle the value instead of the entire snapshot
                    System.out.println("-----------------Datasnapshot has null results-------------------");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void prepareListOfAvailableTrains(DataSnapshot dataSnapshot) {

        for (DataSnapshot Trainsnapshot : dataSnapshot.getChildren()) {

            final GeoTrain geoTrain = Trainsnapshot.getValue(GeoTrain.class);
            System.out.println("-------------GeoTrains id---------------");
            System.out.println(geoTrain.getTrain_id());

            geoTrainsList.add(geoTrain);
        }

        //here we update the all the geoTrainLists with the latest data that we just pulled from the Database
      //  ((MainActivity) getActivity()).geoTrainsList = geoTrainsList;

        sortTrainsByRoute();
        setupSectionedRecyclerView();

        // mSearchResultsAdapter.swapData(geoTrainsList); Todo:uncomment on error with sections
    }

    //sort trains according to their routes by adding them to their route array list
    private void sortTrainsByRoute() {

        for (int x = 0; x < geoTrainsList.size(); x++) {

            GeoTrain train = geoTrainsList.get(x);

            String currentTrainRoute = train.getRoute_Info();

            if(train.getDeparture() == null){


                System.out.println("Route is null for this train");

            }else {

                if (currentTrainRoute.equalsIgnoreCase("Leralla_to_Johannesburg")) {
                    GeoTrains_Leralla_to_Johannesburg.add(train);
                } else {

                    if (currentTrainRoute.equalsIgnoreCase("Leralla_to_Germiston")) {
                        GeoTrains_Leralla_to_Germiston.add(train);
                    } else {

                        if (currentTrainRoute.equalsIgnoreCase("Leralla_to_Elandsfontein")) {

                            GeoTrains_Leralla_to_Elandsfontein.add(train);
                        } else {

                            GeoTrains_Leralla_to_Johannesburg.add(train);
                        }
                    }
                }

            }


        }
    }

    private void initSimpleAutoCompleteTextView(View view) {

        String[] DepartureStation = getResources().getStringArray(R.array.StationNames);
        DepartureACTV = view.findViewById(R.id.DepartureStationET);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, DepartureStation);
        DepartureACTV.setAdapter(adapter);
        DepartureACTV.setThreshold(1);


    }

    private void initCustomAutoCompleteTextView(View view) {
        String[] DestinationStation = getResources().getStringArray(R.array.StationNames);

        DestinationACTV = view.findViewById(R.id.DestinationStationET);

        // custom adapter with the custom header text in the last parameter
        AutoCompleteTextInputAdapter adapter = new AutoCompleteTextInputAdapter(view.getContext(), android.R.layout.simple_list_item_1, DestinationStation, headerText);

        // custom click listener interface
        adapter.setOnHeaderClickListener(new AutoCompleteTextInputAdapter.OnHeaderClickListener() {
            @Override
            public void onHeaderClicked() {
                DestinationACTV.dismissDropDown();
                DestinationACTV.setText(headerText, false);
                DestinationACTV.setSelection(headerText.length());
            }
        });

        DestinationACTV.setThreshold(0);
        DestinationACTV.setAdapter(adapter);

    }

    //TODO: Remove this function its useless since it doesn't resolve the Train index
    @Override
    public void onClick(View view, int pos) {

        System.out.println("=================Route==========");
        System.out.println(SearchedPossibleRoutes.get(pos));
        TrainItemClicked(pos);
    }
    //TODO: Remove this function and its override they are useless since they don't resolve the Train index
    //executes when the list item that shows available trains is clicked
    public void TrainItemClicked(int clickedRoute) {

        //todo:fix this glitch
        ((MainActivity) getActivity()).LoadLatLngToArrayList(SearchedPossibleRoutes.get(clickedRoute).toString());
        //---
        ((MainActivity) getActivity()).startAnimation(true);
        ((MainActivity) getActivity()).toggleBottomSheet();
        ((MainActivity) getActivity()).StartTrainSearchFragmentButton.setVisibility(View.GONE);
        dismiss();

    }
    //todo remove ------------------------------------------------------------------------------------------

    @Override
    public void onClickedHistory(View view, int pos) {

        possible_route = ((MainActivity) getActivity()).LoadLatLngToArrayList(SearchedPossibleRoutes.get(pos).toString());

        //Todo : handle the subroutes properly before you query the database and filter results by exact selected route

        if (Leralla_to_JHB_and_SubRoutes.contains(possible_route)) {

            //Todo : resolve the departure and destination values. e.g Limindlela to isando result in Leralla to JHB and its subroutes
            //TODO...therefore use dep to des to draw poly line and highlight stations

            switch (possible_route) {

                case "Leralla_to_Elandsfontein":
                    getAvailableTrains("Leralla_to_Johannesburg");
                    break;
                case "Leralla_to_Germiston":
                    getAvailableTrains("Leralla_to_Johannesburg");
                    break;
                case "Leralla_to_Johannesburg":
                    getAvailableTrains("Leralla_to_Johannesburg");
                    break;
                default:
                    return;
            }
        }

        if (Pretoria_to_JHB_and_SubRoutes.contains(possible_route)) {

            switch (possible_route) {

                case "Pretoria_to_Elandsfontein":
                    getAvailableTrains("Pretoria_to_Johannesburg");
                    break;
                case "Pretoria_to_Germiston":
                    getAvailableTrains("Pretoria_to_Johannesburg");
                    break;
                case "Pretoria_to_Johannesburg":
                    getAvailableTrains("Pretoria_to_Johannesburg");
                    break;
                default:
                    return;
            }


        }

    }

    //---------------------------------------------sectioned recycler view---------------------------------------

    private void setupSectionedRecyclerView() {

        sectionAdapter = new SectionedRecyclerViewAdapter();

        sectionAdapter.addSection(new RouteSection("Leralla to Johannesburg", GeoTrains_Leralla_to_Johannesburg));
        sectionAdapter.addSection(new RouteSection("Leralla to Germiston", GeoTrains_Leralla_to_Germiston));
        sectionAdapter.addSection(new RouteSection("Leralla to Elandsfontein", GeoTrains_Leralla_to_Elandsfontein));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sectionAdapter);
    }

    public class RouteSection extends StatelessSection {

        String title;
        List<GeoTrain> list;

        RouteSection(String title, List<GeoTrain> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.search_results_list_item2)
                    .headerResourceId(R.layout.section_header)
                    .build());

            this.title = title;
            this.list = list;
        }

        @Override
        public int getContentItemsTotal() {
            return list.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            GeoTrain train = list.get(position);

            itemHolder.mTrain_id_tv.setText(train.getTrain_id());
            itemHolder.mSpeed_tv.setText(Double.toString(train.getCurrent_Speed()) + " km/h");
            itemHolder.Travel_State_tv.setText(train.getTravel_State());
            itemHolder.Route_Info_tv.setText(String.format("to %s",train.getDestination()));
            itemHolder.NextStation_tv.setText(train.getNextStation());
            itemHolder.BtnViewLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = sectionAdapter.getPositionInSection(itemHolder.getAdapterPosition());
                    GeoTrain selectedTrain = geoTrainsList.get(position);
                    //here we try to get the train index on the map
                    List<String> trainIDsAsKeys = new ArrayList<>();
                    trainIDsAsKeys.addAll(((MainActivity) getActivity()).TrainMarkers.keySet());

                    for (int x = 0; x <trainIDsAsKeys.size(); x++) {

                        if (trainIDsAsKeys.get(x) == selectedTrain.getTrain_id()) { position = x; }

                    }

                   //here we make sure that the index doesn't get out of bounds
                    if (position == 0){
                        position = 1;
                    }
                    //here we subtract 1, because array starts to count from 0
                    GeoTrain TrainOnMap = geoTrainsList.get(position);

                    ((MainActivity) getActivity()).prepareUIforAnimation(TrainOnMap,position,true);
                    dismiss();

                }
            });

            //itemHolder.imgItem.setImageResource(name.hashCode() % 2 == 0 ? R.drawable.envelope : R.drawable.calendar);
            itemHolder.BtnMoreInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),
                            String.format("There's no info."),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        HeaderViewHolder(View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        public final View rootView;
        public final TextView mTrain_id_tv;
        public final TextView mSpeed_tv;
        public final TextView Travel_State_tv;
        public final TextView Route_Info_tv;
        public final TextView NextStation_tv;
        public final Button BtnMoreInfo;
        public final Button BtnViewLocation;

        ItemViewHolder(View view) {
            super(view);

            rootView = view;
            mTrain_id_tv = view.findViewById(R.id.train_id);
            mSpeed_tv = view.findViewById(R.id.speed_tv);
            BtnMoreInfo = view.findViewById(R.id.starttracking);
            Travel_State_tv = view.findViewById(R.id.travel_state);
            Route_Info_tv = view.findViewById(R.id.route_info);
            BtnViewLocation = view.findViewById(R.id.btn_view_loc);
            NextStation_tv = view.findViewById(R.id.LVnextStationValue);
        }
    }
}

//string format example
/*
                    Toast.makeText(getContext(),
                            String.format("Clicked on position #%s of Section %s",
                            sectionAdapter.getPositionInSection(itemHolder.getAdapterPosition()),
                            title),
                            Toast.LENGTH_SHORT).show();*/
