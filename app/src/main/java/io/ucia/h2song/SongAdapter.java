package io.ucia.h2song;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends BaseAdapter {
    Context context;
    List<Song> items;
    SongInfo songInfo;//StarrySky中用来存储音频信息的实体类
    boolean playing;

    public SongAdapter(Context context, List<Song> items){
        this.context = context;
        this.items = items;
        this.songInfo = new SongInfo();
        songInfo.setSongId("");
        this.playing = false;
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {

            viewHolder = new ViewHolder();

            view = LayoutInflater.from(context).inflate(R.layout.song_list_item, null);
            viewHolder.btnPlay = view.findViewById(R.id.ib_play);
            viewHolder.btnDownload = view.findViewById(R.id.ib_download);
            viewHolder.tvTitle = view.findViewById(R.id.title);
            viewHolder.tvSinger = view.findViewById(R.id.tv_singer);
            viewHolder.tvAlbum = view.findViewById(R.id.tv_album);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvTitle.setText((items.get(i)).songName);
        viewHolder.tvSinger.setText(( items.get(i)).singerName);
        viewHolder.tvAlbum.setText(( items.get(i)).albumName);
        viewHolder.tvTitle.setSelected(true);
        viewHolder.tvSinger.setSelected(true);
        viewHolder.tvAlbum.setSelected(true);

        viewHolder.btnPlay.setOnClickListener(new PlayClickListener(i));
        viewHolder.btnDownload.setOnClickListener(new SongClickListener(i));

        view.setTag(viewHolder);
        return view;
    }

    public class ViewHolder {
        public ImageButton btnPlay;
        public ImageButton btnDownload;
        public TextView tvTitle;
        public TextView tvSinger;
        public TextView tvAlbum;
    }

    //播放按钮点击监听器
    public class PlayClickListener implements View.OnClickListener {
        int i;
        String url;

        public PlayClickListener(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View view) {
            Song song = items.get(i);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    switch (MainActivity.PLATFORM){
                        case "KG":
                            url = NetworkHelper.getSongUrl(song.fileHash);
                            break;
                        case "QQ":
                            url = NetworkHelper.getSongUrl(song.songMid);
                            break;
                    }

                    if (url.length() == 0) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    if (!playing || !(songInfo.getSongId().equals(song.saveName))){//没有在播放，或者在播放之前点击的歌，则开始播放当前点击的歌
                        playing = false;
                        MusicManager.getInstance().pauseMusic();
                        handler.sendEmptyMessage(1);
                        songInfo.setSongId(song.saveName);
                        songInfo.setSongUrl(url);
                        playing = true;
                        MusicManager.getInstance().playMusicByInfo(songInfo);
                        Log.d("SongPlay", "Start");
                    } else {//在播放当前点击的歌，则暂停播放
                        handler.sendEmptyMessage(2);
                        playing = false;
                        MusicManager.getInstance().pauseMusic();
                        Log.d("SongPlay", "Stop");
                    }
                }
            }).start();
        }
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(context, "无法播放，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(context, "开始播放", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "暂停播放", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    //下载按钮点击监听器
    public class SongClickListener implements View.OnClickListener {
        int i;

        public SongClickListener(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View view) {
            BottomSheetDialog qualityDialog = new BottomSheetDialog(context);//底部对话框
            View qualityView = LayoutInflater.from(context).inflate(R.layout.song_quality_dialog, null);//底部对话框的视图
            ListView lvQuality = qualityView.findViewById(R.id.lv_quality);//音质列表视图

            Song song = items.get(i);
            List<ArrayList<String>> qualityList = new ArrayList<>();

            ArrayList<String> qualityItem1 = new ArrayList<>();
            ArrayList<String> qualityItem2 = new ArrayList<>();
            ArrayList<String> qualityItem3 = new ArrayList<>();
            qualityItem1.add("标准");
            qualityItem1.add(song.fileSize);
            qualityItem1.add(song.saveName + ".mp3");
            qualityItem2.add("高品");
            qualityItem2.add(song.HQFileSize);
            qualityItem2.add(song.saveName + ".mp3");
            qualityItem3.add("无损");
            qualityItem3.add(song.SQFileSize);
            qualityItem3.add(song.saveName + ".flac");
            switch (MainActivity.PLATFORM) {
                case "KG":
                    qualityItem1.add(song.fileHash);
                    qualityItem2.add(song.HQFileHash);
                    qualityItem3.add(song.SQFileHash);
                    break;
                case "QQ":
                    qualityItem1.add(song.songMid);
                    qualityItem2.add(song.songMid);
                    qualityItem3.add(song.songMid);
                    break;
            }

            qualityList.add(qualityItem1);
            qualityList.add(qualityItem2);
            qualityList.add(qualityItem3);

            SongQualityAdapter qualityAdapter = new SongQualityAdapter(context, qualityList, qualityDialog);
            lvQuality.setAdapter(qualityAdapter);
            TextView tvTitle = qualityView.findViewById(R.id.title);
            tvTitle.setText(song.songName);

            qualityDialog.setContentView(qualityView);
            qualityDialog.setTitle("下载：" + song.songName);
            qualityDialog.show();
        }
    }
}
