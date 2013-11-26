/*
 *     Created by Daniel Nadeau
 *     daniel.nadeau01@gmail.com
 *     danielnadeau.blogspot.com
 *
 *     Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.echo.holographlibrary;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BarGraph extends View {

	private final static int AXIS_LABEL_FONT_SIZE = 15;

    private ArrayList<Bar> mBars = new ArrayList<Bar>();
    private Paint mPaint = new Paint();
    private Rect mRect = null;
    private boolean mShowBarText = false;
    private boolean mShowBarTextMax = false;
    private int mIndexSelected = -1;
    private OnBarClickedListener mListener;
    private Bitmap mFullImage;
    private boolean mShouldUpdate = false;
    private float mPadding = 7.0f;
    private float mBottomPadding = 30.0f;
    private double mMaxValue = 0;
    private int mValueFontSize = 10;

    private Context mContext = null;

    public BarGraph(Context context) {
        super(context);
        mContext = context;
    }

    public BarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public BarGraph setShowBarText(boolean show){
        mShowBarText = show;
        return this;
    }

    public BarGraph setShowBarTextMax(boolean show){
        mShowBarTextMax = show;
        return this;
    }

    public BarGraph setBars(ArrayList<Bar> points){
        mBars = points;
        mShouldUpdate = true;
        postInvalidate();
        return this;
    }

    public BarGraph setPadding(float factor) {
        mPadding = factor;
        return this;
    }

    public BarGraph setBottomPadding(float pad) {
        mBottomPadding = pad;
        return this;
    }

    public BarGraph setValueFontSize(int size) {
        mValueFontSize = size;
        return this;
    }

    public ArrayList<Bar> getBars(){
        return mBars;
    }

    void drawGraph(boolean drawAxis, boolean drawBars) {
        mFullImage = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(mFullImage);
        canvas.drawColor(Color.TRANSPARENT);
        NinePatchDrawable popup = (NinePatchDrawable)this.getResources().getDrawable(R.drawable.popup_black);

        float density       = mContext.getResources().getDisplayMetrics().density;
        float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
        float padding       = mPadding * density;
        int selectPadding   = (int) (4 * density);
        float bottomPadding = mBottomPadding * density;
        double maxValue     = mMaxValue <= 0 ? 1 : mMaxValue;

        // Draw x-axis line
        if (drawAxis) {
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(2 * density);
            mPaint.setAlpha(50);
            mPaint.setAntiAlias(true);
            canvas.drawLine(0, getHeight()-bottomPadding + 10*density, getWidth(), getHeight()-bottomPadding+10*density, mPaint);
        }

        float barWidth = (getWidth() - (padding*2)*mBars.size())/mBars.size();


        float usableHeight;
        if (mShowBarText || mShowBarTextMax) {
            mPaint.setTextSize(mValueFontSize * scaledDensity);
            Rect r3 = new Rect();
            mPaint.getTextBounds("$", 0, 1, r3);
            usableHeight = getHeight()-bottomPadding-Math.abs(r3.top-r3.bottom)-24 * density;
        } else {
            usableHeight = getHeight()-bottomPadding;
        }

        mRect = new Rect();

        if (drawBars) {
            int count = 0;

            boolean maxShown = false;
            for (final Bar bar : mBars) {
                // Set bar bounds
                int left   = (int)((padding*2)*count + padding + barWidth*count);
                int top    = (int)(getHeight()-bottomPadding-(usableHeight*(bar.getValue() / maxValue)));
                int right  = (int)((padding*2)*count + padding + barWidth*(count+1));
                int bottom = (int)(getHeight()-bottomPadding);
                mRect.set(left, top, right, bottom);

                // Draw bar
                mPaint.setColor(bar.getColor());
                mPaint.setAlpha(255);
                canvas.drawRect(mRect, mPaint);

                // Create selection region
                Path path = new Path();
                path.addRect(new RectF(mRect.left-selectPadding, mRect.top-selectPadding, mRect.right+selectPadding, mRect.bottom+selectPadding), Path.Direction.CW);
                bar.setPath(path);
                bar.setRegion(new Region(mRect.left-selectPadding, mRect.top-selectPadding, mRect.right+selectPadding, mRect.bottom+selectPadding));

                // Draw x-axis label text
                mPaint.setTextSize(AXIS_LABEL_FONT_SIZE * mContext.getResources().getDisplayMetrics().scaledDensity);
                int x = (int)(((mRect.left+mRect.right)/2)-(mPaint.measureText(bar.getName())/2));
                int y = (int) (getHeight()-3 * mContext.getResources().getDisplayMetrics().scaledDensity);
                canvas.drawText(bar.getName(), x, y, mPaint);

                boolean showThisMax = (!maxShown && bar.getValue() == maxValue);
                maxShown |= showThisMax;

                // Draw value text
                if (mShowBarText || (mShowBarTextMax && showThisMax)) {
                    mPaint.setTextSize(mValueFontSize * mContext.getResources().getDisplayMetrics().scaledDensity);
                    mPaint.setColor(Color.WHITE);
                    Rect r2 = new Rect();
                    String text = bar.getValueString();
                    mPaint.getTextBounds(text, 0, 1, r2);

                    int boundLeft = (int) (((mRect.left+mRect.right)/2)-(mPaint.measureText(bar.getValueString())/2)-10 * density);
                    int boundTop = (int) (mRect.top+(r2.top-r2.bottom)-18 * density);
                    int boundRight = (int)(((mRect.left+mRect.right)/2)+(mPaint.measureText(bar.getValueString())/2)+10 * density);
                    popup.setBounds(boundLeft, boundTop, boundRight, mRect.top);
                    popup.draw(canvas);

                    canvas.drawText(text, (int)(((mRect.left+mRect.right)/2)-(mPaint.measureText(text))/2), mRect.top-(mRect.top - boundTop)/2f+(float)Math.abs(r2.top-r2.bottom)/2f*0.7f, mPaint);
                }
                if (mIndexSelected == count && mListener != null) {
                    mPaint.setColor(Color.parseColor("#33B5E5"));
                    mPaint.setAlpha(100);
                    canvas.drawPath(bar.getPath(), mPaint);
                    mPaint.setAlpha(255);
                }
                count++;
            }
        }
    }

    public void onDraw(Canvas ca) {

        boolean drawBars = true;

        if (mFullImage == null || mShouldUpdate) {
            mMaxValue = 0;
            // Maximum y value = sum of all values.
            for (final Bar bar : mBars) {
                if (bar.getValue() > mMaxValue) {
                    mMaxValue = bar.getValue();
                }
            }

            if (mMaxValue <= 0) {
                drawBars = false;
            }

            drawGraph(true, true);

            mShouldUpdate = false;
        }

        ca.drawBitmap(mFullImage, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int count = 0;
        for (Bar bar : mBars){
            Region r = new Region();
            r.setPath(bar.getPath(), bar.getRegion());
            if (r.contains((int)point.x,(int) point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
                mIndexSelected = count;
            } else if (event.getAction() == MotionEvent.ACTION_UP){
                if (r.contains((int)point.x,(int) point.y) && mListener != null){
                    if (mIndexSelected > -1) mListener.onClick(mIndexSelected);
                    mIndexSelected = -1;
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_CANCEL)
            	mIndexSelected = -1;

            count++;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
            mShouldUpdate = true;
            postInvalidate();
        }

        return true;
    }

    @Override
    protected void onDetachedFromWindow()
    {
    	if(mFullImage != null)
    		mFullImage.recycle();

    	super.onDetachedFromWindow();
    }

    public void setOnBarClickedListener(OnBarClickedListener listener) {
        mListener = listener;
    }

    public interface OnBarClickedListener {
        abstract void onClick(int index);
    }
}
