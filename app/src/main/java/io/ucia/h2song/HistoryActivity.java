package io.ucia.h2song;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends Activity {
    ListView lvHistory;
    List<Map<String,String>> items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Aria.download(this).register();

        lvHistory = findViewById(R.id.lv_history);

        String[] titles = {"茧", "红梅白雪知", "一曲相思"};
        String[] singers = {"祖娅纳惜、夏初临、三个糙汉一个软妹组", "汐音社、云の泣", "半阳"};
        String[] albums = {"三个糙汉一个软妹组·茧", "人间词话古风专辑", "一曲相思"};

        items = new ArrayList<>();
        for (int i =0;i<3;i++){
            Map<String, String> item = new HashMap<>();
            item.put("title", titles[i]);
            item.put("singer", singers[i]);
            item.put("album", albums[i]);
            items.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(HistoryActivity.this,
                items,
                R.layout.history_item,
                new String[]{"title", "singer", "album"},
                new int[] {R.id.tv_htitle, R.id.tv_hsinger, R.id.tv_halbum});
        lvHistory.setAdapter(adapter);
    }
}
