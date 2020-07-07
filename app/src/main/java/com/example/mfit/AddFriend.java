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
import android.widget.SearchView;
import android.widget.Toast;

import com.example.models.User;
import com.example.myplace.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddFriend extends AppCompatActivity {
    private DatabaseReference database;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    final ArrayList<User> suggestions=filterSuggestions(AllUsersData.getInstance().allUsers);
    ArrayAdapter<User> adapter,adapterReq,adapterFriends;
    SearchView sw;
    ListView lwsuggestion,lwrequests,lwfriends;
    public ArrayList<User> usersFriends = new ArrayList<User>();
    public ArrayList<User> friendRequests = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);


        database= FirebaseDatabase.getInstance().getReference();
        lwsuggestion=(ListView) findViewById(R.id.suggestion_list);
        lwrequests=(ListView)findViewById(R.id.requsts_list);
        lwfriends=(ListView)findViewById(R.id.friends_list);
        sw=(SearchView)findViewById(R.id.search_friend);

        //Adapter

        adapterReq = new ArrayAdapter<User>(this,android.R.layout.simple_list_item_1,AllUsersData.getInstance().friendRequests);
        adapterFriends = new ArrayAdapter<User>(this,android.R.layout.simple_list_item_1,AllUsersData.getInstance().findUsersFriends());
        adapter = new ArrayAdapter<User>(this,android.R.layout.simple_list_item_1,AllUsersData.getInstance().filterSuggestions());

        lwsuggestion.setAdapter(adapter);

        lwrequests.setAdapter(adapterReq);
        lwfriends.setAdapter(adapterFriends);
        sw.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        lwsuggestion.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener(){
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                User user =adapter.getItem(info.position);
                menu.setHeaderTitle(user.FullName());
                menu.add(0,1,1,"Send request");
            }
        });


        lwrequests.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener(){
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                User user =adapterReq.getItem(info.position);
                menu.setHeaderTitle(user.FullName());
                menu.add(0,2,1,"Accept request");
                menu.add(0,3,2,"Decline request");
            }
        });

        lwsuggestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(AddFriend.this, ViewProfileActivity.class);
                bundle.putSerializable("user",adapter.getItem(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        lwrequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(AddFriend.this, ViewProfileActivity.class);
                bundle.putSerializable("user",adapterReq.getItem(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        lwfriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(AddFriend.this, ViewProfileActivity.class);
                bundle.putSerializable("user",adapterFriends.getItem(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        if(item.getItemId() == 1){
            User user=adapter.getItem(info.position);
            database.child("requests").child(user.uID).child("nizZahteva").child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getUid());
            Toast.makeText(AddFriend.this,"Friend request sent to "+user.FullName(), Toast.LENGTH_LONG).show();
        }
        else if(item.getItemId() == 2){
            User user=adapterReq.getItem(info.position);
            database.child("requests").child(auth.getCurrentUser().getUid()).child("nizZahteva").child(user.uID).removeValue();
            database.child("users").child(auth.getCurrentUser().getUid()).child("friends").child(user.uID).setValue(user.uID);
            database.child("users").child(user.uID).child("friends").child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getUid());
            adapterReq.remove(user);
            adapter.remove(user);
            adapterFriends.add(user);
            //AllUsersData.getInstance().usersFriends.add(user);
            AllUsersData.getInstance().currUser.friends.put(user.uID,user.uID);
            AllUsersData.getInstance().friendRequests.remove(user);
            Toast.makeText(AddFriend.this,"Friend request accepted from user " + user.FullName(), Toast.LENGTH_LONG).show();
        }
        else if(item.getItemId() == 3){
            User user=adapterReq.getItem(info.position);
            database.child("requests").child(auth.getCurrentUser().getUid()).child("nizZahteva").child(user.uID).removeValue();
            adapterReq.remove(user);
            adapter.add(user);
            AllUsersData.getInstance().friendRequests.remove(user);
            Toast.makeText(AddFriend.this,"Friend request declined from user " + user.FullName(), Toast.LENGTH_LONG).show();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_friend_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.leaderboard){
            Intent i = new Intent(this,LeaderBoardActivity.class);
            finish();
            startActivity(i);
        }
        else if (id == R.id.profile){
            Intent i = new Intent(this, ProfileActivity.class);
            finish();
            startActivity(i);
        }
        else if (id == R.id.show_map){
            Intent i = new Intent(this, Map.class);
            i.putExtra("state", Map.SHOW_FRIENDS);
            finish();
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<User> filterSuggestions(ArrayList<User> allUsers){
        ArrayList<User> suggestions=new ArrayList<>();
        for (int i=0;i<allUsers.size();i++){
            if(!auth.getCurrentUser().getUid().equals(allUsers.get(i).uID)){
                boolean flag = false;
                boolean flag1 = false;
                for(int j=0; j<AllUsersData.getInstance().friendRequests.size(); j++){
                    if(allUsers.get(i).uID.equals(AllUsersData.getInstance().friendRequests.get(j).uID)){
                        flag = true;
                    }
                }
                for(int z=0; z<AllUsersData.getInstance().usersFriends.size(); z++){
                    if(allUsers.get(i).uID.equals(AllUsersData.getInstance().usersFriends.get(z).uID)){
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

}
