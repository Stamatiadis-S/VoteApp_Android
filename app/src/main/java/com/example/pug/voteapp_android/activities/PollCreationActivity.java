package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pug.voteapp_android.models.Option;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.network.NetworkService;
import com.example.pug.voteapp_android.R;
import com.github.jasminb.jsonapi.JSONAPIDocument;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class PollCreationActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private NetworkService networkService;
    private String authenticationToken;
    boolean submittingPoll = false;

    private LinearLayout containerView;
    private Spinner visibilitySpinner;
    private Spinner expirationSpinner;
    private Spinner typeSpinner;
    private EditText pollQuestionInput;
    private FloatingActionButton submitButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_creation);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        networkService = new NetworkService(prefs.getString("SERVER_URL", "http://192.168.1.101:9000/"));
        authenticationToken = "bearer " + this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE)
                .getString("AUTHENTICATION_TOKEN", null);
        initializeViews();
        if(savedInstanceState != null) {
            ArrayList<String> optionData = savedInstanceState.getStringArrayList("option_data");
            if (optionData != null) {
                for (String option : optionData) containerView.addView(addOptionRow(option));
            }
        } else {
            containerView.addView(addOptionRow(""));
            containerView.addView(addOptionRow(""));
        }
        refreshSubmitButtonState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> optionData = new ArrayList<>();
        for(int i = 0; i < containerView.getChildCount(); i++ ) {
            //Access first level child (looking for linear layouts views).
            View fstChildView = containerView.getChildAt(i);
            if(!(fstChildView instanceof LinearLayout)) continue;
            //Access second level child (looking for edit text views).
            View sndChildView = ((LinearLayout) fstChildView).getChildAt(0);
            Object viewTag = sndChildView.getTag();
            if(viewTag != null && viewTag.equals("option_data")) {
                //Don't save the empty options.
                if(((EditText) sndChildView).getText().length() == 0) continue;
                optionData.add(((EditText) sndChildView).getText().toString());
            }
        }
        if(optionData.isEmpty()) {
            outState.remove("option_data");
            return;
        }
        outState.putStringArrayList("option_data", optionData);
    }

    private void initializeViews() {
        containerView = (LinearLayout) findViewById(R.id.poll_creation_content);
        visibilitySpinner = (Spinner) findViewById(R.id.poll_creation_visibility_spinner);
        expirationSpinner = (Spinner) findViewById(R.id.poll_creation_expiration_spinner);
        typeSpinner = (Spinner) findViewById(R.id.poll_creation_mode_spinner);
        pollQuestionInput = (EditText) findViewById(R.id.poll_creation_question_input);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.poll_creation_add_option_button);
        addButton.setOnClickListener(addOptionListener);
        submitButton = (FloatingActionButton) findViewById(R.id.poll_creation_submit_button);
        submitButton.setOnClickListener(submitCreationListener);
        progressBar = (ProgressBar) findViewById(R.id.poll_creation_progress);
        progressBar.setIndeterminate(true);
    }
    private void refreshSubmitButtonState() {
        if(isRequirementsValid())
            enableSubmitButton();
        else
            disableSubmitButton();
    }
    private void enableSubmitButton() {
        submitButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(PollCreationActivity.this, R.color.greenDark)));
        submitButton.setEnabled(true);
    }
    private void disableSubmitButton() {
        submitButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(PollCreationActivity.this, R.color.grayDark)));
        submitButton.setEnabled(false);
    }
    private void makeToastShort(int msgId) {
        Toast.makeText(PollCreationActivity.this,
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }
    private View addOptionRow(String optionData) {
        //Create parent layout.
        LinearLayout linearLayout = new LinearLayout(PollCreationActivity.this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //Add option field.
        EditText editText = new EditText(PollCreationActivity.this);
        editText.setTag("option_data");
        editText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 10));
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        editText.setMaxLines(1);
        editText.setHint("Option");
        editText.setText(optionData);
        linearLayout.addView(editText);

        //Add dismiss button.
        ImageButton imageButton = new ImageButton(PollCreationActivity.this);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        imageButton.setImageResource(R.drawable.ic_action_remove);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setOnClickListener(removeOptionListener);
        linearLayout.addView(imageButton);

        return linearLayout;
    }

    private String [] isPollCreationValid() {
        boolean cancel = false;
        View focusView = null;
        String pollData [] = new String[containerView.getChildCount()+3];

        //Clear all errors.
        pollQuestionInput.setError(null);
        for(int i=2; i < containerView.getChildCount(); i++) {
            LinearLayout currentOptionRootView = ((LinearLayout) containerView.getChildAt(i));
            EditText currentOptionView = ((EditText) currentOptionRootView.getChildAt(0));
            currentOptionView.setError(null);
        }
        pollData[0] = visibilitySpinner.getSelectedItem().toString();
        pollData[1] = expirationSpinner.getSelectedItem().toString();
        pollData[2] = typeSpinner.getSelectedItem().toString();
        //Add poll question.
        if(!isPollQuestionValid()) {
            cancel = true;
            pollQuestionInput.setError(getString(R.string.error_invalid_question));
            focusView = pollQuestionInput;
        }
        pollData[3] = pollQuestionInput.getText().toString();
        //Add poll options.
        for(int i=2; i < containerView.getChildCount(); i++) {
            LinearLayout currentOptionRootView = ((LinearLayout) containerView.getChildAt(i));
            EditText currentOptionView = ((EditText) currentOptionRootView.getChildAt(0));
            if(!isOptionValid(currentOptionView.getText().toString())) {
                cancel = true;
                currentOptionView.setError(getString(R.string.error_invalid_option));
                focusView = currentOptionView;
                continue;
            }
            pollData[i+2] = currentOptionView.getText().toString();
        }
        if(cancel) {
            focusView.requestFocus();
            submitButton.setEnabled(true);
            return null;
        }
        return pollData;
    }
    private boolean isRequirementsValid() {
        return containerView.getChildCount() >= 4;
    }
    private boolean isPollQuestionValid() {
        int chars = pollQuestionInput.getText().toString().length();
        return (chars >= 8 && chars <= 250 );
    }
    private boolean isOptionValid(String pollOption) {
        return (pollOption.length() >= 1 && pollOption.length() <= 100);
    }

    View.OnClickListener removeOptionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(submittingPoll) return;
            containerView.removeView((View) v.getParent());
            refreshSubmitButtonState();
        }
    };

    View.OnClickListener addOptionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(submittingPoll) return;
            containerView.addView(addOptionRow(null));
            refreshSubmitButtonState();
        }
    };

    View.OnClickListener submitCreationListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            view.setEnabled(false);
            submittingPoll = true;
            String [] pollData = isPollCreationValid();
            ArrayList<Option> options = new ArrayList<>();
            if(pollData == null) {
                submittingPoll = false;
                return;
            }
            for(int i=4; i < pollData.length-1; i++) options.add(new Option(pollData[i]));
            Observable<Response<JSONAPIDocument<Poll>>> pollCreationObservable = networkService.getNetworkApi()
                    .createPollObservable(authenticationToken , new JSONAPIDocument<>(new Poll(pollData[0], pollData[2], pollData[3], options, pollData[1])))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            pollCreationObservable.subscribe(new Observer<Response<JSONAPIDocument<Poll>>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNext(Response<JSONAPIDocument<Poll>> pollCreationResponse) {
                    switch(pollCreationResponse.code()) {
                        case 201:
                            makeToastShort(R.string.network_poll_creation_success);
                            finish();
                            break;
                        case 400:
                            //No need to analyze errors since we do validate before hitting the endpoint.
                            makeToastShort(R.string.network_poll_creation_failure);
                            break;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("DEBUG","Error: " + e.toString());
                    progressBar.setVisibility(View.GONE);
                    enableSubmitButton();
                    makeToastShort(R.string.network_problem);
                }

                @Override
                public void onComplete() {
                    submittingPoll = false;
                    progressBar.setVisibility(View.GONE);
                    enableSubmitButton();
                }
            });
        }
    };
}
