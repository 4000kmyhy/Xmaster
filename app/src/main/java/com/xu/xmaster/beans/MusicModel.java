package com.xu.xmaster.beans;

import java.util.List;

public class MusicModel {

    private String url;
    private String songname;
    private String songmid;//音乐mid
    private String singer;
    private String albumname;
    private String imgUrl;
    private String lyric;

    private int position;
    private List<MusicBean> musicList;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getSongmid() {
        return songmid;
    }

    public void setSongmid(String songmid) {
        this.songmid = songmid;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<MusicBean> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<MusicBean> musicList) {
        this.musicList = musicList;
    }

    public String getAlbumname() {
        return albumname;
    }

    public void setAlbumname(String albumname) {
        this.albumname = albumname;
    }

    @Override
    public String toString() {
        return "MusicModel{" +
                "url='" + url + '\'' +
                ", songname='" + songname + '\'' +
                ", songmid='" + songmid + '\'' +
                ", singer='" + singer + '\'' +
                ", albumname='" + albumname + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", lyric='" + lyric + '\'' +
                ", position=" + position +
                ", musicList=" + musicList +
                '}';
    }
}
