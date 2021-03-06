package com.example.learningeasle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Picasso;

public class ViewImage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        //get the image url from the intent and show the full image view of post image
        String image=getIntent().getStringExtra("image");
        ImageView imageView=findViewById(R.id.imageView);
        if (image.equals("noImage")) {
            imageView.setImageResource(R.drawable.ic_pic);
            imageView.setVisibility(View.VISIBLE);
        } else {
            try {
                Picasso.get().load(image).placeholder(R.drawable.ic_default).into(imageView);
                imageView.setVisibility(View.VISIBLE);


            } catch (Exception e) {

            }
        }
    }
}