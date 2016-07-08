package com.codepath.videotabletest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoView vidView;

        // Get singleton instance of database
        VidTagsDatabaseHelper databaseHelper = VidTagsDatabaseHelper.getInstance(this);
    }

    public void startAct(View view) {
        startActivity(new Intent(this, com.codepath.videotabletest.VideoPlayerActivity.class));
    }


}
