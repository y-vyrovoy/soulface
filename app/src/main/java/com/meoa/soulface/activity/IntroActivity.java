package com.meoa.soulface.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.meoa.soulface.DebugLogger;
import com.meoa.soulface.R;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends FragmentActivity {

    private int mCurrentPage;
    private int mPages[] = {R.layout.intro_page_00, R.layout.intro_page_01, R.layout.intro_page_02, R.layout.intro_page_03};
    private int mPageIndexImages[] = {R.drawable.page_index_00, R.drawable.page_index_01, R.drawable.page_index_02, R.drawable.page_index_03};
    private ViewPager mViewPager;
    private ImageButton mBtnBack;
    private ImageButton mBtnForward;
    private ImageButton mBtnSkip;
    private ImageView mImagePageIndex;

    private List<IntroFragment> mFragmentsList;
    private ScreenSlidePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLogger.d(null);

        setContentView(R.layout.activity_intro);

        mViewPager = findViewById(R.id.page_viewer);
        mBtnBack = findViewById(R.id.btn_back);
        mBtnForward = findViewById(R.id.btn_forward);
        mBtnSkip = findViewById(R.id.btn_skip);
        mImagePageIndex = findViewById(R.id.image_page_index);

        mFragmentsList = new ArrayList<>();

        for (int id : mPages) {
            IntroFragment frag = new IntroFragment();
            Bundle param = new Bundle();
            param.putInt(IntroFragment.PARAM_FRAGMENT_LAYOUT, id);
            frag.setArguments(param);

            mFragmentsList.add(frag);
        }

        mAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                DebugLogger.d("position: " + position);

                mCurrentPage = position;
                updateButtons();
                setPage(mCurrentPage);
            }
        });

        mCurrentPage = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        DebugLogger.d(null);

        if (mCurrentPage == 0) {
            mBtnBack.setVisibility(View.INVISIBLE);
        }
        mViewPager.setCurrentItem(mCurrentPage);
    }

    public void onBtnBack(View v) {
        DebugLogger.d(null);

        int nNewPage = getNextPage(false);

        if (nNewPage != mCurrentPage) {
            mCurrentPage = nNewPage;
            updateButtons();
            setPage(mCurrentPage);
        }
    }

    public void onBtnForward(View v) {
        DebugLogger.d(null);

        int nNewPage = getNextPage(true);

        if (nNewPage == mPages.length) {
            openNextActivity();

        } else if (nNewPage != mCurrentPage) {
            mCurrentPage = nNewPage;
            updateButtons();
            setPage(mCurrentPage);
        }
    }

    private void updateButtons() {

        if (mCurrentPage == 0) {
            mBtnBack.setVisibility(View.INVISIBLE);
        } else if (mCurrentPage == mPages.length - 1) {
            mBtnForward.setVisibility(View.INVISIBLE);
        } else {
            mBtnBack.setVisibility(View.VISIBLE);
            mBtnForward.setVisibility(View.VISIBLE);
        }
    }

    private int getNextPage(boolean moveForward) {
        DebugLogger.d(null);

        int nReturnPage;

        if (moveForward == false) {

            if (mCurrentPage <= 0) {
                nReturnPage = 0;
            } else {
                nReturnPage = mCurrentPage - 1;
            }

        } else {
            if (mCurrentPage >= mPages.length) {
                nReturnPage = mPages.length;
            } else {
                nReturnPage = mCurrentPage + 1;
            }
        }

        return nReturnPage;
    }

    public void onBtnSkip(View v) {
        DebugLogger.d(null);
        openNextActivity();
    }

    private void openNextActivity() {
        DebugLogger.d(null);

        startActivity(new Intent(this, PhotoSelectionActivity.class));
        finish();
    }

    private void setPage(int index) {
        mViewPager.setCurrentItem(index);
        mImagePageIndex.setImageResource(mPageIndexImages[index]);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            DebugLogger.d("position: " + position);
            return mFragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentsList.size();
        }
    }

    public void onBackPressed() {
        DebugLogger.d(null);

        openNextActivity();
    }

}
