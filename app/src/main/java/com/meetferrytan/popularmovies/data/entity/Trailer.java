package com.meetferrytan.popularmovies.data.entity;

/**
 * Created by ferrytan on 8/2/17.
 */

import com.google.gson.annotations.SerializedName;

/**
    {
        "id": "5818e6c8c3a368421a000fb7",
        "iso_639_1": "en",
        "iso_3166_1": "US",
        "key": "x7CAjpdRaXU",
        "name": "Official Trailer",
        "site": "YouTube",
        "size": 1080,
        "type": "Trailer"
    }
 **/
public class Trailer {
    public static final String KEY = "{key}";
    public static final String YOUTUBE_THUMBNAIL_URL = "https://i1.ytimg.com/vi/{key}/mqdefault.jpg";
    public static final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v={key}";
    @SerializedName("id")
    private String mId;
    @SerializedName("key")
    private String mKey;
    @SerializedName("name")
    private String mName;
    @SerializedName("site")
    private String mSite;
    @SerializedName("type")
    private String mType;

    public Trailer() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getSite() {
        return mSite;
    }

    public void setSite(String site) {
        mSite = site;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getYoutubeThumbnailUrl(){
        return YOUTUBE_THUMBNAIL_URL.replace(KEY, mKey);
    }

    public String getYoutubeVideoUrl(){
        return YOUTUBE_VIDEO_URL.replace(KEY, mKey);
    }
}
