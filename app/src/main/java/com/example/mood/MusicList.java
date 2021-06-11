package com.example.mood;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;


//activity to display the list of songs
public class MusicList extends AppCompatActivity {

    private Context mContext;
    TextView textv;
    ListView lv;
    String mood;
    String songNameval, songUrlval;
    JcPlayerView jcplayer;

    FirebaseDatabase database;
    DatabaseReference myref;

    ArrayList<String> arrsongName = new ArrayList<>();
    ArrayList<String> arrsongUrl = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    ArrayList<JcAudio> jcAudios = new ArrayList<>(); //array list for jcplayer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        Intent intent = getIntent();
        mood = intent.getStringExtra("mood");
        mContext = this;
        initviews();
    }

    private void initviews() {
        textv = (TextView) findViewById(R.id.textv);
        jcplayer = (JcPlayerView) findViewById(R.id.jcplayer);
        lv = (ListView) findViewById((R.id.myListView));

        textv.setText(mood + " Mood Playlist");
        if(mood.equals("Fear") || mood.equals("Disgust") || mood.equals("Surprise"))
        {
            mood = "Angry";
        }
        //Firebase connection
        database = FirebaseDatabase.getInstance();
        myref = database.getReference(mood); //string value of path
        DatabaseReference lang = myref.child("Hindi");

        lang.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    songNameval = ds.child("songName").getValue().toString();
                    System.out.println("######################################" + songNameval);
                    songUrlval = ds.child("songUrl").getValue().toString();
                    arrsongName.add(songNameval);
                    arrsongUrl.add(songUrlval);
                    jcAudios.add(JcAudio.createFromURL(songNameval,songUrlval));

                }
                arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.list_item_layout, arrsongName);
                jcplayer.initPlaylist(jcAudios, null);
                lv.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
            }
        });


        //list view onclick listener for each song
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                jcplayer.playAudio(jcAudios.get(position));
                jcplayer.setVisibility(View.VISIBLE);
                jcplayer.createNotification();
            }
        });
    }

    //menu functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.item1:
                //All
                Toast.makeText(this, "Fetching All songs", Toast.LENGTH_SHORT).show();
                setfilter(item.getTitle());
                return true;
            case R.id.item2:
                //English
                Toast.makeText(this,"Fetching English songs", Toast.LENGTH_SHORT).show();
                setfilter(item.getTitle());
                return true;
            case R.id.item3:
                //Hindi
                Toast.makeText(this,"Fetching Hindi songs", Toast.LENGTH_SHORT).show();
                setfilter(item.getTitle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setfilter(CharSequence title)
    {
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&7"+title.toString());
        arrayAdapter.clear();
        jcAudios.clear();
        if(title.toString().equals("All"))
        {
            String[] arr_lang = {"Hindi","English"};
            for(String lang:arr_lang) {
                //System.out.println("########$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4"+lang);
                setfilterall(lang);
            }
        }
        else
        {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference myref = db.getReference(mood);
            DatabaseReference filter = myref.child(title.toString());

            filter.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        songNameval = ds.child("songName").getValue().toString();
                        //System.out.println("######################################" + songNameval);
                        songUrlval = ds.child("songUrl").getValue().toString();
                        System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU\t"+songUrlval);
                        arrsongName.add(songNameval);
                        arrsongUrl.add(songUrlval);
                        jcAudios.add(JcAudio.createFromURL(songNameval, songUrlval));

                    }
                    arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.list_item_layout, arrsongName);
                    jcplayer.initPlaylist(jcAudios, null);
                    lv.setAdapter(arrayAdapter);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    }//eof

    private void setfilterall(String title) {
        DatabaseReference filter = myref.child(title);

        filter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    songNameval = ds.child("songName").getValue().toString();
                    //System.out.println("######################################" + songNameval);
                    songUrlval = ds.child("songUrl").getValue().toString();
                    arrsongName.add(songNameval);
                    arrsongUrl.add(songUrlval);
                    jcAudios.add(JcAudio.createFromURL(songNameval, songUrlval));

                }
                arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.list_item_layout, arrsongName);
                jcplayer.initPlaylist(jcAudios, null);
                lv.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}