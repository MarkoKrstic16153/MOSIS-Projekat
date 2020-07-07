package com.example.mfit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.models.FriendRequestHashMap;
import com.example.models.User;
import com.example.myplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    //region Members
    CircleImageView profileImage;
    public static final long ONE_MEGABYTE=1024*1024;
    TextView fullName,email,date,points,friendsNumber,tokensPlaced;
    StorageReference storage;
    DatabaseReference database;
    FirebaseAuth auth;
    Button logOutBtn;
    CheckBox locationServiceCheckBox;
    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
       profileImage=findViewById(R.id.profile_image);
        fullName =findViewById(R.id.profile_full_name);
        email=findViewById(R.id.profile_email);
        logOutBtn=findViewById(R.id.logOutBtn);
        date=findViewById(R.id.profile_date_of_birth);
        points=findViewById(R.id.points);
        friendsNumber=findViewById(R.id.friends_number);
        tokensPlaced=findViewById(R.id.added_tokens);
        locationServiceCheckBox = findViewById(R.id.chlocation);
        locationServiceCheckBox.setChecked(AllUsersData.getInstance().sendMyLocation);
        locationServiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AllUsersData.getInstance().sendMyLocation = isChecked;
            }
        });
        storage=FirebaseStorage.getInstance().getReference();
        database=FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        //endregion

        //region Storage
        storage.child("users").child(auth.getCurrentUser().getUid()).child("profile").getBytes(5*ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                byte[] data = task.getResult();
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                profileImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, profileImage.getWidth(),
                        profileImage.getHeight(), false));
            }
        });
        //endregion*/

        //region Database
        database.child("users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user_data=dataSnapshot.getValue(User.class);
                fullName.setText(user_data.FullName());
                email.setText(user_data.email);
                date.setText(user_data.birthDate);
                points.setText(user_data.points+"");
               tokensPlaced.setText(user_data.tokensPlaced+"");
               friendsNumber.setText((user_data.friends.size()-1)+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //endregion*/

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllUsersData.getInstance().friendRequests = new ArrayList<>();
                AllUsersData.getInstance().friendRequestsMap = new FriendRequestHashMap();
                AllUsersData.getInstance().usersFriends = new ArrayList<>();
              auth.signOut();
              Toast.makeText(ProfileActivity.this,"Please wait, Logging out...", Toast.LENGTH_LONG).show();
              // Intent intent=new Intent(ProfileActivity.this,LoginActivity.class);
               // startActivity(intent);
                //finish();
                //
                Runnable runnable = new Runnable() {
                    public void run() {
                        stopService(new Intent(ProfileActivity.this, LocationService.class));
                        finishAffinity();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        //System.exit(0);
                    }
                };
                Handler handler = new android.os.Handler();
                handler.postDelayed(runnable, 2000);

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.about){
            Intent i = new Intent(this,About.class);
            startActivity(i);
        }
        else if (id == R.id.add_friend){
            Intent i = new Intent(this, AddFriend.class);
            finish();
            startActivity(i);
        }
        else if (id == R.id.show_map){
            Intent i = new Intent(this, Map.class);
            i.putExtra("state", Map.SHOW_MAP);
            finish();
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}

