package edu.bluejack151.occasio.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import edu.bluejack151.occasio.Class.UserData;
import edu.bluejack151.occasio.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int LOADIMAGE_RESULT = 1;
    private String encodedImage = "";
    private Button btnRegister, btnChooseFile;
    private TextView lblError;
    private TextInputLayout iLEmail, iLUsername, iLPassword;
    private EditText txtEmail, txtUsername, txtPassword;
    private ImageView imgvProfilePicture;
    private ProgressDialog progressDialog;
    private Firebase myFirebaseRef;
    private InputMethodManager imm;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btnRegister = (Button) findViewById(R.id.btnRegister_Register);
        iLEmail = (TextInputLayout) findViewById(R.id.input_layout_txtEmail_Register);
        iLUsername = (TextInputLayout) findViewById(R.id.input_layout_txtUsername_Register);
        iLPassword = (TextInputLayout) findViewById(R.id.input_layout_txtPassword_Register);
        lblError = (TextView) findViewById(R.id.lblError_Image);
        txtEmail = (EditText) findViewById(R.id.txtEmail_Register);
        txtUsername = (EditText) findViewById(R.id.txtUsername_Register);
        txtPassword = (EditText) findViewById(R.id.txtPassword_Register);
        btnChooseFile = (Button) findViewById(R.id.btnChooseFile_Register);
        imgvProfilePicture = (ImageView) findViewById(R.id.imgvProfilePicture_Register);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        layout = (LinearLayout)findViewById(R.id.ly_register_page);
        myFirebaseRef = new Firebase("https://luminous-inferno-5595.firebaseio.com/");

        btnRegister.setOnClickListener(this);
        btnChooseFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister_Register:
                if (validateEmail() && validateUsername() && validatePassword() && validateImage()) {
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Registering...");
                    progressDialog.show();
                    final String generatedKey = myFirebaseRef.child("users").push().getKey();
                    final UserData user = new UserData(generatedKey, "password", txtUsername.getText().toString(), txtEmail.getText().toString(), null, encodedImage, txtPassword.getText().toString());
                    Query queryRef = myFirebaseRef.child("users").orderByChild("email").equalTo(txtEmail.getText().toString());
                    queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getChildrenCount() == 0) {
                                Query queryRef = myFirebaseRef.child("users").orderByChild("username").equalTo(txtUsername.getText().toString());
                                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() == 0) {
                                            myFirebaseRef.child("users").child(generatedKey).setValue(user, new Firebase.CompletionListener() {
                                                @Override
                                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                    if (firebaseError == null) {
                                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                        finish();
                                                        Toast.makeText(RegisterActivity.this, "Register Success", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        firebaseCreateUser(user);
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, firebaseError.toString(), Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Username Already Exists", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                        progressDialog.dismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Email Already Exists", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(RegisterActivity.this, firebaseError.toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
                break;
            case R.id.btnChooseFile_Register:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, LOADIMAGE_RESULT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOADIMAGE_RESULT && resultCode == RESULT_OK && data != null) {
            Uri tempImage = data.getData();
            imgvProfilePicture.setImageURI(tempImage);
            Bitmap bitImage = Bitmap.createScaledBitmap(((BitmapDrawable) imgvProfilePicture.getDrawable()).getBitmap(), 200, 200, false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        }
    }

    private boolean validateEmail() {
        String email = txtEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (email.equals("")) {
            iLEmail.setError("Email must be filled");
            return false;
        } else if (!email.matches(emailPattern)) {
            iLEmail.setError("Enter valid email address");
            return false;
        } else {
            iLEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateUsername() {
        String username = txtUsername.getText().toString().trim();
        if (username.equals("")) {
            iLUsername.setError("Username must be filled");
            return false;
        } else if (username.length() < 5) {
            iLUsername.setError("Username must more than 5 characters");
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
        } else if (password.length() < 5) {
            iLPassword.setError("Password must more than 5 characters");
            return false;
        } else {
            iLPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateImage() {
        if (encodedImage.equals("")) {
            lblError.setText("Must Choose Profile Picture");
            lblError.setVisibility(View.VISIBLE);
            return false;
        } else {
            lblError.setText("");
            lblError.setVisibility(View.GONE);
        }
        return true;
    }

    private void firebaseCreateUser(UserData user) {
        myFirebaseRef.createUser(user.getEmail(), user.getPassword(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(RegisterActivity.this, firebaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
