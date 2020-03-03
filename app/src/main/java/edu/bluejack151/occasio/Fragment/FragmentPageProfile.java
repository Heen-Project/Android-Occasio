package edu.bluejack151.occasio.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.net.URL;

import edu.bluejack151.occasio.Activity.LoginActivity;
import edu.bluejack151.occasio.Activity.MasterActivity;
import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;


public class FragmentPageProfile extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private View v;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private Firebase myFirebaseRef;
    private AuthData authData;
    private UserData userData;
    private Bitmap bitmapProfilePic;
    private LinearLayout lt_changeEmail, lt_changePassword, lt_removeAccount, ltgroup_password, ltgroup_facebook;
    private TextInputLayout iLtxtChangeEmail_newEmail, iLtxtChangeEmail_newEmailVerification, iLtxtChangeEmail_password, iLtxtChangePassword_oldPassword, iLtxtChangePassword_newPassword, iLtxtChangePassword_newPasswordVerification, iLtxtRemoveAccount_password;
    private Button btnChangeEmail, btnChangePassword, btnRemoveAccount;
    private EditText txtChangeEmail_newEmail, txtChangeEmail_newEmailVerification, txtChangeEmail_password, txtChangePassword_oldPassword, txtChangePassword_newPassword, txtChangePassword_newPasswordVerification, txtRemoveAccount_password;
    private Switch swChangeEmail, swChangePassword, swRemoveAccount;
    private TextView lblUsername, lblEmail, lblFacebookUsername, lblFacebookEmail;
    private ImageView imgvProfilePicture, imgvFacebookProfilePicture;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private final String DEFAULT_VALUE = "";
    private long checkImages = 0, totalImages = 0, checkLikes = 0, totalLikes = 0, checkComments = 0, totalComments = 0;
    private InputMethodManager imm;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_page_profile, container, false);
        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        initComponent();
        return v;
    }

    private void initComponent() {
        sharedPreferences = getActivity().getSharedPreferences(String.valueOf(R.string.PREFERENCES_KEY_USER), Context.MODE_PRIVATE);
        ltgroup_password = (LinearLayout) v.findViewById(R.id.lt_group_passwordProvider_fProfile);
        ltgroup_facebook = (LinearLayout) v.findViewById(R.id.lt_group_facebookProvider_fProfile);
        lt_changePassword = (LinearLayout) v.findViewById(R.id.lt_changePassword_fProfile);
        lt_removeAccount = (LinearLayout) v.findViewById(R.id.lt_removeAccount_fProfile);
        iLtxtChangePassword_oldPassword = (TextInputLayout) v.findViewById(R.id.input_layout_txtOldPassword_changePassword_fProfile);
        iLtxtChangePassword_newPassword = (TextInputLayout) v.findViewById(R.id.input_layout_txtNewPassword_changePassword_fProfile);
        iLtxtChangePassword_newPasswordVerification = (TextInputLayout) v.findViewById(R.id.input_layout_txtNewPasswordVerification_changePassword_fProfile);
        iLtxtRemoveAccount_password = (TextInputLayout) v.findViewById(R.id.input_layout_txtPassword_removeAccount_fProfile);
        btnChangePassword = (Button) v.findViewById(R.id.btnChangePassword_fProfile);
        btnRemoveAccount = (Button) v.findViewById(R.id.btnRemoveAccount_fProfile);
        txtChangePassword_oldPassword = (EditText) v.findViewById(R.id.txtOldPassword_changePassword_fProfile);
        txtChangePassword_newPassword = (EditText) v.findViewById(R.id.txtNewPassword_changePassword_fProfile);
        txtChangePassword_newPasswordVerification = (EditText) v.findViewById(R.id.txtNewPasswordVerification_changePassword_fProfile);
        txtRemoveAccount_password = (EditText) v.findViewById(R.id.txtPassword_removeAccount_fProfile);
        swChangePassword = (Switch) v.findViewById(R.id.sw_changePassword_fProfile);
        swRemoveAccount = (Switch) v.findViewById(R.id.sw_removeAccount_fProfile);
        lblUsername = (TextView) v.findViewById(R.id.profileUsername_fProfile);
        lblEmail = (TextView) v.findViewById(R.id.profileEmail_fProfile);
        lblFacebookUsername = (TextView) v.findViewById(R.id.profileUsername_facebook_fProfile);
        lblFacebookEmail = (TextView) v.findViewById(R.id.email_facebook_fProfile);
        imgvProfilePicture = (ImageView) v.findViewById(R.id.profilePicture_fProfile);
        imgvFacebookProfilePicture = (ImageView) v.findViewById(R.id.profilePicture_facebook_fProfile);
        bitmapProfilePic = null;
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");
        authData = myFirebaseRef.getAuth();
        initUserData();
        if (myFirebaseRef.getAuth() != null) {
            if (myFirebaseRef.getAuth().getProvider().toString().equals("facebook")) {
                ltgroup_facebook.setVisibility(View.VISIBLE);
                ltgroup_password.setVisibility(View.GONE);
            } else if (myFirebaseRef.getAuth().getProvider().toString().equals("password")) {
                ltgroup_facebook.setVisibility(View.GONE);
                ltgroup_password.setVisibility(View.VISIBLE);
            }
            switch_disabled();
            swChangePassword.setOnCheckedChangeListener(this);
            swRemoveAccount.setOnCheckedChangeListener(this);
            btnChangePassword.setOnClickListener(this);
            btnRemoveAccount.setOnClickListener(this);
        }


    }

    private void checkUserData() {
        if (userData.getProvider().equals("password")) {
            byte[] decodedString = Base64.decode(userData.getImageString(), Base64.DEFAULT);
            bitmapProfilePic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imgvProfilePicture.setImageBitmap(bitmapProfilePic);
            lblUsername.setText(userData.getUsername());
            lblEmail.setText(userData.getEmail());
        } else if (userData.getProvider().equals("facebook")) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                URL url = new URL(userData.getProfileImageURL());
                bitmapProfilePic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imgvFacebookProfilePicture.setImageBitmap(bitmapProfilePic);
                lblFacebookUsername.setText(userData.getUsername());
                lblFacebookEmail.setText(userData.getEmail());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void switch_toggle(LinearLayout layout) {
        switch_disabled();
        layout.setVisibility(View.VISIBLE);
    }

    private void switch_disabled() {
        lt_changePassword.setVisibility(View.GONE);
        lt_removeAccount.setVisibility(View.GONE);
        swChangePassword.setChecked(false);
        swRemoveAccount.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        String email = "";
        if (myFirebaseRef.getAuth() != null) {
            email = myFirebaseRef.getAuth().getProviderData().get("email").toString();
        }
        switch (v.getId()) {
            case R.id.btnChangePassword_fProfile:
                if (fnChangePassword_oldPassword() && fnChangePassword_newPassword() && fnChangePassword_newPasswordVerification()) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Changing your password...");
                    progressDialog.show();
                    myFirebaseRef.changePassword(email, txtChangePassword_oldPassword.getText().toString(), txtChangePassword_newPassword.getText().toString(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            UserData userTemp = userData;
                            userTemp.setPassword(txtChangePassword_newPassword.getText().toString());
                            myFirebaseRef.child("users").child(userData.getUserId()).setValue(userTemp);
                            userData = userTemp;
                            Toast.makeText(getContext(), "Password Changed", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            intentBack(1);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getContext(), firebaseError.toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
                break;
            case R.id.btnRemoveAccount_fProfile:
                checkImages = checkLikes = checkComments = 0;
                if (fnRemoveAccount_password()) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Removing your account...");
                    progressDialog.show();
                    myFirebaseRef.removeUser(email, txtRemoveAccount_password.getText().toString(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            myFirebaseRef.child("users").child(userData.getUserId()).setValue(null, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError == null) {
                                        myFirebaseRef.child("images").orderByChild("userId").equalTo(userData.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                totalImages = dataSnapshot.getChildrenCount();
                                                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                    myFirebaseRef.child("images").child(tempDataSnapshot.getKey()).setValue(null);
                                                    checkImages++;
                                                    fnCheckSuccessRemove(progressDialog);
                                                }
                                                fnCheckSuccessRemove(progressDialog);
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        });
                                        myFirebaseRef.child("likes").orderByChild("userId").equalTo(userData.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                totalLikes = dataSnapshot.getChildrenCount();
                                                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                    myFirebaseRef.child("likes").child(tempDataSnapshot.getKey()).setValue(null);
                                                    checkLikes++;
                                                    fnCheckSuccessRemove(progressDialog);
                                                }
                                                fnCheckSuccessRemove(progressDialog);
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        });
                                        myFirebaseRef.child("comments").orderByChild("userCommentatorId").equalTo(userData.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                totalComments = dataSnapshot.getChildrenCount();
                                                for (DataSnapshot tempDataSnapshot : dataSnapshot.getChildren()) {
                                                    myFirebaseRef.child("comments").child(tempDataSnapshot.getKey()).setValue(null);
                                                    checkComments++;
                                                    fnCheckSuccessRemove(progressDialog);
                                                }
                                                fnCheckSuccessRemove(progressDialog);
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Remove Account Failed", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), firebaseError.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
    }

    private void fnCheckSuccessRemove(ProgressDialog progressDialog) {
        if (checkImages >= totalImages && checkLikes >= totalLikes && checkComments >= totalComments) {
            Toast.makeText(getContext(), "User Removed", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            intentBack(2);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_changePassword_fProfile:
                if (isChecked) {
                    switch_toggle(lt_changePassword);
                    swChangePassword.setChecked(true);
                } else {
                    lt_changePassword.setVisibility(View.GONE);
                }
                break;
            case R.id.sw_removeAccount_fProfile:
                if (isChecked) {
                    switch_toggle(lt_removeAccount);
                    swRemoveAccount.setChecked(true);
                } else {
                    lt_removeAccount.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void intentBack(int i) {
        Intent intent;
        if (i == 1) {
            intent = new Intent(getContext(), MasterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (i == 2) {
            LoginManager.getInstance().logOut();
            myFirebaseRef.unauth();
            intent = new Intent(getContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        getActivity().finish();
    }

    private void fillDetailFacebook() {
        lblFacebookEmail.setText("User Id : " + authData.getProviderData().get("id").toString() + "\n" +
                "Provider : " + authData.getProvider() + "\n" +
                "Username : " + authData.getProviderData().get("displayName").toString() + "\n" +
                "Email : " + authData.getProviderData().get("email").toString() + "\n");
    }


    private void initUserData() {
        if (myFirebaseRef.getAuth() != null) {
            userData = new UserData(sharedPreferences.getString(String.valueOf(R.string.USER_ID), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROVIDER), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.USERNAME), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.EMAIL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PROFILE_IMAGE_URL), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.IMAGE_STRING), DEFAULT_VALUE)
                    , sharedPreferences.getString(String.valueOf(R.string.PASSWORD), DEFAULT_VALUE));
            checkUserData();
        } else {
            startActivity(new Intent(getContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            getActivity().finish();
        }
    }

    private boolean fnChangeEmail_newEmail() {
        String email = txtChangeEmail_newEmail.getText().toString().trim();
        if (email.equals("")) {
            iLtxtChangeEmail_newEmail.setError("Email must be filled");
            return false;
        } else if (!email.matches(emailPattern)) {
            iLtxtChangeEmail_newEmail.setError("Enter valid email address");
            return false;
        } else {
            iLtxtChangeEmail_newEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean fnChangeEmail_newEmailVerification() {
        String email = txtChangeEmail_newEmailVerification.getText().toString().trim();
        if (email.equals("")) {
            iLtxtChangeEmail_newEmailVerification.setError("Email must be filled");
            return false;
        } else if (!email.matches(emailPattern)) {
            iLtxtChangeEmail_newEmailVerification.setError("Enter valid email address");
            return false;
        } else {
            iLtxtChangeEmail_newEmailVerification.setErrorEnabled(false);
        }
        return true;
    }

    private boolean fnChangeEmail_password() {
        String password = txtChangeEmail_password.getText().toString().trim();
        if (password.equals("")) {
            iLtxtChangeEmail_password.setError("Password must be filled");
            return false;
        } else if (password.length() < 5) {
            iLtxtChangeEmail_password.setError("Password must more than 5 characters");
            return false;
        } else {
            iLtxtChangeEmail_password.setErrorEnabled(false);
        }
        return true;
    }

    private boolean fnChangePassword_oldPassword() {
        String password = txtChangePassword_oldPassword.getText().toString().trim();
        if (password.equals("")) {
            iLtxtChangePassword_oldPassword.setError("Password must be filled");
            return false;
        } else if (password.length() < 5) {
            iLtxtChangePassword_oldPassword.setError("Password must more than 5 characters");
            return false;
        } else {
            iLtxtChangePassword_oldPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean fnChangePassword_newPassword() {
        String password = txtChangePassword_newPassword.getText().toString().trim();
        if (password.equals("")) {
            iLtxtChangePassword_newPassword.setError("Password must be filled");
            return false;
        } else if (password.length() < 5) {
            iLtxtChangePassword_newPassword.setError("Password must more than 5 characters");
            return false;
        } else {
            iLtxtChangePassword_newPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean fnChangePassword_newPasswordVerification() {
        String password = txtChangePassword_newPasswordVerification.getText().toString().trim();
        if (password.equals("")) {
            iLtxtChangePassword_newPasswordVerification.setError("Password must be filled");
            return false;
        } else if (password.length() < 5) {
            iLtxtChangePassword_newPasswordVerification.setError("Password must more than 5 characters");
            return false;
        } else if (!txtChangePassword_newPassword.getText().toString().trim().equals(password)) {
            iLtxtChangePassword_newPasswordVerification.setError("Verification password not match with new password ");
            return false;
        } else {
            iLtxtChangePassword_newPasswordVerification.setErrorEnabled(false);
        }
        return true;
    }

    private boolean fnRemoveAccount_password() {
        String password = txtRemoveAccount_password.getText().toString().trim();
        if (password.equals("")) {
            iLtxtRemoveAccount_password.setError("Password must be filled");
            return false;
        } else if (password.length() < 5) {
            iLtxtRemoveAccount_password.setError("Password must more than 5 characters");
            return false;
        } else {
            iLtxtRemoveAccount_password.setErrorEnabled(false);
        }
        return true;
    }
}
