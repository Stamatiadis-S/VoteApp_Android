package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pug.voteapp_android.models.Option;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.models.Vote;
import com.example.pug.voteapp_android.network.NetworkService;
import com.example.pug.voteapp_android.R;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.jasminb.jsonapi.ErrorUtils;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.models.errors.Error;
import com.github.jasminb.jsonapi.models.errors.Errors;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class PollDetailsActivity extends AppCompatActivity {

    private NetworkService networkService;
    private SharedPreferences prefs;
    private String username;
    private String authenticationToken;
    private Poll poll;
    private int checkedBoxes = 0;
    private String checkedOptionId;
    private Set<String> checkedOptionIds = new HashSet<>();

    private LinearLayout containerView;
    private TextView expirationView;
    private RadioGroup optionsRadioGroup;
    private FloatingActionButton submitButton;
    private ProgressBar progressBar;
    private ColorStateList colorGreenDark;
    private ColorStateList colorGrayDark;
    private List<View> options = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_details);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        networkService = new NetworkService(prefs.getString("SERVER_URL", "http://192.168.1.101:9000/"));
        username = prefs.getString("USERNAME", null);
        authenticationToken = "bearer " + prefs.getString("AUTHENTICATION_TOKEN", null);
        Intent intent = getIntent();
        poll = intent.getParcelableExtra("poll");
        initializeViews();
        if(savedInstanceState != null) {
            poll = savedInstanceState.getParcelable("currentPoll");
            ArrayList<Integer> optionsSelected = savedInstanceState.getIntegerArrayList("options_selected");
            if(optionsSelected != null)
                switch (poll.getMode()){
                    case "Single":
                        ((RadioButton) optionsRadioGroup.getChildAt(optionsSelected.get(0))).setChecked(true);
                        break;
                    case "Multiple":
                        for(int index : optionsSelected) ((CheckBox) containerView.getChildAt(index)).setChecked(true);
                        break;
                }
        }
        if(isPollExpired(poll)) markAsExpired();
        if(poll.isVoted()) disableControls();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentPoll", poll);
        ArrayList<Integer> selectedOptions = new ArrayList<>();
        switch (poll.getMode()) {
            case "Single":
                int radioButtonId = optionsRadioGroup.getCheckedRadioButtonId();
                if(radioButtonId == -1) return;
                RadioButton radioButton = (RadioButton) optionsRadioGroup.findViewById(radioButtonId);
                selectedOptions.add(optionsRadioGroup.indexOfChild(radioButton));
                break;
            case "Multiple":
                for(int i=1; i < containerView.getChildCount(); i++) {
                    View currentChild = containerView.getChildAt(i);
                    if (currentChild instanceof CheckBox && ((CheckBox) currentChild).isChecked())
                        selectedOptions.add(i);
                }
                break;
        }
        if(selectedOptions.isEmpty()) {
            outState.remove("options_selected");
            return;
        }
        outState.putIntegerArrayList("options_selected", selectedOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_poll_details, menu);
        if(poll.getCreator().getUsername().equals(username))
            menu.findItem(R.id.menu_item_invite).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_chart_pie:
                Intent pieChartIntent = new Intent(this, PieChartActivity.class);
                pieChartIntent.putExtra("poll", this.poll);
                startActivity(pieChartIntent);
                return true;
            case R.id.menu_item_chart_bar:
                Intent barChartIntent = new Intent(this, BarChartActivity.class);
                barChartIntent.putExtra("poll", this.poll);
                startActivity(barChartIntent);
                return true;
            case R.id.menu_item_invite:
                Intent inviteIntent = new Intent(this, PollInvitationActivity.class);
                inviteIntent.putExtra("poll", this.poll);
                startActivity(inviteIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViews() {
        colorGreenDark = ColorStateList.valueOf(ContextCompat
                .getColor(PollDetailsActivity.this, R.color.greenDark));
        colorGrayDark = ColorStateList.valueOf(ContextCompat
                .getColor(PollDetailsActivity.this, R.color.grayDark));
        TextView creatorView = (TextView) findViewById(R.id.poll_details_creator);
        creatorView.setText(poll.getCreator().getUsername());
        TextView visibilityView = (TextView) findViewById(R.id.poll_details_visibility);
        visibilityView.setText(poll.getVisibility());
        TextView typeView = (TextView) findViewById(R.id.poll_details_mode);
        typeView.setText(poll.getMode());
        expirationView = (TextView) findViewById(R.id.poll_details_expiration);
        expirationView.setText(poll.getExpiration());
        containerView = (LinearLayout) findViewById(R.id.poll_details_content);
        TextView questionView = (TextView) findViewById(R.id.poll_details_question);
        questionView.setText(poll.getQuestion());
        progressBar = (ProgressBar) findViewById(R.id.poll_details_progress);
        progressBar.setIndeterminate(true);
        submitButton = (FloatingActionButton) findViewById(R.id.poll_details_submit_button);
        submitButton.setOnClickListener(submitVoteListener);
        disableSubmitButton();

        switch (poll.getMode()) {
            case "Single":
                //Render radio buttons.
                RadioGroup.LayoutParams radioGroupLayoutParams = new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);
                radioGroupLayoutParams.setMargins(64, 0, 64, 0);
                optionsRadioGroup = new RadioGroup(PollDetailsActivity.this);
                optionsRadioGroup.setId(View.generateViewId());
                optionsRadioGroup.setLayoutParams(radioGroupLayoutParams);
                optionsRadioGroup.setOnCheckedChangeListener(checkRadioListener);
                containerView.addView(optionsRadioGroup);
                for(Option option : poll.getOptions()) {
                    RadioButton radioButton = new RadioButton(PollDetailsActivity.this);
                    radioButton.setId(View.generateViewId());
                    RadioGroup.LayoutParams radioLayoutParams = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    radioButton.setText(option.getOption());
                    //Hiding the option id for easy access is hacky but saves us from multiple searches.
                    radioButton.setHint(option.getId());
                    options.add(radioButton);
                    optionsRadioGroup.addView(radioButton, radioLayoutParams);
                }
                break;
            case "Multiple":
                //Render checkboxes instead of radio buttons.
                for(Option option : poll.getOptions()) {
                    CheckBox checkbox = new CheckBox(PollDetailsActivity.this);
                    checkbox.setId(View.generateViewId());
                    LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    checkboxParams.setMargins(64, 0, 64, 0);
                    checkbox.setText(option.getOption());
                    //Hiding the option id for easy access is hacky but saves us from multiple searches.
                    checkbox.setHint(option.getId());
                    checkbox.setOnCheckedChangeListener(checkBoxListener);
                    options.add(checkbox);
                    containerView.addView(checkbox, checkboxParams);
                }
                break;
        }
    }
    private void enableSubmitButton() {
        submitButton.setBackgroundTintList(colorGreenDark);
        submitButton.setEnabled(true);
    }
    private void disableSubmitButton() {
        submitButton.setBackgroundTintList(colorGrayDark);
        submitButton.setEnabled(false);
    }
    private void refreshSubmitButtonState() {
        if(isRequirementsValid())
            enableSubmitButton();
        else
            disableSubmitButton();
    }
    private void enableControls() {
        enableSubmitButton();
        for(View view : options)
            view.setEnabled(true);
    }
    private void disableControls() {
        disableSubmitButton();
        for(View view : options)
            view.setEnabled(false);
    }
    private void markAsExpired() {
        disableControls();
        expirationView.append(" EXPIRED");
        expirationView.setTextColor(Color.RED);
    }
    private void incrementVotes(Vote vote) {
        for(Option voteOption : vote.getPoll().getOptions())
            for(Option pollOption : poll.getOptions())
                if(voteOption.getId().equals(pollOption.getId()))
                    pollOption.incrementVotes();
    }
    private void makeToastShort(int msgId) {
        Toast.makeText(PollDetailsActivity.this,
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    private boolean isPollExpired(Poll poll) {
        ISO8601DateFormat dateFormat = new ISO8601DateFormat();
        Date date = new Date();
        try {
            date = dateFormat.parse(poll.getExpiration());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            return false;
        }
        return Calendar.getInstance().getTime().after(date);
    }
    private boolean isRequirementsValid() {
        switch (poll.getMode()) {
            case "Single":
                return optionsRadioGroup.getCheckedRadioButtonId() != -1;
            case "Multiple":
                return checkedBoxes > 0;
        }
        return false;
    }
    private Vote craftVoteObject() {
        Poll pollRelationship = new Poll();
        pollRelationship.setId(poll.getId());
        Vote vote = new Vote(pollRelationship);
        Option option;
        switch(poll.getMode()) {
            case "Single":
                option = new Option();
                option.setId(checkedOptionId);
                pollRelationship.getOptions().add(option);
                break;
            case "Multiple":
                for(String optionId : checkedOptionIds) {
                    option = new Option();
                    option.setId(optionId);
                    pollRelationship.getOptions().add(option);
                }
                break;
        }
        return vote;
    }

    RadioGroup.OnCheckedChangeListener checkRadioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            checkedOptionId = ((RadioButton) findViewById(checkedId)).getHint().toString();
            refreshSubmitButtonState();
        }
    };

    CheckBox.OnCheckedChangeListener checkBoxListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                checkedBoxes++;
                checkedOptionIds.add(buttonView.getHint().toString());
            }
            else {
                checkedBoxes--;
                checkedOptionIds.remove(buttonView.getHint().toString());
            }
            refreshSubmitButtonState();
        }
    };

    View.OnClickListener submitVoteListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            final Vote vote = craftVoteObject();
            Observable<Response<JSONAPIDocument<Vote>>> voteCreationObservable = networkService.getNetworkApi()
                    .createVoteObservable(authenticationToken, new JSONAPIDocument<>(vote))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            voteCreationObservable.subscribe(new Observer<Response<JSONAPIDocument<Vote>>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disableControls();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNext(Response<JSONAPIDocument<Vote>> voteCreationResponse) {
                    switch(voteCreationResponse.code()) {
                        case 201:
                            makeToastShort(R.string.network_vote_creation_success);
                            incrementVotes(vote);
                            poll.setVoted(true);
                            break;
                        case 400:
                            try {
                                Errors errorsResponse = ErrorUtils.parseErrorResponse(
                                        networkService.getObjectMapper(),
                                        voteCreationResponse.errorBody(),
                                        Errors.class);
                                //Present important errors to user.
                                for(Error error : errorsResponse.getErrors()) {
                                    switch (error.getTitle()) {
                                        case "Resource poll has expired.":
                                            makeToastShort(R.string.network_vote_creation_failure_expiration);
                                            markAsExpired();
                                            break;
                                        default:
                                            makeToastShort(R.string.network_vote_creation_failure);
                                            break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 409:
                            makeToastShort(R.string.network_vote_creation_failure_conflict);
                            break;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("DEBUG","Error: " + e.toString());
                    progressBar.setVisibility(View.GONE);
                    enableControls();
                    makeToastShort(R.string.network_problem);
                }

                @Override
                public void onComplete() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    };

}
