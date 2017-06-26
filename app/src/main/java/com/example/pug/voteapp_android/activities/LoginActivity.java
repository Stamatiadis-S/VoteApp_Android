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

import com.example.pug.voteapp_android.models.Token;
import com.example.pug.voteapp_android.network.NetworkService;
import com.example.pug.voteapp_android.R;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private NetworkService networkService;

    private ProgressBar progressView;
    private EditText usernameView;
    private EditText passwordView;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = this.getSharedPreferences("com.example.voteapp_android", Context.MODE_PRIVATE);
        networkService = new NetworkService(prefs.getString("SERVER_URL", "http://192.168.1.101:9000/"));
        initializeViews();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeViews() {
        progressView = (ProgressBar) findViewById(R.id.login_progress);
        progressView.setIndeterminate(true);
        usernameView = (EditText) findViewById(R.id.login_username);
        passwordView = (EditText) findViewById(R.id.login_password);
        TextView signupView = (TextView) findViewById(R.id.login_register_prompt);
        signupView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(loginButtonListener);

    }
    private void makeToastShort(int msgId) {
        Toast.makeText(LoginActivity.this,
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    private String[] isLoginValid() {
        String[] formData = new String[2];
        formData[0] = usernameView.getText().toString();
        formData[1] = passwordView.getText().toString();

        loginButton.setEnabled(false);
        usernameView.setError(null);
        passwordView.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(formData[1]) || !isPasswordValid(formData[1])) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }
        // Check for a username.
        if (TextUtils.isEmpty(formData[0]) || !isUsernameValid(formData[0])) {
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            loginButton.setEnabled(true);
            return null;
        }
        return formData;
    }
    private boolean isUsernameValid(String username) {
        return username.length() > 5;
    }
    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    View.OnClickListener loginButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            final String [] formData = isLoginValid();
            if (formData == null) {
                return;
            }
            Observable<Response<Token>> loginObservable = networkService.getNetworkApi()
                    .submitLoginObservable("password", formData[0], formData[1])
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            loginObservable.subscribe(new Observer<Response<Token>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    progressView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onNext(Response<Token> tokenResponse) {
                    switch(tokenResponse.code()) {
                        case 200:
                            prefs.edit().putString("AUTHENTICATION_TOKEN", tokenResponse.body().getAccessToken()).apply();
                            prefs.edit().putString("USERNAME", formData[0]).apply();
                            makeToastShort(R.string.network_login_success);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            break;
                        case 401:
                            makeToastShort(R.string.network_login_unauthorized);
                            break;
                        default:
                            makeToastShort(R.string.network_badrequest);
                            break;
                    }
                }
                @Override
                public void onError(Throwable e) {
                    Log.d("DEBUG","Error: " + e.toString());
                    progressView.setVisibility(View.GONE);
                    view.setEnabled(true);
                    makeToastShort(R.string.network_problem);
                }
                @Override
                public void onComplete() {
                    progressView.setVisibility(View.GONE);
                    view.setEnabled(true);
                }
            });
        }
    };
}

