package com.example.mplayer.Model;


public class Upload {
    public String name;
    public String url;
    public String SongsCategory;

    public Upload(String name, String url, String songsCategory) {
        this.name = name;
        this.url = url;
        SongsCategory = songsCategory;
    }

    public Upload() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSongsCategory() {
        return SongsCategory;
    }

    public void setSongsCategory(String songsCategory) {
        SongsCategory = songsCategory;
    }
}
