package com.example.learningeasle.Accounts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learningeasle.MainActivity;
import com.example.learningeasle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Register extends AppCompatActivity {
    EditText name_reg,email_reg, password_reg,phone_reg;
    TextView loginBtn_reg;
    Button createBtn_reg;
    ProgressBar progressBar_reg;
    FirebaseAuth fAuth_reg;
    String userID;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    SharedPreferences sharedPreferences;
    //FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name_reg        = findViewById(R.id.name_reg);
        email_reg       = findViewById(R.id.email_reg);
        password_reg    = findViewById(R.id.password_reg);
        phone_reg       = findViewById(R.id.mobile_reg);
        createBtn_reg   = findViewById(R.id.register_reg);
        loginBtn_reg    = findViewById(R.id.login_reg);
        progressBar_reg = findViewById(R.id.progressBar_reg);
        fAuth_reg       = FirebaseAuth.getInstance();
        fUser           =   fAuth_reg.getCurrentUser();
        fStore          = FirebaseFirestore.getInstance();

        //If fuser is not null start main Activity
        if(fUser!= null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        createBtn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(Register.this);
                if (isNetworkConnected() && isInternetAvailable()) {
                    final String email = email_reg.getText().toString().trim();
                    final String password = password_reg.getText().toString().trim();
                    final String fullName = name_reg.getText().toString();
                    final String phone = phone_reg.getText().toString();

                    //Throw error if any of the fields is invalid
                    if (TextUtils.isEmpty(fullName)) {
                        name_reg.setError("Username is Required.");
                        return;
                    }

                    if (TextUtils.isEmpty(email)) {
                        email_reg.setError("Email is Required.");
                        return;
                    }

                    if (TextUtils.isEmpty(phone)) {
                        phone_reg.setError("Phone Number is Required.");
                        return;
                    }

                    if (phone.length() < 10) {
                        phone_reg.setError("Enter valid Phone Number");
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        password_reg.setError("Password is Required.");
                        return;
                    }

                    if (password.length() < 6) {
                        password_reg.setError("Password Must be >= 6 Characters");
                        return;
                    }
                    SharedPreferences preferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("email_Id", email);
                    editor.commit();
                    progressBar_reg.setVisibility(View.VISIBLE);
                    //Create user with the email and password
                    fAuth_reg.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser fuser = fAuth_reg.getCurrentUser();
                                final String Uid = fuser.getUid();

                                Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();

                                //User Data Added to  Firebase
                                userID = fAuth_reg.getCurrentUser().getUid();
                                int j = email.length() - 4;
                                final String username = email.substring(0, j);                                                          //user id stored
                                final HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("Name", fullName);
                                hashMap.put("Id", Uid);
                                hashMap.put("Url", "empty");
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "none");
                                hashMap.put("email", email);
                                hashMap.put("phone", phone);
                                hashMap.put("status", "");
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(Uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });

                                // after registration redirect to main class
//                                sharedPreferences=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
//                                String folder = sharedPreferences.getString("email_Id", "");
//                                int j=folder.length()-4;
//                                final String username=folder.substring(0,j);

                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                FirebaseUser users = firebaseAuth.getCurrentUser();
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //Users Interest field also added to firebase
                                final DatabaseReference myRef = database.getReference().child("Users").child(userID).child(username);
                                DatabaseReference channelRef = database.getReference().child("admin").child("channel");
                                channelRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot channels : snapshot.getChildren()) {
                                            myRef.child(channels.getKey()).setValue("0");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //After Registration is done redirect to profile class to upload profile photo
                                startActivity(new Intent(getApplicationContext(), Profile.class));
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                                finish();


                            } else if (task.getException() != null) {
                                Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar_reg.setVisibility(View.GONE);
                            }

                        }
                    });
                }
                else{
                    Toast.makeText(Register.this,"Please connect to internet!",Toast.LENGTH_LONG).show();
                }
            }

        });

        //when login button is pressed

        loginBtn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                finish();

            }
        });

    }

    public boolean isNetworkConnected(){
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public boolean isInternetAvailable(){
        try{
            InetAddress inetAddress=InetAddress.getByName("google.com");
            return !inetAddress.equals("");
        } catch (UnknownHostException e) {
            return false;
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}