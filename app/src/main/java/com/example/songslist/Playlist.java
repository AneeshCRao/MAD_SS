package com.example.songslist;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Playlist extends AppCompatActivity {

    ListView playlist_list;

    static ArrayList<String> displayList;          //List to be displayed (Song name + artist name)
    static ArrayList<String> songNamesList;        //Only song names
    static ArrayList<String> pathList;

    ArrayAdapter<String> adapter;


    int currentIndex = 0;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        MainActivity.mediaPlayer.reset();
        MainActivity.mediaPlayer = new MediaPlayer();
        playlist_list = (ListView)findViewById(R.id.playlist_list);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayList);
        playlist_list.setAdapter(adapter);

        registerForContextMenu(playlist_list);


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

        //On clicking, play from that song
        playlist_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentIndex = i;
                if (MainActivity.mediaPlayer.isPlaying())
                    MainActivity.mediaPlayer.stop();
                String filePath = pathList.get(currentIndex);
                try {
                    MainActivity.mediaPlayer.reset();
                    MainActivity.mediaPlayer.setDataSource(filePath);
                    MainActivity.mediaPlayer.prepare();
                    MainActivity.mediaPlayer.start();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    //Context menu - when he long presses an option in the list of songs -> Play, Add to playlist
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.playlist_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int list_index = (int)info.id;

        switch(item.getItemId()) {
            case R.id.play_playlist:
                String filePath = pathList.get(list_index);
                if (MainActivity.mediaPlayer.isPlaying())
                    MainActivity.mediaPlayer.stop();
                try {
                    MainActivity.mediaPlayer.reset();
                    MainActivity.mediaPlayer.setDataSource(filePath);
                    MainActivity.mediaPlayer.prepare();
                    MainActivity.mediaPlayer.start();
                }catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_SHORT).show();
                }
                currentIndex = list_index;
                return true;
            case R.id.remove_playlist:

                String removedSongName = songNamesList.get(list_index);
                String deleteName = displayList.get(list_index);
                pathList.remove(list_index);
                songNamesList.remove(list_index);
                displayList.remove(list_index);


                adapter.remove(deleteName);
                adapter.notifyDataSetChanged();

                if (displayList.size() == 0) return true;

                if (currentIndex == list_index) {       //Current playing song is removed
                    currentIndex = (currentIndex + 1) % (displayList.size());
                    filePath = pathList.get(currentIndex);
                    try {
                        MainActivity.mediaPlayer.reset();
                        MainActivity.mediaPlayer.setDataSource(filePath);
                        MainActivity.mediaPlayer.prepare();
                        MainActivity.mediaPlayer.start();
                    }catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_SHORT).show();
                    }
                }

                Toast.makeText(this, removedSongName + " removed from playlist", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }







}
