package com.echo.holographlibrary;

import java.util.ArrayList;

import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BarGraph extends SurfaceView implements SurfaceHolder.Callback {

	private ArrayList<Bar> points = new ArrayList<Bar>();
	private Paint p;
	private Rect r;
	private boolean showBarText = true;
	private int indexSelected = -1;
	private OnBarClickedListener listener;
	
	public BarGraph(Context context) {
		super(context);
	    this.setZOrderOnTop(true); //necessary                
	    getHolder().setFormat(PixelFormat.TRANSPARENT); 
	    getHolder().addCallback(this); 
	}
	
	public BarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
	    this.setZOrderOnTop(true); //necessary                
	    getHolder().setFormat(PixelFormat.TRANSPARENT); 
	    getHolder().addCallback(this); 
	}
	
	public void setShowBarText(boolean show){
		showBarText = show;
	}
	
	public void setBars(ArrayList<Bar> points){
		this.points = points;
		postInvalidate();
	}
	
	public ArrayList<Bar> getBars(){
		return this.points;
	}

	public void onDraw(Canvas canvas) {
		
		canvas.drawColor(Color.TRANSPARENT);
		NinePatchDrawable popup = (NinePatchDrawable)this.getResources().getDrawable(R.drawable.popup_nocolor);
		
		float maxValue = 0;
		float padding = 7;
		int selectPadding = 4;
		float bottomPadding = 40;
		
		if (p == null) p = new Paint();
		
		float usableHeight;
		if (showBarText) {
			this.p.setTextSize(40);
			Rect r3 = new Rect();
			this.p.getTextBounds("$", 0, 1, r3);
			usableHeight = getHeight()-bottomPadding-Math.abs(r3.top-r3.bottom)-26;
		} else {
			usableHeight = getHeight()-bottomPadding;
		}
		 
		
		p.setColor(Color.BLACK);
		p.setStrokeWidth(2);
		p.setAlpha(50);
		p.setAntiAlias(true);
		
		canvas.drawLine(0, getHeight()-bottomPadding+10, getWidth(), getHeight()-bottomPadding+10, p);
		
		float barWidth = (getWidth() - (padding*2)*points.size())/points.size();
		
		for (Bar p : points) {
        	maxValue += p.getValue();
        }
		
		r = new Rect();
		
		int count = 0;
		for (Bar p : points) {
			r.set((int)((padding*2)*count + padding + barWidth*count), (int)(getHeight()-bottomPadding-(usableHeight*(p.getValue()/maxValue))), (int)((padding*2)*count + padding + barWidth*(count+1)), (int)(getHeight()-bottomPadding));
        	
			Path path = new Path();
        	path.addRect(new RectF(r.left-selectPadding, r.top-selectPadding, r.right+selectPadding, r.bottom+selectPadding), Path.Direction.CW);
        	p.setPath(path);
        	p.setRegion(new Region(r.left-selectPadding, r.top-selectPadding, r.right+selectPadding, r.bottom+selectPadding));
			
        	this.p.setColor(p.getColor());
        	this.p.setAlpha(255);
			canvas.drawRect(r, this.p);
			this.p.setTextSize(20);
			canvas.drawText(p.getName(), (int)(((r.left+r.right)/2)-(this.p.measureText(p.getName())/2)), getHeight()-5, this.p);
			if (showBarText){
				this.p.setTextSize(40);
				this.p.setColor(Color.WHITE);
				Rect r2 = new Rect();
				this.p.getTextBounds("$"+p.getValue(), 0, 1, r2);
				popup.setBounds((int)(((r.left+r.right)/2)-(this.p.measureText("$"+p.getValue())/2))-14, r.top+(r2.top-r2.bottom)-26, (int)(((r.left+r.right)/2)+(this.p.measureText("$"+p.getValue())/2))+14, r.top);
				popup.draw(canvas);
				canvas.drawText("$"+p.getValue(), (int)(((r.left+r.right)/2)-(this.p.measureText("$"+p.getValue())/2)), r.top-20, this.p);
			}
			if (indexSelected == count && listener != null) {
				this.p.setColor(Color.parseColor("#33B5E5"));
				this.p.setAlpha(100);
				canvas.drawPath(p.getPath(), this.p);
				this.p.setAlpha(255);
			}
        	count++;
        }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    Point point = new Point();
	    point.x = (int) event.getX();
	    point.y = (int) event.getY();
	    
	    int count = 0;
	    for (Bar bar : points){
	    	Region r = new Region();
	    	r.setPath(bar.getPath(), bar.getRegion());
	    	if (r.contains((int)point.x,(int) point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
	    		indexSelected = count;
	    	} else if (event.getAction() == MotionEvent.ACTION_UP){
	    		if (r.contains((int)point.x,(int) point.y) && listener != null){
	    			listener.onClick(indexSelected);
	    		}
	    		indexSelected = -1;
	    	}
		    count++;
	    }
	    
	    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP){
	    	postInvalidate();
	    }
	    
	    

	    return true;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setWillNotDraw(false); 					//Allows us to use invalidate() to call onDraw()
		postInvalidate();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}
	
	public void setOnBarClickedListener(OnBarClickedListener listener) {
		this.listener = listener;
	}
	
	public abstract class OnBarClickedListener {
		abstract void onClick(int index);
	}
}