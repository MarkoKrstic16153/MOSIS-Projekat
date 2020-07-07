package com.example.mfit;

import androidx.annotation.NonNull;

import com.example.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CurrentUserData {

    private DatabaseReference database;
    public User currentUser;
    FirebaseAuth auth;

    public CurrentUserData(){
        database= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        database.child("users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(parentEventListener);
    }

    ValueEventListener parentEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String userId = dataSnapshot.getKey();
            User user = dataSnapshot.getValue(User.class);
            user.uID = userId;
            currentUser=user;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private  static class SingletonHolder{
        private static final CurrentUserData instance = new CurrentUserData();
    }

    public static CurrentUserData getInstance(){
        return  CurrentUserData.SingletonHolder.instance;
    }

    public void init(){

    }
}
