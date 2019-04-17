package com.example.songslist;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    ArrayList<String> displayList;          //List to be displayed (Song name + artist name)
    ArrayList<String> songNamesList;        //Only song names
    ArrayList<String> pathList;
    static MediaPlayer mediaPlayer;
    ListView listView;
    ArrayAdapter<String> adapter;

    int titleIndex, songPathIndex, artistIndex;

    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        Playlist.displayList = new ArrayList<>();
        Playlist.pathList = new ArrayList<>();
        Playlist.songNamesList = new ArrayList<>();

        //When song finishes, play next song in list
        MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                currentIndex = (currentIndex + 1) % songNamesList.size();
                String filePath = pathList.get(currentIndex);
                try {
                    MainActivity.mediaPlayer.reset();
                    MainActivity.mediaPlayer.setDataSource(filePath);
                    MainActivity.mediaPlayer.prepare();
                    MainActivity.mediaPlayer.start();
                }catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        else
            DisplaySongs();
    }



    //Context menu - when he long presses an option in the list of songs -> Play, Add to playlist
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu1, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int list_index = (int)info.id;
        switch(item.getItemId()) {
            case R.id.play:
                String filePath = pathList.get(list_index);
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }catch(Exception e) {
                    e.printStackTrace();
                }
                currentIndex = list_index;
                return true;
            case R.id.add_playlist:
                if (Playlist.displayList.indexOf(displayList.get(list_index)) >= 0) {
                    Toast.makeText(this, "This song is already added to the playlist", Toast.LENGTH_SHORT).show();
                    return true;
                }
                Playlist.displayList.add(displayList.get(list_index));
                Playlist.songNamesList.add(songNamesList.get(list_index));
                Playlist.pathList.add(pathList.get(list_index));

                Toast.makeText(this, songNamesList.get(list_index) + " added to playlist", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }



    public void DisplaySongs () {
        listView = (ListView)findViewById(R.id.lv1);
        displayList = new ArrayList<>();
        songNamesList = new ArrayList<>();
        pathList = new ArrayList<>();
        getMusic();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView)view;
                String s = tv.getText().toString();
                currentIndex = i;
                String filePath = pathList.get(i);

                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                try {
                    Toast.makeText(getApplicationContext(), "Bruh", Toast.LENGTH_SHORT).show();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        registerForContextMenu(listView);
    }

    public void getMusic () {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            titleIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            artistIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            songPathIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                String currentTitle = songCursor.getString(titleIndex);
                String currentArtist = songCursor.getString(artistIndex);
                String currentPath = songCursor.getString(songPathIndex);
                displayList.add(currentTitle + "\n" + currentArtist);
                songNamesList.add(currentTitle);
                pathList.add(currentPath);
            } while (songCursor.moveToNext());

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    DisplaySongs();
                }
                else {
                    Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }



    public void viewPlaylist(View view) {
        if (Playlist.displayList.size() == 0){
            Toast.makeText(this, "No songs in playlist", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, Playlist.class);
        startActivity(i);
    }


}
