package com.example.mfit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.models.Filter;
import com.example.models.Intensity;
import com.example.models.Type;
import com.example.myplace.R;


public class FilterActivity extends AppCompatActivity {
    CheckBox cbShowMyTokens;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        cbShowMyTokens=(CheckBox) findViewById(R.id.show_mine);
        cbShowMyTokens.setChecked(true);
        Button setFilter = (Button)findViewById(R.id.setFilters);
        Button clearFilter = (Button)findViewById(R.id.clearFilters);

        final EditText range= (EditText)findViewById(R.id.range);

        setFilter.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Filter newFilter = new Filter();
                newFilter.setIntensityFilter(getIntensity());
                newFilter.setTypeFilter(getType());
                newFilter.setShowMine(getShowMine());
                if(range.getText() != null && !range.getText().toString().trim().equals(""))
                    newFilter.setRangeFilter(Double.parseDouble(range.getText().toString()));
                else
                    newFilter.setRangeFilter(10000);
                FilterData.getInstance().filter=newFilter;
                startMapFilter();

            }
        });
        clearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filter newFilter = new Filter();
                newFilter.setIntensityFilter(Intensity.ALL);
                newFilter.setTypeFilter(Type.ALL);
                newFilter.setRangeFilter(9999);
                newFilter.setShowMine(getShowMine());
                FilterData.getInstance().filter=newFilter;
                startMapFilter();
            }
        });
    }

    private boolean getShowMine(){
            return cbShowMyTokens.isChecked();
    }

    private void startMapFilter(){
        Intent i = new Intent(this, Map.class);
        i.putExtra("state", Map.FILTER_TOKENS);
        finish();
        startActivity(i);
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
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.about){
            Intent i = new Intent(this,About.class);
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
