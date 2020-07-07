package com.example.mfit;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.models.User;
import com.example.myplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.ByteArrayOutputStream;

public class SignInActivity extends Activity {
    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText confirmPassword;
    EditText date;
    ImageView image;
    Uri imageUri;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private StorageReference storage;
    public static final String USER_CHILD="users";
    private Context that=this;
    User user;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        firstName=findViewById(R.id.FirstNameCA);
        lastName=findViewById(R.id.LastNameCA);
        email=findViewById(R.id.EmailCA);
        password=findViewById(R.id.PasswordCA);
        confirmPassword=findViewById(R.id.ConfirmPasswordCA);

        date= findViewById(R.id.DateCA);
        image=findViewById(R.id.ImageEdit);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        Button btnCreateAccount=findViewById(R.id.CreateAccount);
        //endregion

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //region IF-ELSE Checks
                if(firstName.getText().toString().isEmpty()){
                    firstName.setError("First Name is empty");
                    return;
                }
                else if(lastName.getText().toString().isEmpty()){
                    lastName.setError("Last Name is empty");
                    return;
                }
                else if(email.getText().toString().isEmpty()){
                    email.setError("Email is empty");
                    return;
                }
                else if(password.getText().toString().isEmpty()){
                    password.setError("Password is empty");
                    return;
                }
                else if(confirmPassword.getText().toString().isEmpty()){
                    confirmPassword.setError("Confirm Password is empty");
                    return;
                }
                else if(date.getText().toString().isEmpty()){
                    date.setError("Date is empty");
                    return;
                }
                //endregion

                //region User init with data
                user=new User();
                user.firstName=firstName.getText().toString();
                user.lastName=lastName.getText().toString();
                user.email=email.getText().toString();
                if(password.getText().toString().equals(confirmPassword.getText().toString())){
                    user.password=password.getText().toString();
                }
                else {
                    confirmPassword.setError("Password and confirm password are not matching");
                    password.setError("Password and confirm password are not matching");
                    return;
                }
                user.birthDate=date.getText().toString();

                //endregion

                //region CreateUserWithEmailAndPassword
                auth.createUserWithEmailAndPassword(user.email,user.password).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser currentUser=auth.getCurrentUser();
                            database.child(USER_CHILD).child(currentUser.getUid()).setValue(user);
                            database.child(USER_CHILD).child(currentUser.getUid()).child("friends").child("AAAAAAAA").setValue("AAAAAAAA");
                            uploadImage();
                            Toast.makeText(SignInActivity.this, "Profile Account Created", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(SignInActivity.this, "Account Creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //endregion
            }
        });
    }

    private void pickImage(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Bitmap squarePhoto = cropToSquare(photo);
            image.setImageBitmap(squarePhoto);
        }
    }

    private void uploadImage(){
        FirebaseUser currentUser=auth.getCurrentUser();
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storage.child(USER_CHILD).child(currentUser.getUid()).child("profile").putBytes(data);
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