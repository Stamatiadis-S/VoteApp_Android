package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.pug.voteapp_android.network.NetworkService;
import com.example.pug.voteapp_android.R;
import com.example.pug.voteapp_android.adapters.ViewPagerAdapter;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private NetworkService networkService;
    private ViewPager viewPager;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        networkService = new NetworkService(prefs.getString("SERVER_URL", "http://192.168.1.101:9000/"));
        if(prefs.getString("SERVER_URL", null) == null) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
        }
        if(prefs.getString("AUTHENTICATION_TOKEN", null) == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        username = prefs.getString("USERNAME", "");
        initializeViews();
        //Initialize friendlist within shared preferences.
        if(prefs.getStringSet(username + "_" + "FRIENDLISTS", null) == null)
            prefs.edit().putStringSet(username + "_" + "FRIENDLISTS", new HashSet<String>()).apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_manage_users:
                Intent friendlistIntent = new Intent(this, FriendlistsActivity.class);
                startActivity(friendlistIntent);
                return true;
            case R.id.menu_item_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_item_logout:
                prefs.edit().remove("AUTHENTICATION_TOKEN").commit();
                prefs.edit().remove("USERNAME").commit();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(username);
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        setupViewPager();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_layout_tab);
        tabLayout.setupWithViewPager(viewPager);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.main_add_poll_fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PollCreationActivity.class);
                startActivity(intent);
            }
        });
    }
    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PublicPollsFragment(), "Public");
        adapter.addFragment(new PrivatePollsFragment(), "Private");
        adapter.addFragment(new CreatedPollsFragment(), "Created");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    public NetworkService getNetworkService() {
        return networkService;
    }
    public SharedPreferences getPrefs() {
        return prefs;
    }
}
