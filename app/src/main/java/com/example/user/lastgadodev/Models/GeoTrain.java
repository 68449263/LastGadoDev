package com.example.user.lastgadodev.Models;

import java.io.Serializable;

public class GeoTrain implements Serializable{

    private String Train_id;
    private String Departure;
    private String Destination;
    private String Departure_Time;
    private String Arrival_Time;
    private double Current_Speed;
    private double Current_latitude;
    private double Current_longitude;
    private String Availability;
    private String Travel_Class;
    private String Travel_State;
    private String Route_Info;

    public GeoTrain(){}

    public GeoTrain(String train_id, String departure, String destination, String departure_Time, String arrival_Time, double current_Speed, double current_latitude, double current_longitude, String availability, String travel_Class, String travel_State, String route_Info) {
        Train_id = train_id;
        Departure = departure;
        Destination = destination;
        Departure_Time = departure_Time;
        Arrival_Time = arrival_Time;
        Current_Speed = current_Speed;
        Current_latitude = current_latitude;
        Current_longitude = current_longitude;
        Availability = availability;
        Travel_Class = travel_Class;
        Travel_State = travel_State;
        Route_Info = route_Info;
    }

    public String getTrain_id() {
        return Train_id;
    }

    public void setTrain_id(String train_id) {
        Train_id = train_id;
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

    public String getDeparture_Time() {
        return Departure_Time;
    }

    public void setDeparture_Time(String departure_Time) {
        Departure_Time = departure_Time;
    }

    public String getArrival_Time() {
        return Arrival_Time;
    }

    public void setArrival_Time(String arrival_Time) {
        Arrival_Time = arrival_Time;
    }

    public double getCurrent_Speed() {
        return Current_Speed;
    }

    public void setCurrent_Speed(double current_Speed) {
        Current_Speed = current_Speed;
    }

    public double getCurrent_latitude() {
        return Current_latitude;
    }

    public void setCurrent_latitude(double current_latitude) {
        Current_latitude = current_latitude;
    }

    public double getCurrent_longitude() {
        return Current_longitude;
    }

    public void setCurrent_longitude(double current_longitude) {
        Current_longitude = current_longitude;
    }

    public String getAvailability() {
        return Availability;
    }

    public void setAvailability(String availability) {
        Availability = availability;
    }

    public String getTravel_Class() {
        return Travel_Class;
    }

    public void setTravel_Class(String travel_Class) {
        Travel_Class = travel_Class;
    }

    public String getTravel_State() {
        return Travel_State;
    }

    public void setTravel_State(String travel_State) {
        Travel_State = travel_State;
    }

    public String getRoute_Info() {
        return Route_Info;
    }

    public void setRoute_Info(String route_Info) {
        Route_Info = route_Info;
    }

}
