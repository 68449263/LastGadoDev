package com.example.user.lastgadodev.data;

import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import java.util.HashMap;

//NOTE: all routes lead to/move towards Johannesburg within this DATA Class!

public class StationsData {

    LatLng Pretoria = new LatLng(-25.75954, 28.18902);
    LatLng Fonteine = new LatLng(-25.78395,   28.1932);
    LatLng Kloofsig = new LatLng(-25.81341, 28.20113);
    LatLng Sportpark = new LatLng(-25.82471, 28.20578);
    LatLng Centurion = new LatLng(-25.83489, 28.21085);
    LatLng Irene = new LatLng(-25.87535, 28.22445);
    LatLng Pinedene = new LatLng(-25.91378, 28.22885);
    LatLng Olifantsfontein = new LatLng(-25.96418, 28.23552);
    LatLng Oakmoor = new LatLng(-26.00588, 28.24874);
    LatLng Leralla = new LatLng(-26.02929, 28.19647);
    LatLng Limindlela = new LatLng(-26.01113, 28.2148401);
    LatLng Tembisa = new LatLng(-26.00962, 28.23133);
    LatLng Kaalfontein = new LatLng(-26.0344, 28.25481);
    LatLng Birchleigh = new LatLng(-26.06719, 28.23464);
    LatLng VanRiebeeckPark = new LatLng(-26.08808, 28.22179);
    LatLng KemptonPark = new LatLng(-26.10732, 28.22685);
    LatLng Rhodesfield = new LatLng(-26.12752, 28.22462);
    LatLng Isando = new LatLng(-26.13612, 28.22173);
    LatLng Elandsfontein = new LatLng(-26.16676, 28.20548);
    LatLng Ravensklip = new LatLng(-26.1811, 28.19973);
    LatLng Knights = new LatLng(-26.19775, 28.19573);
    LatLng Germiston = new LatLng(-26.20977, 28.16725);
    LatLng President = new LatLng(-26.21143, 28.1595);
    LatLng Driehoek = new LatLng(-26.21343, 28.14963);
    LatLng Geldenhuis = new LatLng(-26.20806, 28.13293);
    LatLng Cleveland = new LatLng(-26.20852, 28.11839);
    LatLng Tooronga = new LatLng(-26.20421, 28.11051);
    LatLng Denver = new LatLng(-26.20594, 28.0971);
    LatLng GeorgeGoch = new LatLng(-26.20764, 28.08043);
    LatLng Jeppe = new LatLng(-26.204, 28.06345);
    LatLng Ellispark = new LatLng(-26.19895, 28.05904);
    LatLng Doorfotein = new LatLng(-26.19716, 28.0542);
    LatLng Johannesburg = new LatLng(-26.19746, 28.0422);

    // Don't Rearrange the array here! very important
    public LatLng[] Stations = {
            Pretoria,
            Fonteine,
            Kloofsig,
            Sportpark,
            Centurion,
            Irene,
            Pinedene,
            Olifantsfontein,
            Oakmoor,
            Leralla,
            Limindlela,
            Tembisa,
            Kaalfontein,
            Birchleigh,
            VanRiebeeckPark,
            KemptonPark,
            Rhodesfield,
            Isando,
            Elandsfontein,
            Ravensklip,
            Knights,
            Germiston,
            President,
            Driehoek,
            Geldenhuis,
            Cleveland,
            Tooronga,
            Denver,
            GeorgeGoch,
            Jeppe,
            Ellispark,
            Doorfotein,
            Johannesburg
    };

    // Don't Rearrange the array here! very important
    public String[] StationNames = {
            "Pretoria",
            "Fonteine",
            "Kloofsig",
            "Sportpark",
            "Centurion",
            "Irene",
            "Pinedene",
            "Olifantsfontein",
            "Oakmoor",
            "Leralla",
            "Limindlela",
            "Tembisa",
            "Kaalfontein",
            "Birchleigh",
            "VanRiebeeckPark",
            "KemptonPark",
            "Rhodesfield",
            "Isando",
            "Elandsfontein",
            "Ravensklip",
            "Knights",
            "Germiston",
            "President",
            "Driehoek",
            "Geldenhuys",
            "Cleveland",
            "Tooronga",
            "Denver",
            "GeorgeGoch",
            "Jeppe",
            "Ellispark",
            "Doorfotein",
            "Johannesburg"
    };


