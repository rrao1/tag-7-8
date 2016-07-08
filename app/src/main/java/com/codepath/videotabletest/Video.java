package com.codepath.videotabletest;

/**
 * Created by ramyarao on 7/6/16.
 */
public class Video implements Comparable<Video>{
    public String uri;


    @Override
    public int compareTo(Video v) {
        return this.uri.compareTo(v.uri);
    }
}

