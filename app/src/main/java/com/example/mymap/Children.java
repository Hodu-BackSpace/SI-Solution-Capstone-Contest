package com.example.mymap;

public class Children {
    private double latitude;
    private double longitude;
    private String location;

    protected  void setLocation(String location){this.location = location;}
    protected void setLatitude(double latitude){
        this.latitude = latitude;
    }
    protected void  setLongitude(double Longitude){
        this.longitude = Longitude;
    }

    protected String getLocation(){return  location;}
    protected double getLatitude(){
        return latitude;
    }
    protected double  getLongitude(){
        return longitude;
    }




}
