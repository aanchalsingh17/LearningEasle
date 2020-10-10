package com.example.learningeasle;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learningeasle.model.Adapter;
import com.example.learningeasle.model.modelpost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment  {
    ImageView profile;
    FirebaseUser user;
    String userid;
    StorageReference reference;
    TextView username, useremail, userstatus;
    FirebaseFirestore fstore;
    String userID;
    FirebaseAuth fAuth;
    Activity context;
    Button editprofile;
    RecyclerView postlist;
    List<modelpost> modelpostList;
    Adapter adapterPost;
    ImageView more;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profile = view.findViewById(R.id.image);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();//
        reference = FirebaseStorage.getInstance().getReference();
        editprofile = view.findViewById(R.id.editprofile);
        username = view.findViewById(R.id.username);
        useremail = view.findViewById(R.id.email);
        userstatus = view.findViewById(R.id.status);
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        postlist = view.findViewById(R.id.posts);
        more = view.findViewById(R.id.more);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postlist.setLayoutManager(layoutManager);
        modelpostList = new ArrayList<>();
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,UpdateProfile.class));
            }
        });
        loadPosts();
        setProfile();
        /*Adapter.EditClick editClick = new Adapter.EditClick() {
            @Override
            public void onEditClicked(String Name, String url, String Title, String Description, String Image, String Timestamp, String Likes) {
                PostFragment postFragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Name",Name);
                bundle.putString("url",url);
                bundle.putString("Title",Title);
                bundle.putString("Description",Description);
                bundle.putString("Image",Image);
                bundle.putString("TimeStamp",Timestamp);
                bundle.putString("Likes",Likes);
                postFragment.setArguments(bundle);
            }
        };*/
        return view;

    }

    private void loadPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelpostList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    final HashMap<Object, String> hashMap = (HashMap<Object, String>) dataSnapshot.getValue();
                    modelpost post=null;
                    if (hashMap.get("pLikes")==null&&hashMap.get("pId").equals(userID)) {
                        post = new modelpost(hashMap.get("pId"), hashMap.get("pImage"), hashMap.get("pTitle"), hashMap.get("pDesc"),
                                hashMap.get("pTime"), hashMap.get("pName"), hashMap.get("url"), "0");
                    } else if(hashMap.get("pId").equals(userID)){
                        post = new modelpost(hashMap.get("pId"), hashMap.get("pImage"), hashMap.get("pTitle"), hashMap.get("pDesc"),
                                hashMap.get("pTime"), hashMap.get("pName"), hashMap.get("url"), hashMap.get("pLikes"));
                    }
                    if(post!=null)
                    modelpostList.add(post);
                }

                adapterPost = new Adapter(getActivity(), modelpostList);
                postlist.setAdapter(adapterPost);

                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );



    }

    private void setProfile() {
        StorageReference fileref = reference.child("Users/" + userid + "/Images.jpeg");
        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(profile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profile.setImageResource(R.drawable.ic_action_account);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, FullView.class));
            }
        });
        userID = fAuth.getCurrentUser().getUid();                                                           //user id stored

        final DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = (String) documentSnapshot.get("fName");
                String email = (String) documentSnapshot.get("email");
                String status = (String) documentSnapshot.get("status");
                username.setText(name);
                useremail.setText(email);
                userstatus.setText(status);
            }
        });


    }



}