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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kemleynieva on 7/6/16.
 */

public class AddingTagsActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, com.codepath.videotabletest.VideoControllerView.MediaPlayerControl {

    SurfaceView videoSurface;
    MediaPlayer player;
    List<Integer> tagTimes = new ArrayList<>();
    int[] positions;
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
        //databaseHelper.deleteAllVidTagsAndVideos();
        displayTags();
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

    public void displayTags() {
        List<VidTag> associatedTags = databaseHelper.getAssociatedVidTags(video);
        for(int i = 0; i < associatedTags.size(); i ++) {
            tagTimes.add(associatedTags.get(i).time);
        }
        if (tagTimes.size() > 0) {
            controller.setDots(tagTimes);
        }

//        int numberOfTags = associatedTags.size();
//        positions = new int[numberOfTags];
//        for (int i = 0; i < numberOfTags; i++) {
//            positions[i] = (associatedTags.get(i)).time;
//        }
//        if (numberOfTags > 0) {
//            controller.setDots(positions);
//        }

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
        //TODO DOTVALUE TIME
        vidTag.time = (int) dotValue;
        vidTag.video = video;
        databaseHelper.deleteAllVidTagsAndVideos();
        databaseHelper.addVidTag(vidTag);
        tagTimes.add((int) dotValue);
        //int sizeOfNewArray = positions.length + 1;
//        int[] addedTagArray = new int[sizeOfNewArray];
//        for (int i = 0; i < sizeOfNewArray - 1; i++) {
//            addedTagArray[i] = positions[i];
//        }
//        addedTagArray[sizeOfNewArray - 1] = (int) dotValue;
        //int[] newArray = new int[1];
        //newArray[0] = (int) dotValue;

        controller.setDots(tagTimes);
    }

// End VideoMediaController.MediaPlayerControl
}