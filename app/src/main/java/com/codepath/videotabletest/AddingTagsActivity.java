package com.codepath.videotabletest;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by kemleynieva on 7/6/16.
 */

public class AddingTagsActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, com.codepath.videotabletest.VideoControllerView.MediaPlayerControl {

    SurfaceView videoSurface;
    MediaPlayer player;
    com.codepath.videotabletest.VideoControllerView controller;
    //Uri selectedImage;

    Uri uri;
    String uriStr;
    int videoId;
    Video video;

    // Get singleton instance of database
    VidTagsDatabaseHelper databaseHelper = VidTagsDatabaseHelper.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_tags);
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        Log.d("URi",getIntent().getStringExtra("VideoUri") );
        uriStr = getIntent().getStringExtra("VideoUri");
        uri = Uri.parse(uriStr);
        videoId = databaseHelper.getVideoID(uriStr);
        video = databaseHelper.getVideo(videoId);
        RunVideo(uri);
//        selectedImage = Uri.parse(getIntent().getStringExtra("VideoUri"));
//        RunVideo(selectedImage);
    }


    public void RunVideo(Uri uri){
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        player = new MediaPlayer();
        controller = new com.codepath.videotabletest.VideoControllerView(this);

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(String.valueOf(uri)));
            player.setOnPreparedListener(this);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }
    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // End SurfaceHolder.Callback
// Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer((VideoControllerView.MediaPlayerControl) this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        player.start();

        //BE SURE TO CAN THE TIME IF YOU WANT IT TO SHOW THE WHOLE TIME
        controller.show(getDuration());
    }
    // End MediaPlayer.OnPreparedListener
// Implement VideoMediaController.MediaPlayerControl

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    public void addTag(View view) {
        EditText etTagName =(EditText) findViewById(R.id.etTagName);
        String tagName = etTagName.getText().toString();
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(getApplicationContext(), tagName,duration).show();
        double Duration = getDuration();
        double Tagtime = getCurrentPosition();
        double dotValue = (Tagtime/(Duration/10))*100;
        VidTag vidTag = new VidTag();
        vidTag.label = tagName;
        vidTag.time = (int) Tagtime;
        vidTag.video = video;
        Log.d("label", vidTag.label);
        Log.d("time", vidTag.time + "");
        Log.d("video uri", vidTag.video.uri);
        databaseHelper.deleteAllVidTagsAndVideos();
        databaseHelper.addVidTag(vidTag);

        List<VidTag> vidTags = databaseHelper.getAllVidTags();
        for (VidTag vidTag1 : vidTags) {
            Log.d("label", vidTag1.label);
            Log.d("time", vidTag1.time + "");
            Log.d("video uri", vidTag1.video.uri);
        }
        controller.setDots((int) dotValue);
    }

// End VideoMediaController.MediaPlayerControl
}