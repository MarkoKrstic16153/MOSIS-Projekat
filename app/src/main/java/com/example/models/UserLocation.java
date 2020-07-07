package com.example.models;

public class UserLocation {
    public double lat;
    public double longi;
    public UserLocation(){

    }

    public UserLocation(double lat, double longi) {
        this.lat = lat;
        this.longi = longi;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    @Override
    public String toString() {
        return "lat=" + lat + ", longi=" + longi;
    }
}
