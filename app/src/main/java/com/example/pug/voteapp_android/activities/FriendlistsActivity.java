package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pug.voteapp_android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendlistsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Set<String> friendlists;
    private String USERNAME;
    private String selectedFriendlistKey;
    private String selectedFriendlistValue;
    private ArrayAdapter<String> spinnerAdapter;
    private boolean editing = false;

    private Spinner spinner;
    private TextInputEditText nameView;
    private Button editButton;
    private LinearLayout usernamesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        USERNAME = prefs.getString("USERNAME","");
        friendlists = new HashSet<>(prefs.getStringSet(USERNAME + "_" + "FRIENDLISTS", new HashSet<String>()));
        initializeViews();
    }

    private void initializeViews() {
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(friendlists));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) findViewById(R.id.friendlists_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(friendlistsSpinnerListener);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.friendlists_delete_button);
        deleteButton.setOnClickListener(deleteButtonListener);
        Button createButton = (Button) findViewById(R.id.friendlists_new_button);
        createButton.setOnClickListener(createNewListener);
        editButton = (Button) findViewById(R.id.friendlists_edit_button);
        editButton.setOnClickListener(editButtonListener);
        nameView = (TextInputEditText) findViewById(R.id.friendlists_listname);
        usernamesContainer = (LinearLayout) findViewById(R.id.friendlists_usernames);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.friendlists_add_username_button);
        addButton.setOnClickListener(addNewListener);
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.friendlists_save_button);
        saveButton.setOnClickListener(saveButtonListener);
    }
    private View addUsernameRow(String username, boolean enabled) {
        //Create parent layout.
        LinearLayout linearLayout = new LinearLayout(FriendlistsActivity.this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //Add option field.
        EditText editText = new EditText(FriendlistsActivity.this);
        editText.setTag("username_data");
        editText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 10));
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        editText.setMaxLines(1);
        editText.setHint("Username");
        editText.setText(username);

        //Add dismiss button.
        ImageButton imageButton = new ImageButton(FriendlistsActivity.this);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        imageButton.setImageResource(R.drawable.ic_action_remove);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setOnClickListener(removeUsernameListener);

        if(!enabled) {
            editText.setEnabled(false);
            imageButton.setEnabled(false);
        }
        linearLayout.addView(editText);
        linearLayout.addView(imageButton);

        return linearLayout;
    }
    private void resetControls() {
        editing = false;
        selectedFriendlistKey = null;
        selectedFriendlistValue = null;
        nameView.setText(null);
        usernamesContainer.removeAllViewsInLayout();
    }
    private void makeToastShort(int msgId) {
        Toast.makeText(FriendlistsActivity.this,
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    Spinner.OnItemSelectedListener friendlistsSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            resetControls();
            selectedFriendlistKey = parent.getItemAtPosition(position).toString();
            if(selectedFriendlistKey.isEmpty()) return;
            selectedFriendlistValue = prefs.getString(selectedFriendlistKey, null);
            if(selectedFriendlistValue == null) return;
            nameView.setText(selectedFriendlistKey);
            nameView.setEnabled(false);
            if(!selectedFriendlistValue.isEmpty()) {
                List<String> usernames = Arrays.asList(selectedFriendlistValue.split(","));
                for (String username : usernames)
                    usernamesContainer.addView(addUsernameRow(username, false));
            }
            editButton.setEnabled(true);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    View.OnClickListener deleteButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            nameView.setError(null);
            if(selectedFriendlistKey == null) return;
            friendlists.remove(selectedFriendlistKey);
            prefs.edit().remove(selectedFriendlistKey).apply();
            prefs.edit().putStringSet(USERNAME + "_" + "FRIENDLISTS", friendlists).apply();
            spinnerAdapter.remove(selectedFriendlistKey);
            selectedFriendlistKey = null;
            selectedFriendlistValue = null;
            editing = false;
            nameView.setText(null);
            usernamesContainer.removeAllViews();
            spinner.post(new Runnable() {
                @Override
                public void run() {
                    spinner.setSelection(spinner.getCount()-1, true);
                }
            });
            makeToastShort(R.string.notify_friendlist_deleted);
        }
    };

    View.OnClickListener createNewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetControls();
            nameView.setEnabled(true);
            nameView.requestFocus();
            editing = false;
        }
    };

    View.OnClickListener editButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            nameView.setEnabled(true);
            for(int i = 0; i < usernamesContainer.getChildCount(); i++) {
                LinearLayout fstChild = (LinearLayout) usernamesContainer.getChildAt(i);
                fstChild.getChildAt(0).setEnabled(true);
                fstChild.getChildAt(1).setEnabled(true);
            }
            editing = true;
        }
    };

    View.OnClickListener addNewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            usernamesContainer.addView(addUsernameRow(null, true));
        }
    };
    View.OnClickListener removeUsernameListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            usernamesContainer.removeView((View) v.getParent());
        }
    };

    View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String editedFriendlistKey;
            nameView.setError(null);
            selectedFriendlistKey = nameView.getText().toString();

            //Validate name.
            if(selectedFriendlistKey.isEmpty()) {
                nameView.setError("Name must be at least one character!");
                nameView.requestFocus();
                return;
            }

            //Check if name is available.
            if(prefs.getString(selectedFriendlistKey, null) != null) {
                if(editing) {
                    editedFriendlistKey = spinner.getSelectedItem().toString();
                    if(!editedFriendlistKey.equals(selectedFriendlistKey)) {
                        nameView.setError("There is already a friendlist with this name!");
                        nameView.requestFocus();
                        return;
                    }
                    //Clear previous data.
                    friendlists.remove(editedFriendlistKey);
                    spinnerAdapter.remove(editedFriendlistKey);
                    prefs.edit().remove(editedFriendlistKey).apply();
                } else {
                    nameView.setError("There is already a friendlist with this name!");
                    nameView.requestFocus();
                    return;
                }
            }

            StringBuilder friendlistValue = new StringBuilder();
            for(int i = 0; i < usernamesContainer.getChildCount(); i++) {
                LinearLayout fstChild = ((LinearLayout)usernamesContainer.getChildAt(i));
                String username = ((EditText) fstChild.getChildAt(0)).getText().toString();
                if(username.isEmpty()) continue;
                friendlistValue.append(username);
                friendlistValue.append(",");
            }
            if(friendlistValue.length() > 0) friendlistValue.deleteCharAt(friendlistValue.length() - 1);
            prefs.edit().putString(selectedFriendlistKey, friendlistValue.toString()).apply();
            friendlists.add(selectedFriendlistKey);
            prefs.edit().putStringSet(USERNAME + "_" + "FRIENDLISTS", friendlists).apply();
            spinnerAdapter.add(selectedFriendlistKey);
            spinner.post(new Runnable() {
                @Override
                public void run() {
                    spinner.setSelection(spinner.getCount()-1, true);
                }
            });
            makeToastShort(R.string.notify_friendlist_saved);
        }
    };
}