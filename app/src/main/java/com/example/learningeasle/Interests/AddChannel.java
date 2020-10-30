package com.example.learningeasle.Interests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.learningeasle.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddChannel extends AppCompatActivity {
    ImageView coverImage;
    EditText title,Des;
    Button add_channel;
    Uri imageuri = null;
    boolean admin = false;
    String uid;
    String channelName,description,imageurl="empty";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    String[] cameraPermissions;
    String[] storagePermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_channel);
        coverImage = findViewById(R.id.coverImage);
        title = findViewById(R.id.title);
        Des = findViewById(R.id.des);


        add_channel = findViewById(R.id.add_channel);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.
                WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.
                WRITE_EXTERNAL_STORAGE};

        //Check if User is admin or not;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            uid = user.getUid();
        }
        DatabaseReference adminref = FirebaseDatabase.getInstance().getReference("admin").child("Id");
        adminref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(uid)){
                    admin = true;
                    add_channel.setText("Add Channel");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        add_channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                channelName = title.getText().toString();
                description = Des.getText().toString();
                if(imageuri==null||channelName.isEmpty()||description.isEmpty()) {
                    if (imageuri == null) {
                        Toast.makeText(AddChannel.this, "Add Cover Image", Toast.LENGTH_SHORT).show();
                    }

                    if (channelName.isEmpty()) {
                        Toast.makeText(AddChannel.this, "Add Channel Name", Toast.LENGTH_SHORT).show();
                    }
                    if (description.isEmpty()) {
                        Toast.makeText(AddChannel.this, "Add Channel Description", Toast.LENGTH_SHORT).show();
                    }
                }
                    //sendChannelRequest();
                    else {
                    DatabaseReference allChannels = FirebaseDatabase.getInstance().getReference("admin").child("channel");
                    allChannels.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(channelName)) {
                                Toast.makeText(AddChannel.this, "Channel Name Exist", Toast.LENGTH_SHORT).show();
                            } else {
                                uploadImagetoFirebase();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });
        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image_Dialog();
            }
        });
    }

    private void uploadImagetoFirebase() {
        if(imageuri!=null){
            String path = "Channel/"+channelName;
            final StorageReference image = FirebaseStorage.getInstance().getReference().child(path);
            image.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageurl = uri.toString();
                            sendChannelRequest();
                        }
                    });
                }
            });

        }
    }

    private void Image_Dialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddChannel.this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (!checkCameraPermission())
                        requestCameraPermission();
                    else
                        pickFromCamera();
                }

                if (i == 1) {
                    if (!checkStoragePermission())
                        requestStoragePermission();
                    else
                        pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions,
                STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean reuslt = ContextCompat.checkSelfPermission(this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return reuslt;
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Desc");
        imageuri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        //Start activity for result with request code
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(AddChannel.this, Manifest.permission.
                CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(AddChannel.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions,
                CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                        pickFromCamera();
                    else
                        Toast.makeText(this, "Camera and Storage permissions are required...",
                                Toast.LENGTH_SHORT).show();
                } else {


                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted)
                        pickFromGallery();
                    else
                        Toast.makeText(this, "Storage permission is required...",
                                Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            //If image is picked from gallery then create is uri and then set it into image view
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageuri = data.getData();
                coverImage.setImageURI(imageuri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                coverImage.setImageURI(imageuri);
            }
        }
    }

    private void sendChannelRequest() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("cName", channelName);
        hashMap.put("cDes", description);
        hashMap.put("cUrl", imageurl);
        if (admin) {
            final DatabaseReference channelReference = FirebaseDatabase.getInstance().getReference("admin").child("channel");
            channelReference.child(channelName).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AddChannel.this,"New Channel Added",Toast.LENGTH_SHORT).show();
                    //Whenever Admin Add new Channel  add the channel into the users profile
                    final DatabaseReference user_profile = FirebaseDatabase.getInstance().getReference("Users");
                    user_profile.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot user:snapshot.getChildren()){
                                String user_email = (String) user.child("email").getValue();
                                String Id = (String) user.child("Id").getValue();
                                int j=user_email.length()-4;
                                final String username=user_email.substring(0,j);
                                user_profile.child(Id).child(username).child(channelName).setValue("0");

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    Des.setText("");
                    title.setText("");
                    imageuri = null;
                    coverImage.setImageResource(R.drawable.border_image);
                }
            });

        } else {
            final DatabaseReference channelReference = FirebaseDatabase.getInstance().getReference("admin").child("pendingchannel");
            channelReference.child(channelName).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AddChannel.this, "New Channel Request Send to Admin", Toast.LENGTH_SHORT).show();
                    Des.setText("");
                    title.setText("");
                    imageuri = null;
                    coverImage.setImageResource(R.drawable.border_image);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddChannel.this, "Unable to send Request", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}