    public HashMap<String, LatLng> Leralla_to_Johannesburg = new HashMap<>();
    public HashMap<String, LatLng> Leralla_to_Germiston = new HashMap<>();
    public HashMap<String, LatLng> Leralla_to_Elandsfontein = new HashMap<>();

    public HashMap<String, LatLng> Pretoria_to_Johannesburg = new HashMap<>();
    public HashMap<String, LatLng> Pretoria_to_Germiston = new HashMap<>();
    public HashMap<String, LatLng> Pretoria_to_Elandsfontein = new HashMap<>();

    //adds stations to their routes
    public void LoadStationsToRoutes() {

        //there are 8 possible routes according to metrorail map and schedules time table (Johannesburg-Pretoria-Leralla)
        //NOTE: other routes are sub-routes therefore we update them within main route update process
        //this helps when searching user departure to destination inputs
        for (int s = 0; s < 3; s++) {

            System.out.println("--------------Loading Stations to Routes ----------------");
            switch (s) {
                case 0://if the loop is at PRETORIA station (index 0)

                    //PtJ = Pretoria_to_Johannesburg
                    for (int PtJ = 0; PtJ < Stations.length; PtJ++) {

                        //the following station indexes are not within PtJ route,therefore we don't need to add them in PtJ
                        /*
                        * 9 = Leralla
                        * 10 = Limindlela
                        * 11 = Tembisa
                        */
                        if(PtJ == 9 || PtJ == 10 || PtJ == 11 ){

                            System.out.println("-----Station Belongs to a different route");

                        }else {

                            Pretoria_to_Johannesburg.put(StationNames[PtJ], Stations[PtJ]);

                            //if the loop hasn't passed Germiston we add stations to PtG
                            if (PtJ <= 21 ){

                                Pretoria_to_Germiston.put(StationNames[PtJ], Stations[PtJ]);
                            }

                            //if the loop hasn't passed Elandsfontein we add stations to PtE
                            if (PtJ <= 18){

                                Pretoria_to_Elandsfontein.put(StationNames[PtJ], Stations[PtJ]);
                            }

                        }


                    }
                    //for debugging purposes
                    System.out.println("--------------PtJ_STATIONS----------------");
                    System.out.println(Pretoria_to_Johannesburg.keySet().size());
                    System.out.println(Pretoria_to_Johannesburg);
                    System.out.println("--------------PtG_STATIONS----------------");
                    System.out.println(Pretoria_to_Germiston.keySet().size());
                    System.out.println(Pretoria_to_Germiston);
                    System.out.println("--------------PtE_STATIONS----------------");
                    System.out.println(Pretoria_to_Elandsfontein.keySet().size());
                    System.out.println(Pretoria_to_Elandsfontein);


                case 1:

                    //LtJ = Leralla_to_Johannesburg
                    int DeadEndStation = 9; // we update the index to start at Leralla station
                    for (int LtJ = DeadEndStation; LtJ < Stations.length; LtJ++) {

                        Leralla_to_Johannesburg.put(StationNames[LtJ], Stations[LtJ]);

                        //we add stations for Leralla to Germiston
                        if (LtJ <= 21){

                            Leralla_to_Germiston.put(StationNames[LtJ], Stations[LtJ]);
                        }

                        //we add stations for Leralla to Elandsfontein
                        if (LtJ <= 18){

                            Leralla_to_Elandsfontein.put(StationNames[LtJ], Stations[LtJ]);
                        }

                    }
                    //for debugging purposes
                    System.out.println("--------------LtJ_STATIONS----------------");
                    System.out.println(Leralla_to_Johannesburg.keySet().size());
                    System.out.println(Leralla_to_Johannesburg);
                    System.out.println("--------------LtG_STATIONS----------------");
                    System.out.println(Leralla_to_Germiston.keySet().size());
                    System.out.println(Leralla_to_Germiston);
                    System.out.println("--------------LtE_STATIONS----------------");
                    System.out.println(Leralla_to_Elandsfontein.keySet().size());
                    System.out.println(Leralla_to_Elandsfontein);
                    System.out.println("------------------------------------------");

                default:
                    return;
            }
        }

        System.out.println("--------------STATIONS FOR LOOP ENDED----------------");
    }

    public void clearStationsFromHashMap(){

        Leralla_to_Elandsfontein.clear();
        Leralla_to_Germiston.clear();
        Leralla_to_Johannesburg.clear();

        Pretoria_to_Elandsfontein.clear();
        Pretoria_to_Germiston.clear();
        Pretoria_to_Johannesburg.clear();

    }
}
