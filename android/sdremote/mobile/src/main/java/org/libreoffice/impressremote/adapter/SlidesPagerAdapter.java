/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/*
 * This file is part of the LibreOffice project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.libreoffice.impressremote.adapter;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.core.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.libreoffice.impressremote.R;
import org.libreoffice.impressremote.communication.SlideShow;
import org.libreoffice.impressremote.util.ImageLoader;

public class SlidesPagerAdapter extends PagerAdapter {
    private final LayoutInflater mLayoutInflater;
    private final ImageLoader mImageLoader;

    private final SlideShow mSlideShow;

    private final View.OnTouchListener mSlideTouchListener;

    public SlidesPagerAdapter(Context aContext, SlideShow aSlideShow, View.OnTouchListener aSlideTouchListener) {
        mLayoutInflater = LayoutInflater.from(aContext);
        mImageLoader = new ImageLoader(aContext.getResources(), R.drawable.bg_slide_unknown);

        mSlideShow = aSlideShow;

        mSlideTouchListener = aSlideTouchListener;
    }

    @Override
    public int getCount() {
        return mSlideShow.getSlidesCount();
    }

    @Override
    public Object instantiateItem(ViewGroup aViewGroup, int aPosition) {
        ImageView aSlideView = (ImageView) mLayoutInflater.inflate(R.layout.view_pager_slide, aViewGroup, false);

        if (mSlideShow.getSlidePreviewBytes(aPosition) != null) {
            byte[] aSlidePreviewBytes = mSlideShow.getSlidePreviewBytes(aPosition);

            mImageLoader.loadImage(aSlideView, aSlidePreviewBytes);
            ImageViewCompat.setImageTintList(aSlideView, null);
        }
        else {
            aSlideView.setImageResource(R.drawable.bg_slide_unknown);
        }

        // touch listener that handles tap and double-tap
        aSlideView.setOnTouchListener(mSlideTouchListener);

        aViewGroup.addView(aSlideView);

        return aSlideView;
    }

    @Override
    public void destroyItem(ViewGroup aViewGroup, int aPosition, Object aObject) {
        View aView = (View) aObject;

        aViewGroup.removeView(aView);
    }

    @Override
    public boolean isViewFromObject(View aView, Object aObject) {
        return aView == aObject;
    }

    @Override
    public int getItemPosition(Object aObject) {
        // There seems no other way to update slides with notifyDataSetChanged.

        return POSITION_NONE;
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
