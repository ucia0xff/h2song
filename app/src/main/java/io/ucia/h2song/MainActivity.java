package io.ucia.h2song;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SearchView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.lzx.starrysky.manager.MediaSessionConnection;
import com.lzx.starrysky.manager.MusicManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private static SearchView searchView;
    private static LoadMoreListView lvSong;
    private static List<Song> songList;
    private static SongAdapter songAdapter;
    private String query;
    private int page;

    public static String PLATFORM = "KG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(MainActivity.this);

        //初始化和注册Aria下载库
        Aria.init(this);
        Aria.download(this).register();
        Aria.get(this).getDownloadConfig().setReTryNum(2);//失败重试次数
        Aria.get(this).getDownloadConfig().setReTryInterval(6000);//重试间隔

        //初始化StarrySky音乐播放库
        MusicManager.initMusicManager(this);
        MediaSessionConnection.getInstance().connect();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        songList = new ArrayList<>();//歌曲列表

        songAdapter = new SongAdapter(this, songList);

        lvSong = findViewById(R.id.lv_song);//列表视图
        lvSong.setAdapter(songAdapter);
        lvSong.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });

    }

    //加载更多
    private void loadMore() {
        page++;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Song> list = NetworkHelper.getSongList(query, page);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = list;
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    //处理新的搜索
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchView.clearFocus();
            final String query = intent.getStringExtra(SearchManager.QUERY);
            this.query = query;
            page = 1;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Song> list = NetworkHelper.getSongList(query, page);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = list;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    public static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    songList.clear();
                    lvSong.smoothScrollToPosition(0);
                    break;
                case 0: //新关键词
                    songList.clear();
                    lvSong.smoothScrollToPosition(0);
                    songList.addAll((List<Song>) msg.obj);
                    break;
                case 1: //加载更多
                    lvSong.setLoadCompleted();
                    songList.addAll((List<Song>) msg.obj);
                    break;
            }
            songAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menuItem.getActionView();//搜索框
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);//默认是图标，不展开
        searchView.setSubmitButtonEnabled(true);//在搜索框中显示“开始搜索”的按钮，一个右箭头

        return true;
    }

    //工具栏菜单项被选择
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.platform_kg:
                Toast.makeText(MainActivity.this,"酷狗音乐",Toast.LENGTH_SHORT).show();
                PLATFORM = "KG";
                handler.sendEmptyMessage(-1);
                break;
            case R.id.platform_qq:
                Toast.makeText(MainActivity.this,"QQ音乐",Toast.LENGTH_SHORT).show();
                PLATFORM = "QQ";
                handler.sendEmptyMessage(-1);
                break;
            case R.id.platform_wy:
                Toast.makeText(MainActivity.this,"网易云音乐",Toast.LENGTH_SHORT).show();
                PLATFORM = "KG";
                handler.sendEmptyMessage(-1);
                break;
            case R.id.platform_kw:
                Toast.makeText(MainActivity.this,"酷我音乐",Toast.LENGTH_SHORT).show();
                PLATFORM = "KG";
                handler.sendEmptyMessage(-1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_history:
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.item_setting:
            case R.id.item_about:
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Download.onTaskStart void downloadStart(DownloadTask task) {
        Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
    }
    @Download.onTaskComplete void downloadComplete(DownloadTask task) {
        Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
    }
    @Download.onTaskFail void downloadFail(DownloadTask task) {
        Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
    }

    //检查读写权限
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Aria.download(this).unRegister();
        MediaSessionConnection.getInstance().disconnect();
    }
}
