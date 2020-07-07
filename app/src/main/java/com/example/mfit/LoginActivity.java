package com.example.mfit;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {

    private EditText emailEdit;
    private EditText passwordEdit;
    private TextView forgotPassTxt;
    private TextView noAccTxt;
    private Button loginBtn;
    private Context that=this;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //region Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyTokensData.getInstance().init();
        auth=FirebaseAuth.getInstance();
        emailEdit=findViewById(R.id.emailEditTxt);
        passwordEdit=findViewById(R.id.passEditTxt);
        noAccTxt=findViewById(R.id.noAccTxt);
        loginBtn=findViewById(R.id.loginBtn);
        //endregion

        //region LoginBtn Listener
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signInWithEmailAndPassword(emailEdit.getText().toString(),passwordEdit.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    //MyTokensData.getInstance().init();
                                    //AllUsersData.getInstance().resetListeners();
                                    Intent i1= new Intent(LoginActivity.this, LocationService.class);
                                    LoginActivity.this.startService(i1);
                                    Intent i = new Intent(LoginActivity.this, Map.class);
                                    i.putExtra("state", Map.SHOW_MAP);
                                    startActivity(i);
                                    finish();
                                }
                                else {
                                    Toast.makeText(that, "Email or password incorrect", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        //endregion
        loginBtn.setEnabled(false);
        //region TextChange Listeners
        emailEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(emailEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()){
                    loginBtn.setEnabled(false);
                }
                else {
                    loginBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(emailEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()){
                    loginBtn.setEnabled(false);
                }
                else {
                    loginBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion

        noAccTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                that.startActivity(new Intent(that, SignInActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyTokensData.getInstance().init();
        FirebaseUser user=auth.getCurrentUser();
        if(user!=null){
            setContentView(R.layout.loading);
            Intent i1= new Intent(this, LocationService.class);
            Runnable runnable = new Runnable() {
                public void run() {
                    Intent i = new Intent(LoginActivity.this, Map.class);
                    i.putExtra("state", Map.SHOW_MAP);
                    startActivity(i);
                    finish();
                }
            };
            Handler handler = new android.os.Handler();
            handler.postDelayed(runnable, 2000);
            this.startService(i1);
        }
    }
}