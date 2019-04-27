package io.ucia.h2song;

public class Song {
    public String songName;
    public String singerName;
    public String albumName;
    public String saveName;

    public String songMid;//QQ音乐

    public String fileSize;//mp3_128
    public String HQFileSize;//mp3_320
    public String SQFileSize;//flac

    public String fileName;
    public String HQFileName;
    public String SQFileName;

    public String fileHash;//酷狗音乐
    public String HQFileHash;//酷狗音乐
    public String SQFileHash;//酷狗音乐

    public String url;
    public String HQUrl;
    public String SQUrl;

    public Song() {

    }

    public Song(String songName, String singerName, String albumName){
        this.songName = songName;
        this.singerName = singerName;
        this.albumName = albumName;
    }
}
