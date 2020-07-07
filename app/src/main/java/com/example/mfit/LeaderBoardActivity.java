package com.example.mfit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.models.User;
import com.example.myplace.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

public class LeaderBoardActivity extends AppCompatActivity {
    private DatabaseReference database;
    private StorageReference storage;
    //private static final String FIREBASE_CHILD="tokens";
    ListView lw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        lw=(ListView) findViewById(R.id.listaaa);
        lw.setAdapter(new ArrayAdapter<User>(this,android.R.layout.simple_list_item_1,AllUsersData.getInstance().allUsers));
        lw.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener(){
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                User user = AllUsersData.getInstance().allUsers.get(info.position);
                menu.setHeaderTitle(user.FullName());
                //menu.add(0,1,1,"View Profile");
            }


           /* public  boolean onContextItemSelected(MenuItem item){

                return List.super.onContextItemSelected(item);
            }*/

        });

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(LeaderBoardActivity.this, ViewProfileActivity.class);
                bundle.putSerializable("user",AllUsersData.getInstance().allUsers.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }


        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leader_board_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.about){
            Intent i = new Intent(this,About.class);
            startActivity(i);
        }
        else if (id == R.id.profile){
            Intent i = new Intent(this, ProfileActivity.class);
            finish();
            startActivity(i);
        }
        else if (id == R.id.add_friend){
            Intent i = new Intent(this, AddFriend.class);
            finish();
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
