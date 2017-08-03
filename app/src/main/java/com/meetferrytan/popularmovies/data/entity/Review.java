package com.meetferrytan.popularmovies.data.entity;

/**
 * Created by ferrytan on 8/2/17.
 */

import com.google.gson.annotations.SerializedName;

/**
 {
     "id": "58d04679c3a3682dcd0002c6",
     "author": "Salt-and-Limes",
     "content": "**Spoilers**\r\n\r\nThe live action remake of \"Beauty and the Beast\" was good, but it failed to capture the magic of the cartoon version. There were somethings that they got right, and others that dragged on.\r\n\r\nI thought \"Be Our Guest\" was done beautifully. The 3d made it even more enchanting. The main characters' backstories also added some depth to them. However, there were some scenes that I felt added nothing to the story. Such as the search for Belle by Gaston and her father. The \"No one is like Gaston\" scene didn't have the bravado or arrogance of the original.\r\n\r\nI also felt that Luke Evans was miscast. He wasn't the handsomest guy in town, nor was he the strongest. Which is why it was hard for me to accept him as the character. Emma Watson was serviceable. Her voice was fine, but it wasn't strong enough to carry Belle's songs. Dan Stevens was the best part of the film. I felt that he should have had more songs, because he has a beautiful baritone. Although his beast costume should have been more frightening. \r\n\r\nOverall, it's a fun film to watch. Though, I wouldn't call it a classic.",
     "url": "https://www.themoviedb.org/review/58d04679c3a3682dcd0002c6"
 }
 */
public class Review {
    @SerializedName("id")
    private String mId;
    @SerializedName("author")
    private String mAuthor;
    @SerializedName("content")
    private String mContent;
    @SerializedName("url")
    private String mUrl;

    public Review() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
