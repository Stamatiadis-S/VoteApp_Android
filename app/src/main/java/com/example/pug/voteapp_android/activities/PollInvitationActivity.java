package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pug.voteapp_android.models.Participation;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.models.User;
import com.example.pug.voteapp_android.network.NetworkService;
import com.example.pug.voteapp_android.R;
import com.github.jasminb.jsonapi.JSONAPIDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class PollInvitationActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private NetworkService networkService;
    private String authenticationToken;
    private String USERNAME;
    private Poll poll;
    private ArrayAdapter<String> spinnerAdapter;
    private Set<String> friendlists;
    private List<String> usernames = new ArrayList<>();

    private Spinner spinner;
    private TextInputEditText singleUsernameView;
    private Button inviteSingleButton;
    private Button inviteMultipleButton;
    private LinearLayout usernamesContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_invitation);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        networkService = new NetworkService(prefs.getString("SERVER_URL", "http://192.168.1.101:9000/"));
        USERNAME = prefs.getString("USERNAME", "");
        authenticationToken = "bearer " + prefs.getString("AUTHENTICATION_TOKEN", null);
        friendlists = prefs.getStringSet(USERNAME + "_" + "FRIENDLISTS", new HashSet<String>());
        Intent intent = getIntent();
        poll = intent.getParcelableExtra("poll");
        initializeViews();
    }

    private void initializeViews() {
        singleUsernameView = (TextInputEditText) findViewById(R.id.poll_invitation_username);
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(friendlists));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) findViewById(R.id.poll_invitation_friendlists_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(friendlistsSpinnerListener);
        usernamesContainer = (LinearLayout) findViewById(R.id.poll_invitation_friendlist_usernames);
        inviteSingleButton = (Button) findViewById(R.id.poll_invitation_invite_single);
        inviteSingleButton.setOnClickListener(inviteSingleListener);
        inviteMultipleButton = (Button) findViewById(R.id.poll_invitaion_invite_multiple);
        inviteMultipleButton.setOnClickListener(inviteMultipleListener);
        progressBar = (ProgressBar) findViewById(R.id.poll_invitation_progress);
        progressBar.setIndeterminate(true);
    }
    private View addUsernameRow(String username, boolean enabled) {
        //Add option field.
        EditText editText = new EditText(PollInvitationActivity.this);
        editText.setTag("username_data");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        editText.setMaxLines(1);
        editText.setHint("Username");
        editText.setText(username);
        if (!enabled)
            editText.setEnabled(false);

        return editText;
    }
    private void makeToastShort(int msgId) {
        Toast.makeText(PollInvitationActivity.this,
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 5 && username.length() <= 20;
    }
    private Participation craftParticipationObject(Integer pos) {
        Poll pollRelationship = new Poll();
        pollRelationship.setId(poll.getId());
        pollRelationship.setOptions(null);
        if (pos == null)
            return new Participation(pollRelationship, new User(singleUsernameView.getText().toString()));
        else
            return new Participation(pollRelationship, new User(usernames.get(pos)));
    }

    Spinner.OnItemSelectedListener friendlistsSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            usernamesContainer.removeAllViews();
            String selectedFriendlistValue = prefs.getString(parent.getItemAtPosition(position).toString(), null);
            if (selectedFriendlistValue == null) return;
            if (!selectedFriendlistValue.isEmpty()) {
                usernames = Arrays.asList(selectedFriendlistValue.split(","));
                for (String username : usernames)
                    usernamesContainer.addView(addUsernameRow(username, false));
            }
            inviteMultipleButton.setEnabled(true);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    View.OnClickListener inviteSingleListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            singleUsernameView.setError(null);
            if(!isUsernameValid(singleUsernameView.getText().toString())) {
                singleUsernameView.setError("Username must be between 5-20 characters.");
                return;
            }
            final Participation participation = craftParticipationObject(null);
            participation.getUser().setId("null");final Observable<Response<JSONAPIDocument<Participation>>> participationCreationObservable = networkService.getNetworkApi()
                    .createParticipationObservable(authenticationToken, new JSONAPIDocument<>(participation))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            participationCreationObservable.subscribe(new Observer<Response<JSONAPIDocument<Participation>>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    progressBar.setVisibility(View.VISIBLE);
                    inviteSingleButton.setEnabled(false);
                    inviteMultipleButton.setEnabled(false);
                }

                @Override
                public void onNext(Response<JSONAPIDocument<Participation>> participationCreationResponse) {
                    switch (participationCreationResponse.code()) {
                        case 201:
                            makeToastShort(R.string.network_participation_creation_success);
                            break;
                        case 400:
                            makeToastShort(R.string.network_participation_creation_failure_expiration);
                            break;
                        case 409:
                            makeToastShort(R.string.network_participation_creation_failure_conflict);
                            break;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("DEBUG", "Error: " + e.toString());
                    makeToastShort(R.string.network_problem);
                }

                @Override
                public void onComplete() {
                    progressBar.setVisibility(View.GONE);
                    inviteSingleButton.setEnabled(true);
                    inviteMultipleButton.setEnabled(true);
                }
            });
        }
    };

    View.OnClickListener inviteMultipleListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(usernames.size() < 1) return;
            progressBar.setVisibility(View.VISIBLE);
            inviteSingleButton.setEnabled(false);
            inviteMultipleButton.setEnabled(false);
            for (int i = 0; i < usernames.size(); i++) {
                final int usernamePosition = i;
                final Participation participation = craftParticipationObject(i);
                participation.getUser().setId("null");
                final Observable<Response<JSONAPIDocument<Participation>>> participationCreationObservable = networkService.getNetworkApi()
                        .createParticipationObservable(authenticationToken, new JSONAPIDocument<>(participation))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                participationCreationObservable.subscribe(new Observer<Response<JSONAPIDocument<Participation>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Response<JSONAPIDocument<Participation>> participationCreationResponse) {
                        switch (participationCreationResponse.code()) {
                            case 201:
                                ((EditText) usernamesContainer.getChildAt(usernamePosition))
                                        .setTextColor(Color.GREEN);
                                break;
                            case 400:
                                makeToastShort(R.string.network_participation_creation_failure_expiration);
                                break;
                            case 409:
                                ((EditText) usernamesContainer.getChildAt(usernamePosition))
                                        .setTextColor(Color.GREEN);
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("DEBUG", "Error: " + e.toString());
                        makeToastShort(R.string.network_problem);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
            }
            progressBar.setVisibility(View.GONE);
            inviteSingleButton.setEnabled(true);
            inviteMultipleButton.setEnabled(true);
        }
    };
}