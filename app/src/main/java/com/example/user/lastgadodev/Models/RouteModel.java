package com.example.user.lastgadodev.Models;

public class RouteModel {

    private String Departure,Destination;

    public RouteModel(String departure, String destination) {
        Departure = departure;
        Destination = destination;
    }

    public String getDeparture() {
        return Departure;
    }

    public void setDeparture(String departure) {
        Departure = departure;
    }

    public String getDestination() {
        return Destination;
    }

    public void setDestination(String destination) {
        Destination = destination;
    }
}
