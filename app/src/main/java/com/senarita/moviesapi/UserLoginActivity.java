package com.senarita.moviesapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class UserLoginActivity extends AppCompatActivity implements  View.OnClickListener {


    protected EditText usernameEditText;
    protected EditText passwordEditText;
    protected Button loginButton;


    protected TextView signUpTextView;


    private static final String LOG_TAG = UserLoginActivity.class.getSimpleName();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        //ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        signUpTextView = (TextView)findViewById(R.id.signUpText);
        usernameEditText = (EditText)findViewById(R.id.usernameField);
        passwordEditText = (EditText)findViewById(R.id.passwordField);
        loginButton = (Button)findViewById(R.id.loginButton);


        signUpTextView.setOnClickListener(this);
        loginButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpText:
                signUp();

                break;
            case R.id.loginButton:
                login();
                break;


        }
    }



    private void login() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        username = username.trim().toLowerCase();
        password = password.trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this);
            builder.setMessage(R.string.login_error_message)
                    .setTitle(R.string.login_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            setProgressBarIndeterminateVisibility(true);

            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    setProgressBarIndeterminateVisibility(false);

                    if (e == null) {

                        Log.d(LOG_TAG,"User logged in successfully");
//
                        // Success!
                        Intent intent = new Intent(UserLoginActivity.this, MovieListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // Fail
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this);
                        builder.setMessage(MovieListActivity.capitalize(e.getMessage()))
                                .setTitle(R.string.login_error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
    }

    private void signUp() {

        Intent intent = new Intent(UserLoginActivity.this, NewUserRegister.class);
        startActivity(intent);
    }




}
