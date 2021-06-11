package com.example.mood;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

//activity for displaying the music player and its controls
public class PlayerActivity extends AppCompatActivity {

    TextView song_name, artist_name, duration_played,duration_total;
    ImageView cover_art, nextBtn , prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPausedBtn;
    SeekBar seekBar;
    int position = -1;
    public static ArrayList<Song> listSongs= new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
    }

    private void initView() {
        song_name=findViewById(R.id.song_name);
        artist_name=findViewById(R.id.song_artist);
        duration_played=findViewById(R.id.durationPlayed);
        duration_total=findViewById(R.id.durationTotal);
        cover_art=findViewById(R.id.cover_art);
        nextBtn=findViewById(R.id.id_next);
        prevBtn=findViewById(R.id.id_prev);
        backBtn=findViewById(R.id.back_btn);
        shuffleBtn=findViewById(R.id.id_shuffle);
        repeatBtn=findViewById(R.id.id_repeat);
        playPausedBtn=findViewById(R.id.play_pause);
        seekBar=findViewById(R.id.seekBar);

    }
}