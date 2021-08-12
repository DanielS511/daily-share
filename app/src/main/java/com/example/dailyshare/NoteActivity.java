package com.example.dailyshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
    static ArrayList<String> title;
    static ArrayAdapter arrayAdapter;
    static ArrayList<String> note;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.addNote){
            Intent intent = new Intent(getApplicationContext(),NoteEditorActivity.class);
            intent.putExtra("NoteID", title.size());
            startActivity(intent);
            return true;
        }
        return false;
    }

    /*
    Update data for permanent store
     */
    public void updateData(){
        try {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.note", MODE_PRIVATE);
            sharedPreferences.edit().putString("title", ObjectSerializer.serialize(NoteActivity.title)).apply();
            sharedPreferences.edit().putString("note", ObjectSerializer.serialize(NoteActivity.note)).apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    Get former data from the device
     */
    protected void getData(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.note", MODE_PRIVATE);
        try {
            title = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("title", ObjectSerializer.serialize(new ArrayList<String>())));
            note = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("note", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    Initialize two arraylist -- title and note
     */
    protected void setData(){
        getData();

        if (note.size() == 0 || title.size() == 0){
            note.add("This is where you can type your life");
            title.add("note example");
        }
    }
    /*
    Set up the main list view:
        Display titles
        A click listener to let user edit text in NoteEditor activity for specific title
        A long click listener to delete some note
     */
    public void setNoteListView(){
        ListView noteMainListView = findViewById(R.id.noteMainListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, title);// display title
        noteMainListView.setAdapter(arrayAdapter);

        noteMainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // move to NoteEditor activity
                Intent intent = new Intent(getApplicationContext(),NoteEditorActivity.class);
                intent.putExtra("NoteID", position);
                startActivity(intent);
            }
        });

        noteMainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog alertDialog = new AlertDialog.Builder(NoteActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Alert!")
                        .setMessage("Do you want to delete a note")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete the note
                                note.remove(position);
                                title.remove(position);
                                arrayAdapter.notifyDataSetChanged();
                                updateData();
                            }
                        })
                        .setNegativeButton("no", null)
                        .show();
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        title = new ArrayList<>();
        note = new ArrayList<>();
        setData();
        setNoteListView();
    }
}