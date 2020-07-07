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

import com.example.models.Intensity;
import com.example.models.Token;
import com.example.models.Type;
import com.example.myplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CollectTokenActivity extends AppCompatActivity {

        public static final long ONE_MEGABYTE=1024*1024;
        StorageReference storage;
        DatabaseReference database;
        Token sentToken = null;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_collect_token);
            CurrentUserData.getInstance().init();
            storage= FirebaseStorage.getInstance().getReference();
            database=FirebaseDatabase.getInstance().getReference();
            Button okBtn = (Button)findViewById(R.id.ok_button);
            Button collectBtn=(Button)findViewById(R.id.collectBtn);
            TextView typeText = (TextView)findViewById(R.id.textViewType);
            TextView intensityText = (TextView)findViewById(R.id.textViewIntensity);
            TextView placedByText = (TextView)findViewById(R.id.textViewPlacedBy);
            TextView distanceText = (TextView)findViewById(R.id.textViewDistance);
            TextView tokenWorth=(TextView)findViewById(R.id.tokenValue);
            final ImageView tokenImage = (ImageView)findViewById(R.id.tokenImage);

            try{
                Intent listIntent = getIntent();
                Bundle dataBundle = listIntent.getExtras();
                sentToken = (Token) dataBundle.get("token");
            }
            catch(Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                finish();
            }
            if(sentToken != null){
                storage.child("tokens").child(sentToken.key).child("token").getBytes(5*ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        byte[] data = task.getResult();
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        tokenImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, tokenImage.getWidth(),
                                tokenImage.getHeight(), false));
                    }
                });
                typeText.setText(typeText.getText() + ": " + sentToken.tokenType.toString() + ".");
                intensityText.setText(intensityText.getText() + ": "  + sentToken.tokenIntensity.toString() + ".");
                placedByText.setText(placedByText.getText() + ": "  + sentToken.getFullName() + ".");
                distanceText.setText(distanceText.getText() + ": "  + sentToken.getDistance() + " meters.");
                tokenWorth.setText(tokenWorth.getText() + ": " + this.getTokenWorth(sentToken) + ".");
            }

            collectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collectToken();
                    Toast.makeText(CollectTokenActivity.this,"Token collected", Toast.LENGTH_LONG).show();
                    AllUsersData.getInstance().updateAllUsers();
                    Intent i = new Intent(CollectTokenActivity.this, Map.class);
                    i.putExtra("state", Map.SHOW_MAP);
                    finish();
                    startActivity(i);
                }
            });

        }

        private void collectToken(){
            database.child("tokens").child(sentToken.key).removeValue();
            database.child("users").child(CurrentUserData.getInstance().currentUser.uID).child("points").setValue(CurrentUserData.getInstance().currentUser.points+getTokenWorth(sentToken));
            for(int i=0;i<MyTokensData.getInstance().myTokens.size();i++){
                if(MyTokensData.getInstance().myTokens.get(i).key.equals(sentToken.key)){
                    MyTokensData.getInstance().myTokens.remove(i);
                    i--;
                }
            }
        }

        public int getIntensityPoints(Token token){
            if(token.tokenIntensity== Intensity.HARD){
                return 50;
            }
            else if(token.tokenIntensity==Intensity.MEDIUM){
                return 25;
            }
            else if(token.tokenIntensity==Intensity.EASY){
                return 10;
            }
            else
                return 0;
        }

        public int getTypePoints(Token token){
            if(token.tokenType== Type.RED){
                return 30;
            }
            else  if(token.tokenType==Type.BLUE){
                return 20;
            }
            else if(token.tokenType==Type.GREEN){
                return 10;
            }
            else
                return 0;
        }

        public int getTokenWorth(Token token){
            return (getIntensityPoints(token)+getTypePoints(token));
        }

}

