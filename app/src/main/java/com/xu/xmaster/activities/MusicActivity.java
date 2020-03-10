package com.xu.xmaster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.beans.MusicModel;
import com.xu.xmaster.beans.MusicListBean;
import com.xu.xmaster.fragments.MusicFragment;
import com.xu.xmaster.fragments.MusicSearchFragment;
import com.xu.xmaster.fragments.MusicListFragment;
import com.xu.xmaster.views.MusicView;
import com.xu.xmaster.views.MyMusicPlayerView;
import com.xu.xmaster.views.SimpleToolbar;
import com.xu.xxplayer.players.BasePlayerView;
import com.xu.xxplayer.utils.XXPlayerManager;

public class MusicActivity extends BaseActivity {

    private static final String TAG = "MusicActivity";

    private SimpleToolbar toolbar;
    private SlidingTabLayout mTabLayout;
    private ViewPager mViewPager;
    private MusicView mMusicView;
    private MyMusicPlayerView mPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_music);

        initView();
        initEvent();
        initPlayer();

        if (getIntent().getBooleanExtra("startFromNotification", false)) {
            mMusicView.enterFullScreen();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("startFromNotification", false)) {
            mMusicView.enterFullScreen();
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);
        mMusicView = findViewById(R.id.mMusicView);

        initToolbar();
        initViewPager();
    }

    private void initToolbar() {
        toolbar.setPaddingTop();

        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.setRightBtn1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicSearchFragment musicSearchFragment = MusicSearchFragment.newInstance();
                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                        .replace(R.id.container, musicSearchFragment)
                        .addToBackStack(null)
                        .commit();

                musicSearchFragment.setOnMusicSearchFragmentListener(new MusicSearchFragment.OnMusicSearchFragmentListener() {
                    @Override
                    public void setMusicModel(MusicModel musicModel) {
                        mMusicView.setMusicModel(musicModel);
                        mMusicView.playMusic();
                    }
                });
            }
        });
    }

    private void initViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            String[] listName = {"推荐榜", "巅峰榜", "地区榜", "其他榜"};
            int[][] topList = {Constant.recommendTopid, Constant.topTopid, Constant.regionTopid, Constant.otherTopid};

            @Override
            public Fragment getItem(int i) {
                MusicListFragment songListFragment = MusicListFragment.newInstance(topList[i], i == 0);

                songListFragment.setOnSongListItemClickListener(new MusicListFragment.OnSongListItemClickListener() {
                    @Override
                    public void setSongList(MusicListBean songList) {
                        MusicFragment musicFragment = MusicFragment.newInstance(songList);
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                                .replace(R.id.container, musicFragment)
                                .addToBackStack(null)
                                .commit();

                        musicFragment.setOnMusicFragmentListener(new MusicFragment.OnMusicFragmentListener() {
                            @Override
                            public void setMusicModel(MusicModel musicModel) {
                                mMusicView.setMusicModel(musicModel);
                                mMusicView.playMusic();
                            }
                        });
                    }
                });
                return songListFragment;
            }

            @Override
            public int getCount() {
                return listName.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return listName[position];
            }
        });

        //在viewpager setAdapter之后
        mTabLayout.setViewPager(mViewPager);
        mTabLayout.setSnapOnTabClick(true);//viewpager切换不滑动
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void initEvent() {
        mMusicView.setOnMusicListener(new MusicView.OnMusicListener() {
            @Override
            public void searchMusic(String name) {
                MusicSearchFragment musicSearchFragment = MusicSearchFragment.newInstance(name);
                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                        .replace(R.id.container, musicSearchFragment)
                        .addToBackStack(null)
                        .commit();

                musicSearchFragment.setOnMusicSearchFragmentListener(new MusicSearchFragment.OnMusicSearchFragmentListener() {
                    @Override
                    public void setMusicModel(MusicModel musicModel) {
                        mMusicView.setMusicModel(musicModel);
                        mMusicView.playMusic();
                    }
                });
            }
        });
    }

    private void initPlayer() {
        if (XXPlayerManager.instance().getCurrentPlayer() != null &&
                XXPlayerManager.instance().getCurrentPlayer() instanceof MyMusicPlayerView) {
            mPlayerView = (MyMusicPlayerView) XXPlayerManager.instance().getCurrentPlayer();
            mMusicView.setPlayerView(mPlayerView);
            mMusicView.setMusicModel(mPlayerView.getMusicModel());

            int state = mPlayerView.getCurrentState();
            mPlayerView.setOnPlayStateChanged(BasePlayerView.STATE_PLAYING);
            mPlayerView.setOnPlayStateChanged(state);
        } else {
            mPlayerView = new MyMusicPlayerView(getContext());
            mMusicView.setPlayerView(mPlayerView);
        }

        mPlayerView.setOnMusicPlayerListener(new MyMusicPlayerView.OnMusicPlayerListener() {
            @Override
            public void setMusicModel(MusicModel musicModel) {
                mMusicView.setMusicModel(musicModel);
                mMusicView.playMusic();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mMusicView.isFullScreen()) {
            mMusicView.exitFullScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.unbindService();
    }
}
