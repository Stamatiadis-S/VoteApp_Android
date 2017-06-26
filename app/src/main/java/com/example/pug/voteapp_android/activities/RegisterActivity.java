package com.example.pug.voteapp_android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pug.voteapp_android.models.User;
import com.example.pug.voteapp_android.network.NetworkService;
import com.example.pug.voteapp_android.R;
import com.github.jasminb.jsonapi.ErrorUtils;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.models.errors.Error;
import com.github.jasminb.jsonapi.models.errors.Errors;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private NetworkService networkService;

    private ProgressBar progressBar;
    private EditText emailInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText passwordRepeatInput;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        networkService = new NetworkService(prefs.getString("SERVER_URL", "http://192.168.1.101:9000/"));
        initializeActivityViews();
    }

    private void initializeActivityViews() {
        progressBar = (ProgressBar) findViewById(R.id.register_progress);
        progressBar.setIndeterminate(true);
        emailInput = (EditText) findViewById(R.id.register_email);
        usernameInput = (EditText) findViewById(R.id.register_username);
        passwordInput = (EditText) findViewById(R.id.register_password);
        passwordRepeatInput = (EditText) findViewById(R.id.register_password_repeat);
        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(registerButtonListener);
        TextView loginView = (TextView) findViewById(R.id.register_login_prompt);
        loginView.setOnClickListener(loginViewListener);
    }

    private String[] isLoginValid() {
        String[] formData = new String[4];
        formData[0] = emailInput.getText().toString();
        formData[1] = usernameInput.getText().toString();
        formData[2] = passwordInput.getText().toString();
        formData[3] = passwordRepeatInput.getText().toString();

        registerButton.setEnabled(false);
        emailInput.setError(null);
        usernameInput.setError(null);
        passwordInput.setError(null);
        passwordRepeatInput.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check if password repeat is correct.
        if (!formData[3].equals(formData[2])) {
            passwordRepeatInput.setError(getString(R.string.error_incorrect_password_repeat));
            focusView = passwordRepeatInput;
            cancel = true;
        }
        // Check for a valid password.
        if (TextUtils.isEmpty(formData[2]) || !isPasswordValid(formData[2])) {
            passwordInput.setError(getString(R.string.error_invalid_password));
            focusView = passwordInput;
            cancel = true;
        }
        // Check for a username.
        if (TextUtils.isEmpty(formData[1]) || !isUsernameValid(formData[1])) {
            usernameInput.setError(getString(R.string.error_invalid_username));
            focusView = usernameInput;
            cancel = true;
        }
        // Check for a valid email.
        if (!isEmailValid(formData[0])) {
            emailInput.setError(getString(R.string.error_invalid_email));
            focusView = emailInput;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            registerButton.setEnabled(true);
            return null;
        }
        return formData;
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }
    private boolean isUsernameValid(String username) {
        return username.length() > 5;
    }
    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    View.OnClickListener loginViewListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener registerButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            String [] formData = isLoginValid();
            if(formData == null) return;
            Observable<Response<JSONAPIDocument<User>>> registrationObservable = networkService.getNetworkApi()
                    .submitSignupObservable(new JSONAPIDocument<User>(new User(formData[0], formData[1], formData[2])))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            registrationObservable.subscribe(new Observer<Response<JSONAPIDocument<User>>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    progressBar.setVisibility(View.VISIBLE); }
                @Override
                public void onNext(Response<JSONAPIDocument<User>> registrationResponse) {
                    switch(registrationResponse.code()) {
                        case 201:
                            Toast.makeText(RegisterActivity.this,
                                    getString(R.string.network_registration_success),
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            break;
                        case 400:
                            try {
                                Errors errorsResponse = ErrorUtils.parseErrorResponse(
                                        networkService.getObjectMapper(),
                                        registrationResponse.errorBody(),
                                        Errors.class);
                                //Present important errors to user.
                                for(Error error : errorsResponse.getErrors()) {
                                    switch (error.getTitle()) {
                                        case "Invalid email.":
                                            emailInput.setError(getString(R.string.error_invalid_email));
                                            emailInput.requestFocus();
                                            break;
                                        case "Invalid username.":
                                            usernameInput.setError(getString(R.string.error_invalid_username));
                                            usernameInput.requestFocus();
                                            break;
                                        case "Invalid password.":
                                            passwordInput.setError(getString(R.string.error_invalid_password));
                                            passwordInput.requestFocus();
                                            break;
                                        default:
                                            Toast.makeText(RegisterActivity.this,
                                                    getString(R.string.network_registration_failed),
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 409:
                            try {
                                Errors errorsResponse = ErrorUtils.parseErrorResponse(
                                        networkService.getObjectMapper(),
                                        registrationResponse.errorBody(),
                                        Errors.class);
                                //Present important errors to user.
                                for(Error error : errorsResponse.getErrors()) {
                                    switch (error.getTitle()) {
                                        case "This email is already in use.":
                                            emailInput.setError(getString(R.string.error_used_email));
                                            emailInput.requestFocus();
                                            break;
                                        case "This username is already in use.":
                                            usernameInput.setError(getString(R.string.error_used_username));
                                            usernameInput.requestFocus();
                                            break;
                                        default:
                                            Toast.makeText(RegisterActivity.this,
                                                    getString(R.string.network_registration_failed),
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            Toast.makeText(RegisterActivity.this,
                                    getString(R.string.network_badrequest),
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                @Override
                public void onError(Throwable e) {
                    Log.d("DEBUG","Error: " + e.toString());
                    progressBar.setVisibility(View.GONE);
                    view.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            getString(R.string.network_problem),
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onComplete() {
                    progressBar.setVisibility(View.GONE);
                    view.setEnabled(true);
                }
            });
        }
    };
}