package com.example.project2;

public class userDetails {

    String username;
    String state;
    String city;
    String address;

    public userDetails(String username,String state,String city,String address) {
        this.username = username;
        this.state = state;
        this.city = city;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }
}
