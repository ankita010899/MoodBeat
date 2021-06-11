package com.example.mood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class EmojiPage extends AppCompatActivity {

    ImageButton angrybtn, happybtn, neutralbtn, sadbtn, surprisebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_page);

        //initializing the views
        initViews();
    }

    private void initViews() {
        happybtn = (ImageButton)findViewById(R.id.imgbtn1);
        surprisebtn = (ImageButton)findViewById(R.id.imgbtn2);
        angrybtn = (ImageButton)findViewById(R.id.imgbtn3);
        sadbtn = (ImageButton)findViewById(R.id.imgbtn4);
        neutralbtn = (ImageButton)findViewById(R.id.imgbtn5);

        //setting onclick listeners for each button
        //happy button
        happybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmojiPage.this, MusicList.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("mood","Happy");
                intent.setType("text/plain");
                EmojiPage.this.startActivity(intent);
            }
        });

        //surprise button
        surprisebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmojiPage.this, MusicList.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("mood","Surprise");
                intent.setType("text/plain");
                EmojiPage.this.startActivity(intent);
            }
        });

        //angry button
        angrybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmojiPage.this, MusicList.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("mood","Angry");
                intent.setType("text/plain");
                EmojiPage.this.startActivity(intent);
            }
        });

        //sad button
        sadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmojiPage.this, MusicList.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("mood","Sad");
                intent.setType("text/plain");
                EmojiPage.this.startActivity(intent);
            }
        });

        //neutral button
        neutralbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmojiPage.this, MusicList.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("mood","Neutral");
                intent.setType("text/plain");
                EmojiPage.this.startActivity(intent);
            }
        });

    }


}