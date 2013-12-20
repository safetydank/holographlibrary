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

    public interface LabelGenerator {
        // public String getLabel(double value);
        public void mapLabels(double[] values, String[] labels);
    }

    private final static int X_AXIS_LABEL_FONT_SIZE = 12;
    private final static int Y_AXIS_LABEL_FONT_SIZE = 12;

    private ArrayList<Bar> mBars = new ArrayList<Bar>();
    private Paint mPaint = new Paint();
    private boolean mShowBarText = false;
    private boolean mShowBarTextMax = false;
    private boolean mShowGraphLines = false;
    private int mIndexSelected = -1;
    private OnBarClickedListener mListener;
    private Bitmap mFullImage;
    private boolean mShouldUpdate = false;
    private float mPadding = 7.0f;
    private float mBottomPadding = 30.0f;
    private double mMaxValue = 0;
    private int mValueFontSize = 10;
    private int mGraphLineColor;
    private int mXLabelColor;
    private int mYLabelColor;
    private LabelGenerator mLabelGenerator = null;

    private Context mContext = null;

    public BarGraph(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    void init() {
        //  Defaults
        mGraphLineColor = Color.parseColor("#dddddd");
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

    public BarGraph setShowGraphLines(boolean show) {
        mShowGraphLines = show;
        return this;
    }

    public BarGraph setGraphLineColor(int color) {
        mGraphLineColor = color;
        return this;
    }

    public BarGraph setXLabelColor(int color) {
        mXLabelColor = color;
        return this;
    }

    public BarGraph setYLabelColor(int color) {
        mYLabelColor = color;
        return this;
    }

    public BarGraph setLabelGenerator(LabelGenerator gen) {
        mLabelGenerator = gen;
        return this;
    }

    public ArrayList<Bar> getBars(){
        return mBars;
    }

    /**
     * +--+---+
     * |1 |2  |   1 - yLabelRect
     * |  |   |   2 - graphRect
     * |  |   |   3 - xLabelRect
     * |  +---+
     * |  |3  |
     * +--+---+
     */
    void drawGraph2(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        final float density = mContext.getResources().getDisplayMetrics().density;
        final float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //  Space between bars
        final float kPadding = mPadding * density;
        final float kLeftOverhang = 10.0f * density;
        final float kLineHeight = 30.0f * density;

        // int kYLabelWidth = 40 + ((int) kLeftOverhang);

        //  Calculate x-axis label text height
        mPaint.setTextSize(X_AXIS_LABEL_FONT_SIZE * scaledDensity);
        Rect rect = new Rect();
        mPaint.getTextBounds("$", 0, 1, rect);
        final float kXLabelHeight = rect.height() + 2.0f * kPadding;

        //  Create labels with 0-width yLabelRect
        RectF yLabelRect = new RectF(0, kPadding * 2.0f, 0, height-1);
        RectF xLabelRect = new RectF(0, height - kXLabelHeight, width-1, height-1);
        RectF graphRect  = new RectF(0, kPadding * 2.0f, width-1, height - kXLabelHeight);

        //  Horizontal graph lines
        boolean validValues = (mMaxValue > 0);
        double maxValue = !validValues ? 1 : mMaxValue;

        //  Generate vertical labels
        int yLineCount = (int) (graphRect.height() / kLineHeight + 1);
        double yLineAmount = (kLineHeight / graphRect.height() * maxValue);

        String[] yLabels = new String[yLineCount];
        double[] yValues = new double[yLineCount];

        if (!validValues) {
            for (int i=0; i < yLabels.length; ++i) {
                yLabels[i] = null;
            }
        }

        for (int i=0; i < yLineCount; ++i) {
            yValues[i] = i * yLineAmount;
            if (validValues && mLabelGenerator == null) {
                yLabels[i] = String.valueOf(yValues[i]);
            }
        }

        if (validValues && mLabelGenerator != null) {
            mLabelGenerator.mapLabels(yValues, yLabels);
        }
        
        float maxWidth = 0;
        mPaint.setTextSize(Y_AXIS_LABEL_FONT_SIZE * scaledDensity);
        for (String label : yLabels) {
            if (label != null) {
                maxWidth = Math.max(maxWidth, mPaint.measureText(label));
            }
        }

        //  Set the width of yLabelRect and adjust others
        yLabelRect.right = maxWidth + 2 * kPadding + kLeftOverhang;
        xLabelRect.left  = yLabelRect.right;
        graphRect.left   = yLabelRect.right;

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
//        canvas.drawRect(yLabelRect, mPaint);
//        canvas.drawRect(xLabelRect, mPaint);
//        canvas.drawRect(graphRect, mPaint);

        float barWidth = (graphRect.width() / mBars.size()) - (kPadding * 2);

        float x;

        if (mShowGraphLines) {
            mPaint.setColor(mGraphLineColor);
            mPaint.setStrokeWidth(1.0f * density);
            mPaint.setStyle(Paint.Style.STROKE);

            //  Vertical lines
            for (int i=0; i < mBars.size() + 1; ++i) {
                x = i * (barWidth + 2 * kPadding) + graphRect.left;
                canvas.drawLine(x, xLabelRect.bottom, x, graphRect.top, mPaint);
            }

            //  Horizontal lines
            for (int i=0; i < yLineCount; ++i) {
                float y = graphRect.bottom - i * kLineHeight;
                canvas.drawLine(graphRect.left - kLeftOverhang, y, graphRect.right, y, mPaint);
            }

        }

        //  Y axis labels
        mPaint.setTextSize(Y_AXIS_LABEL_FONT_SIZE * scaledDensity);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mYLabelColor);
        for (int i=0; i < yLabels.length; ++i) {
            String label = yLabels[i];
            if (label == null)
                continue;

            mPaint.getTextBounds(label, 0, label.length(), rect);

            float tx = yLabelRect.right + - (rect.width() + kPadding + kLeftOverhang);
            float ty = graphRect.bottom - i * kLineHeight + rect.height() / 2;

            canvas.drawText(label, tx, ty, mPaint);
        }

        //  Draw bars
        x = kPadding;
        mPaint.setTextSize(X_AXIS_LABEL_FONT_SIZE * scaledDensity);
        for (final Bar bar : mBars) {
            RectF barRect = new RectF(x, (float) -(graphRect.height() * (bar.getValue() / maxValue)), x+barWidth, 0);
            barRect.offset(graphRect.left, graphRect.bottom);
            x += barWidth + kPadding * 2;

            // Draw bar
            mPaint.setColor(bar.getColor());
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(barRect, mPaint);

            // Draw x-axis label text
            float tx = (int) barRect.centerX() - mPaint.measureText(bar.getName()) / 2;
            float ty = (int) xLabelRect.bottom - kPadding;
            mPaint.setColor(mXLabelColor);
            canvas.drawText(bar.getName(), tx, ty, mPaint);
        }
    }

    void drawGraph() {
        mFullImage = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(mFullImage);
        canvas.drawColor(Color.TRANSPARENT);
        NinePatchDrawable popup = (NinePatchDrawable) getResources().getDrawable(R.drawable.popup_black);

        float density       = mContext.getResources().getDisplayMetrics().density;
        float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
        float padding       = mPadding * density;
        int selectPadding   = (int) (4 * density);
        float bottomPadding = mBottomPadding * density;
        double maxValue     = mMaxValue <= 0 ? 1 : mMaxValue;

        mPaint.setAntiAlias(true);

        float barWidth = (getWidth() / mBars.size()) - (padding * 2);
        float base = getHeight() - bottomPadding;

        float usableHeight = getHeight() - bottomPadding;
        if (mShowBarText || mShowBarTextMax) {
            mPaint.setTextSize(mValueFontSize * scaledDensity);
            Rect r3 = new Rect();
            mPaint.getTextBounds("$", 0, 1, r3);
            usableHeight -= Math.abs(r3.top-r3.bottom)-24 * density;
        }

        if (mShowGraphLines) {
            mPaint.setColor(mGraphLineColor);
            mPaint.setStrokeWidth(1.0f * density);

            //  Vertical lines
            for (int i=0; i < mBars.size() + 1; ++i) {
                float x = i * (barWidth + 2*padding);
                canvas.drawLine(x, base, x, base - (int) usableHeight, mPaint);
            }

            //  Horizontal lines
            float lineHeight = 30.0f * density;
            int hcount = (int) (usableHeight / lineHeight);
            for (int i=0; i < hcount + 1; ++i) {
                float y = base - i * lineHeight;
                canvas.drawLine(0f, y, (float) getWidth(), y, mPaint);
            }
        }

        Rect rect = new Rect();

        int count = 0;

        boolean maxShown = false;
        for (final Bar bar : mBars) {
            // Set bar bounds
            int left   = (int) ((barWidth + padding*2) * count + padding);
            int right  = (int) (left + barWidth);
            int bottom = (int) (getHeight() - bottomPadding);
            int top    = (int) (bottom - (usableHeight*(bar.getValue() / maxValue)));
            rect.set(left, top, right, bottom);

            // Draw bar
            mPaint.setColor(bar.getColor());
            mPaint.setAlpha(255);
            canvas.drawRect(rect, mPaint);

            // Create selection region
            Path path = new Path();
            path.addRect(new RectF(rect.left - selectPadding, rect.top-selectPadding,
                        rect.right + selectPadding, rect.bottom + selectPadding), Path.Direction.CW);
            bar.setPath(path);
            bar.setRegion(new Region(rect.left-selectPadding, rect.top-selectPadding,
                        rect.right+selectPadding, rect.bottom+selectPadding));

            // Draw x-axis label text
            mPaint.setTextSize(X_AXIS_LABEL_FONT_SIZE * scaledDensity);
            int x = (int) (((rect.left+rect.right)/2)-(mPaint.measureText(bar.getName())/2));
            int y = (int) (getHeight()-3 * scaledDensity);
            canvas.drawText(bar.getName(), x, y, mPaint);

            boolean showThisMax = (!maxShown && bar.getValue() == maxValue);
            maxShown |= showThisMax;

            // Draw value text
            if (mShowBarText || (mShowBarTextMax && showThisMax)) {
                mPaint.setTextSize(mValueFontSize * scaledDensity);
                mPaint.setColor(Color.WHITE);
                Rect r2 = new Rect();
                String text = bar.getValueString();
                mPaint.getTextBounds(text, 0, 1, r2);

                float centerX = (rect.left + rect.right) * 0.5f;
                int boundLeft  = (int) (centerX-(mPaint.measureText(bar.getValueString())/2)-10 * density);
                int boundTop   = (int) (rect.top+(r2.top-r2.bottom)-18 * density);
                int boundRight = (int) (centerX+(mPaint.measureText(bar.getValueString())/2)+10 * density);
                popup.setBounds(boundLeft, boundTop, boundRight, rect.top);
                popup.draw(canvas);

                canvas.drawText(text,
                        (int)(centerX-(mPaint.measureText(text))/2),
                        rect.top-(rect.top - boundTop)/2f+(float)Math.abs(r2.top-r2.bottom)/2f*0.7f,
                        mPaint);
            }

            //  Highlight selected
            if (mIndexSelected == count && mListener != null) {
                mPaint.setColor(Color.parseColor("#33B5E5"));
                mPaint.setAlpha(100);
                canvas.drawPath(bar.getPath(), mPaint);
                mPaint.setAlpha(255);
            }
            count++;
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

            // drawGraph();
            mFullImage = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            drawGraph2(mFullImage);

            mShouldUpdate = false;
        }

        ca.drawBitmap(mFullImage, 0, 0, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mShouldUpdate = true;
    }

    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    //     Point point = new Point();
    //     point.x = (int) event.getX();
    //     point.y = (int) event.getY();

    //     int count = 0;
    //     for (Bar bar : mBars){
    //         Region r = new Region();
    //         r.setPath(bar.getPath(), bar.getRegion());
    //         if (r.contains((int)point.x,(int) point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
    //             mIndexSelected = count;
    //         } else if (event.getAction() == MotionEvent.ACTION_UP){
    //             if (r.contains((int)point.x,(int) point.y) && mListener != null){
    //                 if (mIndexSelected > -1) mListener.onClick(mIndexSelected);
    //                 mIndexSelected = -1;
    //             }
    //         }
    //         else if(event.getAction() == MotionEvent.ACTION_CANCEL)
    //          mIndexSelected = -1;

    //         count++;
    //     }

    //     if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
    //         mShouldUpdate = true;
    //         postInvalidate();
    //     }

    //     return true;
    // }

    @Override
    protected void onDetachedFromWindow()
    {
        if (mFullImage != null)
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
