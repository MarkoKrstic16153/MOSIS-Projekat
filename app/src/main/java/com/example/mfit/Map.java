package com.example.mfit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.models.Filter;
import com.example.models.Intensity;
import com.example.models.Token;
import com.example.models.Type;
import com.example.models.User;
import com.example.models.UserLocation;
import com.example.myplace.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Map extends AppCompatActivity {

    MyLocationNewOverlay myLocationOverlay =null;
    ItemizedIconOverlay myPlacesOverlay = null;
    static int NEW_PLACE = 1;
    static final int LOCATION_PERMISSION = 2;
    MapView map = null;

    IMapController mapController = null;

    FirebaseAuth auth;
    public static final int SHOW_MAP = 0;
    public static final int FILTER_TOKENS = 1;
    public static final int SHOW_FRIENDS = 2;


    private int state = 0;
    private boolean selCoorsEnabled = false;
    private GeoPoint placeLoc = null;
    private boolean selected = false;
    private Menu meni = null;
    private DatabaseReference database;
    Location currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        setContentView(R.layout.activity_map);
        Intent mapIntent = getIntent();
        Bundle mapBundle = mapIntent.getExtras();
        state = mapBundle.getInt("state");

        map = (MapView) findViewById(R.id.map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapController = map.getController();
        mapController.setZoom(15.0);

        GeoPoint startPoint = new GeoPoint(43.3209, 21.8958);
        mapController.setCenter(startPoint);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE},LOCATION_PERMISSION);
        }
        else{
            setupMap();
        }

        findViewById(R.id.fab).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent( getApplicationContext(), AddTokenActivity.class);
                startActivityForResult(i,NEW_PLACE);
            }
        });
    }

    @SuppressLint("Missing Permissions")
    @Override
    public void onRequestPermissionsResult(int reqCode,String permissions[],int[] grantedResults ){
        setupMap();
    }

    private void setMyLocationOverlay(){
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);
        mapController.setZoom(15.0);
        myLocationOverlay.enableFollowLocation();

    }

    private void setCenterPlaceOnMap(){
        mapController = map.getController();
        mapController.setZoom(15.0);
        mapController.setCenter(placeLoc);
    }

    private void setupMap(){
        switch (state){
            case SHOW_FRIENDS :
                setMyLocationOverlay();
                prikaziSvePrijatelje();
                break;
            case SHOW_MAP :
                setMyLocationOverlay();
                prikaziSveTokene();
                break;
            case FILTER_TOKENS :
                setMyLocationOverlay();
                prikaziFiltriraneTokene(FilterData.getInstance().filter);
                break;
            default:
                setMyLocationOverlay();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.maps_menu, menu);
            return true;
    }

    private void addMenuItems(Menu menu){
        menu.clear();
        if(selCoorsEnabled == false){
            menu.add(0,1,1,"Select Coordinates");
            menu.add(0,2,2,"Cancel");
        }
        else
        menu.add(0,1,1,"Cancel");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.profile){
            Intent i = new Intent(this, ProfileActivity.class);
            finish();
            startActivity(i);
        }
        else if (id == R.id.add_friend){
            Intent i = new Intent(this, AddFriend.class);
            finish();
            startActivity(i);
        }
        else if (id == R.id.filter){
            Intent i = new Intent(this, FilterActivity.class);
            finish();
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        map.onPause();
    }

    private void prikaziSveTokene() {
        if(myPlacesOverlay!=null)
            map.getOverlays().remove(myPlacesOverlay);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        for(int i = 0; i<MyTokensData.getInstance().myTokens.size();i++){
            Token token = MyTokensData.getInstance().getToken(i);
            OverlayItem overlayItem = new OverlayItem(token.tokenIntensity.toString(),token.tokenType.toString(),new GeoPoint(token.getLatitude(),token.getLongitude()));
            overlayItem.setMarker(this.getResources().getDrawable(getTokenImage(token)));
            overlayArrayList.add(overlayItem);
            if(MyTokensData.getInstance().currentLocation == null){
                double a = calculateDistanceBetweenTwoGeoPoints(21.8958,43.3209,token.getLongitude(),token.getLatitude());
                token.setDistance(a);
            }
            else{
                double a = calculateDistanceBetweenTwoGeoPoints(MyTokensData.getInstance().currentLocation.getLongitude(),MyTokensData.getInstance().currentLocation.getLatitude(),token.getLongitude(),token.getLatitude());
                token.setDistance(a);
            }
        }

        myPlacesOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Token token=MyTokensData.getInstance().getToken(index);
                Bundle bundle = new Bundle();
                Intent intent = new Intent(Map.this, ViewToken.class);
                bundle.putSerializable("token",token);
                intent.putExtras(bundle);
                startActivity(intent);
                return false;
            }
            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Token token=MyTokensData.getInstance().getToken(index);
                if(token.distance<=1300 && !token.getUserId().equals(auth.getCurrentUser().getUid())){
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(Map.this, CollectTokenActivity.class);
                    bundle.putSerializable("token",token);
                    intent.putExtras(bundle);
                    finish();
                    startActivity(intent);
                }
                return false;
            }
        },getApplicationContext());
        this.map.getOverlays().add(myPlacesOverlay);
    }

    private void prepareTokenDistances(){
        for(int i=0;i<MyTokensData.getInstance().myTokens.size();i++) {
            Token token = MyTokensData.getInstance().myTokens.get(i);
            if (MyTokensData.getInstance().currentLocation == null) {
                double a = calculateDistanceBetweenTwoGeoPoints(21.8958, 43.3209, token.getLongitude(), token.getLatitude());
                token.setDistance(a);
            } else {
                double a = calculateDistanceBetweenTwoGeoPoints(MyTokensData.getInstance().currentLocation.getLongitude(), MyTokensData.getInstance().currentLocation.getLatitude(), token.getLongitude(), token.getLatitude());
                token.setDistance(a);
            }
        }
    }

    private void  prikaziFiltriraneTokene(Filter filter){
        prepareTokenDistances();

        ArrayList<Token> finalTokens;
        ArrayList<Token> filterMineTokens = filterMine(MyTokensData.getInstance().myTokens,filter);
        finalTokens = filterMineTokens;

        if(filter.getRangeFilter() != 9999){
            ArrayList<Token> filterTypeTokens = filterType(filterMineTokens,filter);
            ArrayList<Token> filterIntensityTokens = filterIntensity(filterTypeTokens,filter);
            ArrayList<Token> filterDistanceTokens = filterDistance(filterIntensityTokens,filter);
            finalTokens = filterDistanceTokens;
        }

        if(myPlacesOverlay!=null)
            map.getOverlays().remove(myPlacesOverlay);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        for(int i = 0; i<finalTokens.size();i++){
            Token token = finalTokens.get(i);
            OverlayItem overlayItem = new OverlayItem(token.tokenIntensity.toString(),token.tokenType.toString(),new GeoPoint(token.getLatitude(),token.getLongitude()));
            overlayItem.setMarker(this.getResources().getDrawable(getTokenImage(token)));
            overlayArrayList.add(overlayItem);

        }

        final ArrayList<Token> finalTokens1 = finalTokens;
        final Map map = this;
        myPlacesOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Token token = finalTokens1.get(index);
                Bundle bundle = new Bundle();
                Intent intent = new Intent(Map.this, ViewToken.class);
                bundle.putSerializable("token",token);
                intent.putExtras(bundle);
                startActivity(intent);
                return false;
            }
            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Token token=finalTokens1.get(index);
                if(token.distance<=1300 && !token.getUserId().equals(auth.getCurrentUser().getUid())){

                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(Map.this, CollectTokenActivity.class);
                    bundle.putSerializable("token",token);
                    intent.putExtras(bundle);
                    finish();
                    startActivity(intent);
                }
                return false;
            }
        },getApplicationContext());
        this.map.getOverlays().add(myPlacesOverlay);
    }

    private void prikaziSvePrijatelje(){
        HashMap<String, UserLocation> friendMap = AllUsersData.getInstance().getFriendsLocations();
        Collection<String> keys = friendMap.keySet();
        final Object[] niz = keys.toArray();

        if(myPlacesOverlay!=null)
            map.getOverlays().remove(myPlacesOverlay);
        final ArrayList<OverlayItem> overlayArrayList = new ArrayList<>();
        for(int i = 0; i<niz.length;i++){
            UserLocation userLocation = friendMap.get(niz[i]);
            OverlayItem overlayItem = new OverlayItem("AAAA","BBBB",new GeoPoint(userLocation.getLat(), userLocation.getLongi()));
            overlayItem.setMarker(new BitmapDrawable(getResources(),AllUsersData.getInstance().friendImages.get(niz[i])));
            overlayArrayList.add(overlayItem);
        }

        myPlacesOverlay = new ItemizedIconOverlay<>(overlayArrayList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                User clickedFriend = AllUsersData.getInstance().getFriend(niz[index].toString());
                Bundle bundle = new Bundle();
                Intent intent = new Intent(Map.this, ViewProfileActivity.class);
                bundle.putSerializable("user",clickedFriend);
                intent.putExtras(bundle);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        },getApplicationContext());
        this.map.getOverlays().add(myPlacesOverlay);
    }

    private ArrayList<Token> filterIntensity(ArrayList<Token> filterTypeTokens, Filter filter) {
        ArrayList<Token> retTokenList=new ArrayList<>();
        for(int i =0;i<filterTypeTokens.size();i++){
            Token token=filterTypeTokens.get(i);
            switch (filter.getIntensityFilter()){
                case ALL:
                    retTokenList.add(token);
                    break;
                case EASY:
                    if(token.getTokenIntensity()==Intensity.EASY)
                        retTokenList.add(token);
                    break;
                case MEDIUM:
                    if(token.getTokenIntensity()==Intensity.MEDIUM)
                        retTokenList.add(token);
                    break;
                case HARD:
                    if(token.getTokenIntensity()==Intensity.HARD)
                        retTokenList.add(token);
                    break;
                default:
                    retTokenList.add(token);
                    break;
            }
        }
        return retTokenList;
    }

    private ArrayList<Token> filterDistance(ArrayList<Token> filterIntensityTokens, Filter filter) {
        ArrayList<Token> retTokenList=new ArrayList<>();
        for(int i =0;i<filterIntensityTokens.size();i++){
            Token token=filterIntensityTokens.get(i);
            if(filter.getRangeFilter()>=token.getDistance()) {
                retTokenList.add(token);
            }
        }
        return retTokenList;
    }

    private ArrayList<Token> filterType(ArrayList<Token> filterMineTokens, Filter filter) {
        ArrayList<Token> retTokenList=new ArrayList<>();
        for(int i =0;i<filterMineTokens.size();i++){
            Token token=filterMineTokens.get(i);
            switch (filter.getTypeFilter()){
                case ALL:
                    retTokenList.add(token);
                break;
                case GREEN:
                    if(token.getTokenType()==Type.GREEN)
                    retTokenList.add(token);
                break;
                case BLUE:
                    if(token.getTokenType()==Type.BLUE)
                    retTokenList.add(token);
                break;
                case RED:
                    if(token.getTokenType()==Type.RED)
                    retTokenList.add(token);
                break;
                default:
                    retTokenList.add(token);
                break;
            }
        }
        return retTokenList;
    }

    private ArrayList<Token> filterMine(ArrayList<Token> myTokens, Filter filter) {
        ArrayList<Token> retTokenList=new ArrayList<>();
        for(int i =0;i<myTokens.size();i++){
            Token token=myTokens.get(i);
            if(filter.isShowMine()){
                retTokenList.add(token);
            }
            else{
                if(!token.getUserId().equals(auth.getCurrentUser().getUid()))
                    retTokenList.add(token);
            }
        }
        return retTokenList;
    }

    private int getTokenImage(Token token){
        if(token.tokenType == Type.RED && token.tokenIntensity == Intensity.EASY)
            return R.drawable.er;
        else if(token.tokenType == Type.RED && token.tokenIntensity == Intensity.MEDIUM)
            return R.drawable.mr;
        else if(token.tokenType == Type.RED && token.tokenIntensity == Intensity.HARD)
            return R.drawable.hr;
        else if(token.tokenType == Type.BLUE && token.tokenIntensity == Intensity.EASY)
            return R.drawable.eb;
        else if(token.tokenType == Type.BLUE && token.tokenIntensity == Intensity.MEDIUM)
            return R.drawable.mb;
        else if(token.tokenType == Type.BLUE && token.tokenIntensity == Intensity.HARD)
            return R.drawable.hb;
        else if(token.tokenType == Type.GREEN && token.tokenIntensity == Intensity.EASY)
            return R.drawable.eg;
        else if(token.tokenType == Type.GREEN && token.tokenIntensity == Intensity.MEDIUM)
            return R.drawable.mg;
        else if(token.tokenType == Type.GREEN && token.tokenIntensity == Intensity.HARD)
            return R.drawable.hg;
        else
            return R.drawable.er;
    }

    private double calculateDistanceBetweenTwoGeoPoints(double long1,double lat1,double long2,double lat2){
        double earthRadiusKm = 6371.0;
        double dLat = degreesToRadians(lat2-lat1);
        double dLon = degreesToRadians(long2-long1);
        double latt1 = degreesToRadians(lat1);
        double latt2 = degreesToRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(latt1) * Math.cos(latt2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double res = earthRadiusKm * c;
        return res;
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }
}
