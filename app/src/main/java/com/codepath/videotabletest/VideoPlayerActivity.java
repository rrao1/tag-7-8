
package com.codepath.videotabletest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by kemleynieva on 7/6/16.
 */

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, com.codepath.videotabletest.VideoControllerView.MediaPlayerControl {

    SurfaceView videoSurface;
    MediaPlayer player;
    com.codepath.videotabletest.VideoControllerView controller;
    private static int RESULT_LOAD_VIDEO = 1;
    String imgDecodableString;
    Uri uri;


    // Get singleton instance of database
    VidTagsDatabaseHelper databaseHelper = VidTagsDatabaseHelper.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        getVideo();

    }
    public void getVideo(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        //android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_VIDEO);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_VIDEO && resultCode == RESULT_OK
                    && null != data) {
                //TODO delete this line after testing
                databaseHelper.deleteAllVidTagsAndVideos();

                uri = data.getData();
                newVideo(uri);
                String[] filePathColumn = { MediaStore.Video.Media.DATA };

                Cursor cursor = getContentResolver().query(uri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                RunVideo(uri);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void newVideo(Uri uri) {

        String uriStr = uri.toString();
        Video video = new Video();
        video.uri = uriStr;
        databaseHelper.addOrUpdateVideo(video);
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

    public void startTag(View view) {
        Intent addTags = new Intent(this, com.codepath.videotabletest.AddingTagsActivity.class);
        addTags.putExtra("VideoUri",uri.toString());
        startActivity(addTags);
    }
// End VideoMediaController.MediaPlayerControl
}