package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pug.voteapp_android.R;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    private EditText urlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        initializeViews();
    }

    private void initializeViews() {
        urlView = (EditText) findViewById(R.id.settings_server_url);
        urlView.setText(prefs.getString("SERVER_URL","http://192.168.1.101:9000/"));
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.settings_save_button);
        saveButton.setOnClickListener(saveButtonListener);
    }

    private void makeToastShort(int msgId) {
        Toast.makeText(SettingsActivity.this,
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            prefs.edit().remove("SERVER_URL").apply();
            prefs.edit().putString("SERVER_URL", urlView.getText().toString()).apply();
            makeToastShort(R.string.notify_settings_saved);
        }
    };
}
