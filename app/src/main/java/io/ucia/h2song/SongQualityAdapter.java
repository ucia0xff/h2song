package io.ucia.h2song;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;

import java.util.ArrayList;
import java.util.List;

import io.ucia.h2song.NetworkHelper;
import io.ucia.h2song.R;

public class SongQualityAdapter extends BaseAdapter {
    Dialog dialog;
    Context context;
    List<ArrayList<String>> items;

    public SongQualityAdapter(Context context, List<ArrayList<String>> items, Dialog dialog) {
        this.context = context;
        this.items = items;
        this.dialog = dialog;
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
        ViewHolder viewHolder = new ViewHolder();
        view = LayoutInflater.from(context).inflate(R.layout.song_quality_list_item, null);

        viewHolder.tvQuality = view.findViewById(R.id.tv_quality);
        viewHolder.tvSize = view.findViewById(R.id.tv_size);
        viewHolder.btnDownload = view.findViewById(R.id.btn_download);

        viewHolder.tvQuality.setText(items.get(i).get(0));//音质
        viewHolder.tvSize.setText(items.get(i).get(1));//大小
        viewHolder.btnDownload.setOnClickListener(new DownloadClickListener(items.get(i).get(2), items.get(i).get(3)));//str,saveName

        view.setTag(viewHolder);
        return view;
    }

    public class DownloadClickListener implements View.OnClickListener {
        String str;
        String saveName;
        String fileUrl;

        public DownloadClickListener(String saveName, String str) {
            this.saveName = saveName;
            this.str = str;
            this.fileUrl = "";
        }

        @Override
        public void onClick(View view) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    fileUrl = NetworkHelper.getSongUrl(str);
                    if (fileUrl.length() > 0) {
                        Aria.download(this)
                                .load(fileUrl)
                                .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/H2Song/" + saveName)
                                .resetState()
                                .start();
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                }
            }).start();
            dialog.dismiss();
        }

    }

    public class ViewHolder {
        public TextView tvQuality;
        public TextView tvSize;
        public Button btnDownload;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(context, "无法获取链接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}