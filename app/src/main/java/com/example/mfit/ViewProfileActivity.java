package com.example.mfit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.models.Token;
import com.example.models.User;
import com.example.myplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewProfileActivity extends AppCompatActivity {

    static int NEW_PLACE;
    public static final long ONE_MEGABYTE=1024*1024;
    StorageReference storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        storage= FirebaseStorage.getInstance().getReference();

        Button okBtn = (Button)findViewById(R.id.ok_button);
        TextView fullNameText = (TextView)findViewById(R.id.profile_full_name);
        TextView pointsText = (TextView)findViewById(R.id.points);
        TextView addedTokensText = (TextView)findViewById(R.id.added_tokens);
        TextView friendsNumberText = (TextView)findViewById(R.id.friends_number);
        TextView mailText=(TextView)findViewById(R.id.profile_email);
        TextView birthDateText=(TextView)findViewById(R.id.profile_date_of_birth);
        final ImageView profileImage = (ImageView)findViewById(R.id.profile_image);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        User user = null;
        try{
            Intent listIntent = getIntent();
            Bundle dataBundle = listIntent.getExtras();
            user = (User) dataBundle.get("user");
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            finish();
        }
        if(user != null){
            storage.child("users").child(user.uID).child("profile").getBytes(5*ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    byte[] data = task.getResult();
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    profileImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, profileImage.getWidth(),
                            profileImage.getHeight(), false));
                }
            });
            fullNameText.setText(user.FullName());
            pointsText.setText(user.points + "");
            addedTokensText.setText(user.tokensPlaced + "");
            friendsNumberText.setText(user.friends.size()-1 + "");
            mailText.setText(user.email);
            birthDateText.setText(user.birthDate);

        }

    }


}
