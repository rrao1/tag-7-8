package com.codepath.videotabletest;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class VidTagsDatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "videosDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "DEBUG";

    // Table Names
    private static final String TABLE_VIDEOS = "videos";
    private static final String TABLE_VIDTAGS = "vidtags";

    // Video Table Columns
    private static final String KEY_VIDEO_ID = "id";
    //private static final String KEY_POST_USER_ID_FK = "userId";
    private static final String KEY_VIDEO_URI = "uri";

    // VidTag Table Columns
    private static final String KEY_VIDTAG_ID = "id";
    private static final String KEY_VIDTAG_VIDEO_ID_FK = "videoId";
    private static final String KEY_VIDTAG_LABEL = "label";
    private static final String KEY_VIDTAG_TIME = "time";

    private static VidTagsDatabaseHelper sInstance;

    public static synchronized VidTagsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new VidTagsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private VidTagsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_VIDTAGS_TABLE = "CREATE TABLE " + TABLE_VIDTAGS +
                "(" +
                KEY_VIDTAG_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_VIDTAG_VIDEO_ID_FK + " INTEGER REFERENCES " + TABLE_VIDEOS + "," + // Define a foreign key
                KEY_VIDTAG_LABEL + " TEXT," +
                //TODO CHECK IF IT'S AN INTENGER
                KEY_VIDTAG_TIME + " INTEGER" +
                ")";

        String CREATE_VIDEOS_TABLE = "CREATE TABLE " + TABLE_VIDEOS +
                "(" +
                KEY_VIDEO_ID + " INTEGER PRIMARY KEY," +
                KEY_VIDEO_URI + " TEXT" +
                ")";

        db.execSQL(CREATE_VIDTAGS_TABLE);
        db.execSQL(CREATE_VIDEOS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDTAGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);
            onCreate(db);
        }
    }

    // Insert a vidtag into the database
    public void addVidTag(VidTag vidTag) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).

            long videoId = addOrUpdateVideo(vidTag.video);

            ContentValues values = new ContentValues();
            values.put(KEY_VIDTAG_VIDEO_ID_FK, videoId);
            values.put(KEY_VIDTAG_LABEL, (vidTag.label).toLowerCase());
            values.put(KEY_VIDTAG_TIME, vidTag.time);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_VIDTAGS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add video tag to database");
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long addOrUpdateVideo(Video video) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VIDEO_URI, video.uri);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_VIDEOS, values, KEY_VIDEO_URI + "= ?", new String[]{video.uri});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_VIDEO_ID, TABLE_VIDEOS, KEY_VIDEO_URI);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(video.uri)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_VIDEOS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    //Takes in the uri of a video, returns the video id
    public int getVideoID(String uri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int id = 0;
        try {

            cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID, KEY_VIDEO_URI}, KEY_VIDEO_URI + "=?", new String[]{uri}, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getInt(cursor.getColumnIndex(KEY_VIDEO_ID));
            }
            return id;
        } finally {
            cursor.close();
        }
    }

    //Takes in video id, returns full video object
    public Video getVideo(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String uriStr;
        Video video = new Video();
        try {

            cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID, KEY_VIDEO_URI}, KEY_VIDEO_ID + "=?", new String[]{Integer.toString(id)}, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                uriStr = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
                video.uri = uriStr;
            }
            return video;
        } finally {
            cursor.close();
        }
    }

    //Takes in video, returns a list of all vidtags associated with that video
    public List<VidTag> getAssociatedVidTags(Video video) {
        List<VidTag> vidTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int id = getVideoID(video.uri);
        if (id == 0) {
            return vidTags;
        }
        try {

            cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_VIDEO_ID_FK + "=?", new String[]{Integer.toString(id)}, null, null, null);

            if (cursor.moveToFirst()) {

                do {
                    VidTag vidTag = new VidTag();
                    vidTag.video = video;
                    vidTag.label = cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL));
                    vidTag.time = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_TIME));
                    vidTags.add(vidTag);
                } while (cursor.moveToNext());
            }
            return vidTags;
        } finally {
            cursor.close();
        }

    }


    //Takes in video, deletes all tags associated with that video
    public boolean deleteAssociatedVidTags(Video video) {
        int id = getVideoID(video.uri);
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_VIDTAGS + " WHERE " + KEY_VIDTAG_VIDEO_ID_FK + "= '" + id + "'");
            //Close the database
            database.close();

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    //Takes in string query, returns a set of videos which contain tags whose labels match the query
    public Set<Video> getSearchResults(String query) {
        Set<Video> videoResults = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_LABEL + "=?", new String[]{query.toLowerCase()}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    int videoId = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_VIDEO_ID_FK));
                    Video video = getVideo(videoId);
                    videoResults.add(video);
                } while (cursor.moveToNext());
            }
            return videoResults;
        } finally {
            cursor.close();
        }


    }

    // Gets all vidtags in the database
    public List<VidTag> getAllVidTags() {
        List<VidTag> vidTags = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                        TABLE_VIDTAGS,
                        TABLE_VIDEOS,
                        TABLE_VIDTAGS, KEY_VIDTAG_VIDEO_ID_FK,
                        TABLE_VIDEOS, KEY_VIDEO_ID);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Video newVideo = new Video();
                    newVideo.uri = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
                    VidTag newVidTag = new VidTag();
                    newVidTag.label = cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL));
                    newVidTag.time = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_TIME));
                    newVidTag.video = newVideo;
                    vidTags.add(newVidTag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get video tags from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return vidTags;
    }

    //Deletes video from the database, and deletes all tags associated with the video
    public boolean deleteVideo(Video video) {
        String uri = video.uri;
        deleteAssociatedVidTags(video);
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_VIDEOS + " WHERE " + KEY_VIDEO_URI + "= '" + uri + "'");
            //Close the database
            database.close();

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    //Takes in vidtag, returns id for the tag
    public int getVidTagId(VidTag vidTag) {
        List<VidTag> vidTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int videoId = getVideoID((vidTag.video).uri);
        int id = 0;
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_VIDEO_ID_FK + "=?", new String[]{Integer.toString(videoId)}, null, null, null);

            if (cursor.moveToFirst()) {

                do {
                    if (vidTag.label.equals(cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL)))
                            && vidTag.time == cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_TIME))) {
                        id = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_ID));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return id;
    }

    //Deletes a vidtag
    public boolean deleteVidTag(VidTag vidTag) {
        int id = getVidTagId(vidTag);
        if (id == 0) { return false; }
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_VIDTAGS + " WHERE " + KEY_VIDTAG_ID + "= '" + id + "'");
            //Close the database
            database.close();

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    // Gets all videos in the database
    public List<Video> getAllVideos() {
        List<Video> videos = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VIDEOS, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Video newVideo = new Video();
                    newVideo.uri = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
                    videos.add(newVideo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get videos from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return videos;
    }

    //TODO: add update methods so users can edit vidtags
//    // Update the video's uri
//    public int updateUserProfilePicture(User user) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);
//
//        // Updating profile picture url for user with that userName
//        return db.update(TABLE_USERS, values, KEY_USER_NAME + " = ?",
//                new String[] { String.valueOf(user.userName) });
//    }

    // Delete all videos and vidtags in the database
    public void deleteAllVidTagsAndVideos() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_VIDTAGS, null, null);
            db.delete(TABLE_VIDEOS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all vidtags and videos!");
        } finally {
            db.endTransaction();
        }
    }
}