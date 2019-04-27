package io.ucia.h2song;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.ucia.h2song.MainActivity;
import io.ucia.h2song.Song;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkHelper {
    public static final String QQ_SEARCH_API = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?ct=24&qqmusic_ver=1298&new_json=1&remoteplace=txt.yqq.center&t=0&aggr=1&cr=1&catZhida=1&lossless=0&flag_qc=0&p=PAGE&n=20&w=KEYWORD&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
    public static final String QQ_ANALYZE_API = "https://api.mlwei.com/music/api/?key=523077333&cache=1&type=url&id=MID&size=hq";
//    public static final String QQ_ANALYZE_API = "https://api.itooi.cn/music/tencent/url?key=579621905&id=MID&br=128";
    public static final String KG_SEARCH_API = "http://songsearch.kugou.com/song_search_v2?keyword=KEYWORD&page=PAGE&pagesize=20&platform=WebFilter&filter=2&iscorrection=1&privilege_filter=0";
    public static final String KG_ANALYZE_API = "http://www.kugou.com/yy/index.php?r=play/getdata&hash=FILEHASH";
    public static List<io.ucia.h2song.Song> songList = null;

    /**
     * 发送get请求，获得json
     *
     * @param url 请求的网址
     * @return
     */
    public static String doGet(String url) {
        String context = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            context = response.body().string();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context;
    }

    /**
     * 发送post请求，获得json
     * @param url 请求的网址
     * @param formData 表单键值对
     * @return
     */
    public static String doPost(String url, HashMap<String, String> formData) {
        String context = null;
        try {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : formData.keySet()) {
                builder.add(key, formData.get(key));
            }
            OkHttpClient client = new OkHttpClient.Builder().build();
            RequestBody formBody = builder.build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            context = response.body().string();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context;
    }

    /**
     * 获取搜索结果
     * @param keyWord 传过来的搜索关键词
     * @param page 搜索结果分页
     * @return
     */
    public static List<io.ucia.h2song.Song> getSongList(String keyWord, int page) {
        songList = new ArrayList<>();
        String url = "";
        switch (MainActivity.PLATFORM) {
            case "KG":
                url = KG_SEARCH_API.replace("KEYWORD", keyWord).replace("PAGE", "" + page);
                break;
            case "QQ":
                url = QQ_SEARCH_API.replace("KEYWORD", keyWord).replace("PAGE", "" + page);
                break;
        }
        String json = doGet(url);
        Log.d("Helper", "搜索API:" + url);
        try {

            JSONObject jsonData = null;
            JSONArray jsonSongList = null;
            JSONObject jsonSong;
            JSONArray jsonSingerList;
            JSONObject jsonSinger;
            JSONObject jsonFileInfo;

            String singerName;
            String songName;
            String albumName;
            String fileMid;

            Song song;

            switch (MainActivity.PLATFORM) {
                case "KG":
                    jsonData = new JSONObject(json).getJSONObject("data");
                    jsonSongList = jsonData.getJSONArray("lists");
                    break;
                case "QQ":
                    jsonData = new JSONObject(json).getJSONObject("data");
                    jsonSongList = jsonData.getJSONObject("song").getJSONArray("list");
                    break;
            }

            for (int i = 0; i < jsonSongList.length(); i++) {
                song = new Song();
                singerName = "";
                jsonSong = jsonSongList.getJSONObject(i);

                switch (MainActivity.PLATFORM) {
                    case "QQ":
                        jsonSingerList = jsonSong.getJSONArray("singer");
                        for (int j = 0; j < jsonSingerList.length(); j++) {
                            jsonSinger = jsonSingerList.getJSONObject(j);
                            singerName += jsonSinger.getString("title") + "、";
                        }
                        song.singerName = singerName.substring(0, singerName.length() - 1);
                        song.songName = jsonSong.getString("title");
                        song.albumName = jsonSong.getJSONObject("album").getString("title");
                        song.songMid = jsonSong.getString("mid");
                        song.saveName = song.singerName + " - " + song.songName;
                        jsonFileInfo = jsonSong.getJSONObject("file");
                        song.fileSize = String.format("%.2f", jsonFileInfo.getInt("size_128mp3") / 1048576.0) + "M";
                        song.HQFileSize = String.format("%.2f", jsonFileInfo.getInt("size_320mp3") / 1048576.0) + "M";
                        song.SQFileSize = String.format("%.2f", jsonFileInfo.getInt("size_flac") / 1048576.0) + "M";
                        break;
                    case "KG":
                        song.songName = jsonSong.getString("SongName");
                        song.singerName = jsonSong.getString("SingerName");
                        song.albumName = jsonSong.getString("AlbumName");
                        song.saveName = song.singerName + " - " + song.songName;
                        song.fileSize = String.format("%.2f", jsonSong.getInt("FileSize")/1048576.0) +"M";
                        song.fileHash = jsonSong.getString("FileHash");
                        song.HQFileSize = String.format("%.2f", jsonSong.getInt("HQFileSize")/1048576.0) +"M";;
                        song.HQFileHash = jsonSong.getString("HQFileHash");
                        song.SQFileSize = String.format("%.2f", jsonSong.getInt("SQFileSize")/1048576.0) +"M";;
                        song.SQFileHash = jsonSong.getString("SQFileHash");
                        break;
                }
                songList.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songList;
    }

    /**
     * 获取歌曲URL
     * @param str QQ音乐：mid；酷狗音乐：fileHash
     * @return
     */
    public static String getSongUrl(String str) {
        String url = "";
        switch (MainActivity.PLATFORM){
            case "KG":
                String json = doGet(KG_ANALYZE_API.replace("FILEHASH", str));
                try {
                    JSONObject data = new JSONObject(json).getJSONObject("data");
                    url = data.getString("play_url");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "QQ":
                url = QQ_ANALYZE_API.replace("MID", str);
                break;
        }
        Log.d("Helper", "解析API:" + url);
        return url;
    }
}
