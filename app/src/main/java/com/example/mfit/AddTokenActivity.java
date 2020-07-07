package com.example.mfit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.models.Intensity;
import com.example.models.Token;
import com.example.models.Type;
import com.example.models.User;
import com.example.myplace.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class AddTokenActivity extends AppCompatActivity {

    boolean editMode = true;
    int position = -1;
    FirebaseAuth auth;
    User user=new User();
    private DatabaseReference database;
    private StorageReference storage;
    private static final String FIREBASE_CHILD="tokens";
    ImageView image;
    Uri imageUri;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String TOKEN_CHILD="tokens";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_token);
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        image=findViewById(R.id.tokenImage);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        try {
            Intent i = getIntent();
            Bundle positionBundle = i.getExtras();
            if(positionBundle != null){
                position = positionBundle.getInt("position");
            }
            else{
                editMode = false;
            }
        }
        catch (Exception e){
            editMode = false;
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        Button addBtn = (Button)findViewById(R.id.add_button);

        RadioButton radGreen = (RadioButton) findViewById(R.id.rb_green);
        RadioButton radBlue = (RadioButton) findViewById(R.id.rb_blue);
        RadioButton radRed = (RadioButton) findViewById(R.id.rb_red);
        radGreen.setChecked(true);

        RadioButton radEasy = (RadioButton) findViewById(R.id.rb_easy);
        RadioButton radMedium = (RadioButton) findViewById(R.id.rb_medium);
        RadioButton radHard = (RadioButton) findViewById(R.id.rb_hard);
        radEasy.setChecked(true);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (MyTokensData.getInstance().currentLocation != null) {
                    Token newToken = new Token();
                    newToken.setUserId(auth.getCurrentUser().getUid());
                    newToken.setLatitude(MyTokensData.getInstance().currentLocation.getLatitude());
                    newToken.setLongitude(MyTokensData.getInstance().currentLocation.getLongitude());
                    newToken.setTokenIntensity(getIntensity());
                    newToken.setTokenType(getType());
                    newToken.setFullName(user.FullName());

                    String key = database.push().getKey();
                    newToken.key = key;

                    database.child(FIREBASE_CHILD).child(key).setValue(newToken);
                    uploadImage(key);
                    Intent i = new Intent(AddTokenActivity.this, Map.class);
                    i.putExtra("state", Map.SHOW_MAP);
                    startActivity(i);
                    finish();
                    database.child("users").child(auth.getCurrentUser().getUid()).child("tokensPlaced").setValue(user.tokensPlaced + 1);
                }
                else{
                    Toast.makeText(AddTokenActivity.this,"Unable to get current location, try again later.", Toast.LENGTH_LONG).show();
                }
            }
        });

        database.child("users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user_data=dataSnapshot.getValue(User.class);
                user=user_data;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private Type getType() {
        RadioButton radGreen = (RadioButton) findViewById(R.id.rb_green);
        RadioButton radBlue = (RadioButton) findViewById(R.id.rb_blue);
        RadioButton radRed = (RadioButton) findViewById(R.id.rb_red);

        if(radGreen.isChecked()){
            return Type.GREEN;
        }
        else if(radBlue.isChecked()){
            return Type.BLUE;
        }
        else if(radRed.isChecked()){
            return Type.RED;
        }
        else {
            return Type.ALL;
        }
    }

    private Intensity getIntensity() {
        RadioButton radEasy = (RadioButton) findViewById(R.id.rb_easy);
        RadioButton radMedium = (RadioButton) findViewById(R.id.rb_medium);
        RadioButton radHard = (RadioButton) findViewById(R.id.rb_hard);

        if(radEasy.isChecked()){
            return Intensity.EASY;
        }
        else if(radMedium.isChecked()){
            return Intensity.MEDIUM;
        }
        else if(radHard.isChecked()){
            return Intensity.HARD;
        }
        else {
            return Intensity.ALL;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_token_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.about){
            Intent i = new Intent(this,About.class);
            startActivity(i);
        }
        else if (id == R.id.filter){
            Intent i = new Intent(this, FilterActivity.class);
            startActivity(i);
            finish();
        }
        else if (id == R.id.show_map){
            Intent i = new Intent(this, Map.class);
            i.putExtra("state", Map.SHOW_MAP);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
            {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap squarePhoto = cropToSquare(photo);
                image.setImageBitmap(squarePhoto);
            }
        }
        catch (Exception e){

        }

    }
    private void pickImage(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void uploadImage(String key){
        FirebaseUser currentUser=auth.getCurrentUser();
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storage.child(TOKEN_CHILD).child(key).child("token").putBytes(data);
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        return cropImg;
    }
}
