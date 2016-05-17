package com.imooc.luckypan;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class SurfaceViewTemplate extends SurfaceView implements Callback, Runnable {
	
	private SurfaceHolder mHolder;
	private Canvas mCanvas;
	
	/**
	 * ���ڻ��Ƶ��߳�
	 */
	private Thread t;
	/**
	 * �̵߳Ŀ��ƿ���
	 */
	private boolean isRunning;

	public SurfaceViewTemplate(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		
		//��ý���
		setFocusable(true);
		setFocusableInTouchMode(true);
		//���ó���
		setKeepScreenOn(true);
	}
	
	public SurfaceViewTemplate(Context context) {
		this(context, null);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		isRunning = true;
		
		t = new Thread(this);
		t.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		isRunning = false;
	}

	@Override
	public void run() {
		while (isRunning) {
			draw();
		}
	}

	private void draw() {
		try {
			mCanvas = mHolder.lockCanvas();
			if (mCanvas != null) {
				//draw something
			}
		} catch (Exception e) 
		{
		} finally {
			if (mCanvas != null) {
				mHolder.unlockCanvasAndPost(mCanvas);
			}
		}
	}
}
