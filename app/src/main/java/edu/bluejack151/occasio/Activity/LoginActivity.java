package edu.bluejack151.occasio.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Arrays;
import java.util.Map;

import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;

public class LoginActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private Button btnLogin;
    private TextView btnRegister;
    private EditText txtUsername, txtPassword;
    private TextInputLayout iLUsername, iLPassword;
    private Firebase myFirebaseRef;
    private LoginButton loginButton;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private InputMethodManager imm;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(LoginActivity.this); /*Facebook*/
        setContentView(R.layout.activity_login);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        layout = (LinearLayout)findViewById(R.id.ly_login_page);


        //Progress Dialog
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");

        callbackManager = CallbackManager.Factory.create();
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");

        loginButton = (LoginButton) findViewById(R.id.login_button);
        btnLogin = (Button) findViewById(R.id.btnLogin_Login);
        btnRegister = (TextView) findViewById(R.id.btnRegister_Login);
        txtUsername = (EditText) findViewById(R.id.txtUsername_Login);
        txtPassword = (EditText) findViewById(R.id.txtPassword_Login);
        iLUsername = (TextInputLayout) findViewById(R.id.input_layout_txtUsername_Login);
        iLPassword = (TextInputLayout) findViewById(R.id.input_layout_txtPassword_Login);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        checkLoginStatus();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                onFacebookAccessTokenChange(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Facebook Login Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, "Facebook Login Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            progressDialog.show();
            myFirebaseRef.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(final AuthData authData) {
                    UserData user = new UserData(authData.getProviderData().get("id").toString(), authData.getProvider(), authData.getProviderData().get("displayName").toString(), authData.getProviderData().get("email").toString(), authData.getProviderData().get("profileImageURL").toString(), null, null);
                    myFirebaseRef.child("users").child(authData.getProviderData().get("id").toString()).setValue(user, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            sharedPreferences = getSharedPreferences(String.valueOf(R.string.PREFERENCES_KEY_USER), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(String.valueOf(R.string.USER_ID), authData.getProviderData().get("id").toString());
                            editor.putString(String.valueOf(R.string.PROVIDER), authData.getProvider());
                            editor.putString(String.valueOf(R.string.USERNAME), authData.getProviderData().get("displayName").toString());
                            editor.putString(String.valueOf(R.string.EMAIL), authData.getProviderData().get("email").toString());
                            editor.putString(String.valueOf(R.string.PROFILE_IMAGE_URL), authData.getProviderData().get("profileImageURL").toString());
                            editor.putString(String.valueOf(R.string.IMAGE_STRING), "");
                            editor.putString(String.valueOf(R.string.PASSWORD), "");
                            editor.commit();
                            Toast.makeText(LoginActivity.this, "Facebook Login Success", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            intentMaster();
                        }
                    });

                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Facebook Login Error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            myFirebaseRef.unauth();
            LoginManager.getInstance().logOut();
        }
    }

    private void checkLoginStatus() {
        if (myFirebaseRef.getAuth() != null) {
            intentMaster();
        }
    }

    private void logout() {
        myFirebaseRef.unauth();
        LoginManager.getInstance().logOut();
    }

    private void login() {
        AuthData authData = myFirebaseRef.getAuth();
        if (authData != null) {
            logout();
        } else {
            if (validateUsername() && validatePassword()) {
                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                progressDialog.show();
                Query queryRef = myFirebaseRef.child("users").orderByChild("username").equalTo(txtUsername.getText().toString());
                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() < 1) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "There is no such a Username", Toast.LENGTH_SHORT).show();
                        } else if (dataSnapshot.getChildrenCount() == 1) {
                            UserData user = dataSnapshot.getChildren().iterator().next().getValue(UserData.class);
                            if (user.getPassword().equals(txtPassword.getText().toString())) {
                                firebaseAuthWithPassword(user, progressDialog);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Username and Password Unmatched", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Username Redundant", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        }
    }

    private void firebaseCreateUser(UserData user) {
        myFirebaseRef.createUser(user.getEmail(), user.getPassword(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Toast.makeText(LoginActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(LoginActivity.this, firebaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void firebaseAuthWithPassword(final UserData user, final ProgressDialog progressDialog) {
        myFirebaseRef.authWithPassword(user.getEmail(), user.getPassword(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                sharedPreferences = getSharedPreferences(String.valueOf(R.string.PREFERENCES_KEY_USER), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(String.valueOf(R.string.USER_ID), user.getUserId());
                editor.putString(String.valueOf(R.string.PROVIDER), user.getProvider());
                editor.putString(String.valueOf(R.string.USERNAME), user.getUsername());
                editor.putString(String.valueOf(R.string.EMAIL), user.getEmail());
                editor.putString(String.valueOf(R.string.PROFILE_IMAGE_URL), user.getProfileImageURL());
                editor.putString(String.valueOf(R.string.IMAGE_STRING), user.getImageString());
                editor.putString(String.valueOf(R.string.PASSWORD), user.getPassword());
                editor.commit();
                intentMaster();
                progressDialog.dismiss();

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Toast.makeText(LoginActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    public void intentMaster() {
        Intent intent = new Intent(LoginActivity.this, MasterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateUsername() {
        String username = txtUsername.getText().toString().trim();
        if (username.equals("")) {
            iLUsername.setError("Username must be filled");
            return false;
        } else {
            iLUsername.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        String password = txtPassword.getText().toString().trim();
        if (password.equals("")) {
            iLPassword.setError("Password must be filled");
            return false;
        } else {
            iLPassword.setErrorEnabled(false);
        }
        return true;
    }
}
