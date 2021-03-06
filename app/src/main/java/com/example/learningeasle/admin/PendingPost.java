package com.example.learningeasle.admin;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.learningeasle.R;
import com.example.learningeasle.model.modelpost;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PendingPost extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    List<modelpost> modelpostList;
    AdapterPendingPost adapterPendingPost;
    ShimmerFrameLayout shimmerFrameLayout;

    boolean notify = false;
    public PendingPost() {
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
        View view = inflater.inflate(R.layout.fragment_pending_post, container, false);
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        modelpostList = new ArrayList<>();
        setHasOptionsMenu(true);

        //create api service

        shimmerFrameLayout.startShimmer();
        //Load all the pending post from the admins pending post section
        loadAllPendingPost();
        return  view;
    }
    private void loadAllPendingPost() {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("admin").child("pendingpost");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelpostList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    HashMap<Object,String> hashMap = (HashMap<Object, String>) ds.getValue();
                    modelpost post = new modelpost(hashMap.get("pId").toString(), hashMap.get("pImage").toString(), hashMap.get("pTitle").toString(), hashMap.get("pDesc").toString(),
                            hashMap.get("pTime").toString(), hashMap.get("pName").toString(), hashMap.get("url").toString(), "0",
                            "0", hashMap.get("type").toString(),
                            hashMap.get("videourl").toString(),hashMap.get("pdfurl").toString(),hashMap.get("audiourl").toString());
                    modelpostList.add(post);
                    //onDataReceiveCallback.onDataReceived(modelpostList);
                }
                adapterPendingPost = new AdapterPendingPost(getActivity(),modelpostList);
                recyclerView.setAdapter(adapterPendingPost);
                recyclerView.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               // progressDialog.dismiss();

            }
        });

    }
}