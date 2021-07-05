package com.example.mplayer.Model;

public class UploadSource {
    public  String songscategory,songfile,artish,album,songduration,songlink,skey;

    public UploadSource(String songscategory, String songfile, String artish, String album, String songduration, String songlink)
    {
        if(songfile.trim().equals(""))
        {
            songfile = "No Title";
        }
        this.songscategory = songscategory;
        this.songfile = songfile;
        this.artish = artish;
        this.album = album;
        this.songduration = songduration;
        this.songlink = songlink;
    }

    public UploadSource()
    {
    }

    public String getSongscategory() {
        return songscategory;
    }

    public void setSongscategory(String songscategory) {
        this.songscategory = songscategory;
    }

    public String getSongfile() {
        return songfile;
    }

    public void setSongfile(String songfile) {
        this.songfile = songfile;
    }

    public String getArtish() {
        return artish;
    }

    public void setArtish(String artish) {
        this.artish = artish;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSongduration() {
        return songduration;
    }

    public void setSongduration(String songduration) {
        this.songduration = songduration;
    }

    public String getSonglink() {
        return songlink;
    }

    public void setSonglink(String songlink) {
        this.songlink = songlink;
    }

    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }
}
