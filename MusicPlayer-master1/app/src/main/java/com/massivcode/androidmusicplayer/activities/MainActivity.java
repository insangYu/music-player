
package com.massivcode.androidmusicplayer.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.FinishActivity;
import com.massivcode.androidmusicplayer.events.InitEvent;
import com.massivcode.androidmusicplayer.events.ReloadPlaylist;
import com.massivcode.androidmusicplayer.events.SaveState;
import com.massivcode.androidmusicplayer.managers.Manager;
import com.massivcode.androidmusicplayer.services.MusicService;
import com.massivcode.androidmusicplayer.utils.DataBackupUtil;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener, ExpandableListView.OnChildClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_NAME = "com.massivcode.androidmusicplayer.MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private MyPlaylistFacade mFacade;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private NavigationAdapter mNavigationAdapter;

    private List<String> mMemuTitleList;

    private Intent mServiceIntent;

    private MusicService mMusicService;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "serviceConnected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mMusicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EventBus.getDefault().register(this);

        mFacade = new MyPlaylistFacade(getApplicationContext());


        mServiceIntent = new Intent(MainActivity.this, MusicService.class);
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);


        initViews();

        checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mConnection);


        EventBus.getDefault().unregister(this);
    }

    public void onEvent(Event event) {
        if (event instanceof FinishActivity) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMusicService != null) {
            if (mMusicService.getCurrentInfo() != null && mMusicService.getCurrentPlaylist() != null && mMusicService.getCurrentPosition() != -1) {
                EventBus.getDefault().post(new SaveState());
            }
        }
    }


    private void initViews() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mMemuTitleList = Arrays.asList(getResources().getStringArray(R.array.nav_menu_array));
        mNavigationAdapter = new NavigationAdapter(getSupportFragmentManager());

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_player));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_playlist));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_songs));

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mNavigationAdapter);

        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(mMemuTitleList.get(position));


                mNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


        @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String title = item.getTitle().toString();
        int index = mMemuTitleList.indexOf(title);

        mViewPager.setCurrentItem(index, true);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkPermissions(String permission, int userPermission) {

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                ActivityCompat.requestPermissions(this, new String[]{permission}, userPermission);
            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, userPermission);
            }
        }

        else {
            Log.d(TAG, "사용자가 이전에 승인을 했을 경우");
            switch (permission) {
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    EventBus.getDefault().post(new InitEvent());
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    mFacade.createDb();
                    break;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult @ MainActivity");
        switch (requestCode) {
            case MainActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "권한 승인됨");
                    EventBus.getDefault().post(new InitEvent());
                } else {

                }
                return;
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.player_miniAlbumArt_iv:
            case R.id.player_title_tv:
            case R.id.player_artist_tv:
                mViewPager.setCurrentItem(0);
                mTabLayout.setScrollPosition(0, 0, true);
                break;
            case R.id.player_previous_ib:
                if (mMusicService != null && mMusicService.isReady()) {

                    Intent nextIntent = new Intent(MainActivity.this, MusicService.class);
                    nextIntent.setAction(MusicService.ACTION_PLAY_PREVIOUS);
                    nextIntent.putExtra("position", mMusicService.getPositionAtPreviousOrNext(MusicService.ACTION_PLAY_PREVIOUS));
                    startService(nextIntent);
                }
                break;
            case R.id.player_play_ib:
                if (mMusicService != null && mMusicService.isReady()) {
                    Intent pauseIntent = new Intent(MainActivity.this, MusicService.class);
                    pauseIntent.setAction(MusicService.ACTION_PAUSE);
                    startService(pauseIntent);
                }
                break;
            case R.id.player_next_ib:
                if (mMusicService != null && mMusicService.isReady()) {

                    Intent nextIntent = new Intent(MainActivity.this, MusicService.class);
                    nextIntent.setAction(MusicService.ACTION_PLAY_NEXT);
                    nextIntent.putExtra("position", mMusicService.getPositionAtPreviousOrNext(MusicService.ACTION_PLAY_NEXT));
                    startService(nextIntent);
                }
                break;
            case R.id.player_shuffle_ib:
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                DataBackupUtil.getInstance(getApplicationContext()).saveIsShuffle(v.isSelected());
                break;
            case R.id.player_repeat_ib:
                if (v.isSelected()) {
                    v.setSelected(false);
                } else {
                    v.setSelected(true);
                }
                DataBackupUtil.getInstance(getApplicationContext()).saveIsRepeat(v.isSelected());
                break;
            case R.id.player_favorite_ib:
                if (mMusicService != null && mMusicService.isReady()) {
                    if (v.isSelected()) {
                        v.setSelected(false);
                    } else {
                        v.setSelected(true);
                    }

                    mFacade.toggleFavoriteList(mMusicService.getCurrentPlaylist().get(mMusicService.getCurrentPosition()));
                    EventBus.getDefault().post(new ReloadPlaylist());
                }
                break;
            case R.id.songs_playAll_btn:
                Intent playAllIntent = new Intent(MainActivity.this, MusicService.class);
                playAllIntent.setAction(MusicService.ACTION_PLAY);
                playAllIntent.putExtra("list", MusicInfoLoadUtil.getPlayAllList(MainActivity.this));
                playAllIntent.putExtra("position", 0);
                startService(playAllIntent);
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        switch (parent.getId()) {
            case R.id.songs_listView: {
                ArrayList<Long> list = (ArrayList) MusicInfoLoadUtil.getSelectedSongPlaylist(MainActivity.this, (Cursor) parent.getAdapter().getItem(position));
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY);
                intent.putExtra("list", list);
                intent.putExtra("position", 0);
                startService(intent);
                break;
            }

        }


    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        switch (parent.getId()) {
            case R.id.playlist_listView: {
                Cursor parentData = (Cursor) parent.getExpandableListAdapter().getGroup(groupPosition);
                parentData.moveToPosition(groupPosition);
                String playlistName = parentData.getString(parentData.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST));
                ArrayList<Long> list = MusicInfoLoadUtil.getMusicIdListFromPlaylistName(playlistName, getApplicationContext());
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY);
                intent.putExtra("list", list);
                intent.putExtra("position", childPosition);
                startService(intent);
                break;
            }

        }
        return false;
    }


    private class NavigationAdapter extends FragmentPagerAdapter {

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return Manager.getInstance(position);
        }

        @Override
        public int getCount() {
            return Manager.FRAGMENTS.length;
        }
    }

}
