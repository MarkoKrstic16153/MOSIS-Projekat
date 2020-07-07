package com.example.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@IgnoreExtraProperties
public class User implements Serializable {
    @Exclude
    public String uID;
    public String email;
    @Exclude
    public String password;
    public String firstName;
    public String lastName;
    public int points;
    public int tokensPlaced;
    public String birthDate;
    public HashMap<String, String> friends;

    public String FullName(){
        return firstName+" "+lastName;
    }

    public User() {
        this.friends=new HashMap<String, String>();
    }

    public User(String uID, String email, String password, String firstName, String lastName, String birthDate) {
        this.uID = uID;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.points=0;
        this.tokensPlaced=0;
        this.friends=new HashMap<String, String>();
    }

    @Override
    public String toString() {
        return  firstName + " " + lastName + " " + points;
    }
}
