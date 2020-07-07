package com.example.mfit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.models.FriendRequestHashMap;
import com.example.models.User;
import com.example.models.UserLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class AllUsersData {

    public static final long ONE_MEGABYTE=1024*1024;
   public ArrayList<User> allUsers=new ArrayList<User>();
   public FriendRequestHashMap friendRequestsMap=new FriendRequestHashMap();
   public ArrayList<User> friendRequests = new ArrayList<User>();
   public ArrayList<User> usersFriends = new ArrayList<User>();
   public HashMap<String,UserLocation> userLocations = new HashMap<String,UserLocation>();
   public HashMap<String,Bitmap> friendImages = new HashMap<>();
   public User currUser = new User();
   public boolean sendMyLocation = true;
    private DatabaseReference database;
    private FirebaseAuth auth;
    StorageReference storage;
    private static final String FIREBASE_CHILD="users";
    public AllUsersData(){
        auth=FirebaseAuth.getInstance();
        this.allUsers = new ArrayList<User>();
        database= FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        resetListeners();
        updateAllUsers();
    }

    public void resetListeners(){

        database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
        database.child("requests").addChildEventListener(childEventListener1);

    }

    public void updateAllUsers(){
        allUsers.clear();
        Query q = database.child(FIREBASE_CHILD).orderByChild("points");
        q.addChildEventListener(childEventListener);
    }

    public ArrayList<User> findUsersFriends(){
        ArrayList<User> lista = new ArrayList<>();
        for(int i=0;i<allUsers.size();i++){
            if(currUser.friends.containsValue(allUsers.get(i).uID)){
                lista.add(allUsers.get(i));
            }
        }
        usersFriends = lista;
        database.child("locations").addChildEventListener(childEventListener2);
        for(int i=0;i<usersFriends.size();i++){
            final String s = usersFriends.get(i).uID;
            storage.child("users").child(s).child("profile").getBytes(5*ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    byte[] data = task.getResult();
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap scaledBmp = bmp.createScaledBitmap(bmp,100,100,false);
                    friendImages.put(s,scaledBmp);
                }
            });
        }

        return lista;
    }

    ValueEventListener parentEventListener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    ChildEventListener childEventListener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String userId = dataSnapshot.getKey();
            User user = dataSnapshot.getValue(User.class);
            user.uID = userId;
            if(userId.equals(auth.getCurrentUser().getUid())){
                currUser = user;
            }
            allUsers.add(0,user);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String userId = dataSnapshot.getKey();
            User user = dataSnapshot.getValue(User.class);
            user.uID = userId;
            for(int i=0;i<allUsers.size();i++){
                if(allUsers.get(i).uID.equals(userId)) {
                    allUsers.set(i,user);
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener1=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())){
                friendRequestsMap=dataSnapshot.getValue(FriendRequestHashMap.class);
                    for(int i =0; i < allUsers.size(); i++) {
                        if(friendRequestsMap.nizZahteva.containsValue(allUsers.get(i).uID)){
                            friendRequests.add(allUsers.get(i));
                        }
                    }
                    }

        }


        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())){
                friendRequestsMap=dataSnapshot.getValue(FriendRequestHashMap.class);
                for(int i =0; i < allUsers.size(); i++) {
                    if(friendRequestsMap.nizZahteva.containsValue(allUsers.get(i).uID)){
                        friendRequests.add(allUsers.get(i));
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener2=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if(!dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())){
                userLocations.put(dataSnapshot.getKey(),dataSnapshot.getValue(UserLocation.class));
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if(dataSnapshot.getKey().equals(auth.getCurrentUser().getUid())){
                friendRequestsMap=dataSnapshot.getValue(FriendRequestHashMap.class);
                for(int i =0; i < allUsers.size(); i++) {
                    if(friendRequestsMap.nizZahteva.containsValue(allUsers.get(i).uID)){
                        friendRequests.add(allUsers.get(i));
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public ArrayList<User> filterSuggestions(){
        ArrayList<User> suggestions=new ArrayList<>();
        for (int i=0;i<allUsers.size();i++){
            if(!auth.getCurrentUser().getUid().equals(allUsers.get(i).uID)){
                boolean flag = false;
                boolean flag1 = false;
                for(int j=0; j<friendRequests.size(); j++){
                    if(allUsers.get(i).uID.equals(friendRequests.get(j).uID)){
                        flag = true;
                    }
                }
                for(int z=0; z<usersFriends.size(); z++){
                    if(allUsers.get(i).uID.equals(usersFriends.get(z).uID)){
                        flag1 = true;
                    }
                }
                if(flag == false && flag1 == false){
                    suggestions.add(allUsers.get(i));
                }
            }
        }
        return suggestions;
    }

    public HashMap<String,UserLocation> getFriendsLocations(){
        HashMap<String,UserLocation> retMap = new HashMap<>();
        for(int i=0;i<usersFriends.size();i++){
            retMap.put(usersFriends.get(i).uID,userLocations.get(usersFriends.get(i).uID));
        }
        return retMap;
    }

    public User getFriend(String friendId) {
        for(int i=0;i<usersFriends.size();i++){
            if(usersFriends.get(i).uID.equals(friendId))
                return usersFriends.get(i);
        }
        return null;
    }

    private  static class SingletonHolder{
        private static final AllUsersData instance = new AllUsersData();
    }

    public static AllUsersData getInstance(){
        return  AllUsersData.SingletonHolder.instance;
    }

}
