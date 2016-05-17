package com.imooc.luckypan;

import android.content.ClipData.Item;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class LuckyPan extends SurfaceView implements Callback, Runnable {
	
	private SurfaceHolder mHolder;
	private Canvas mCanvas;
	
	/**
	 * 用于绘制的线程
	 */
	private Thread t;
	/**
	 * 线程的控制开关
	 */
	private boolean isRunning;
	/**
	 * 转盘的奖项
	 */
	private String[] mStrs = new String[]{"单反相机", "IPAD",
			"恭喜发财", "iphone", "服装一套", "恭喜发财"};
	/**
	 * 奖项的图片
	 */
	private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.f040, 
			R.drawable.iphone, R.drawable.meizi, R.drawable.f015};
	/**
	 * 与图片对应的Bitmap数组
	 */
	private Bitmap[] mImgsBitmap;
	
	private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),
			R.drawable.bg2);
	/**
	 * 奖项的颜色
	 */
	private int[] mColors = new int[]{0xFFFFC300, 0XFFF1701,
			0xFFFFC300, 0XFFF1701, 0xFFFFC300, 0XFFF170E};
	
	private int mItemCount = 6;
	/**
	 * 绘制盘块的画笔
	 */
	private Paint mArcPaint;
	/**
	 * 绘制文本的画笔
	 */
	private Paint mTextPaint;
	
	private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
			20, getResources().getDisplayMetrics());
	/**
	 * 整个盘块的范围
	 */
	private RectF mRange = new RectF();
	/**
	 * 整个盘块的直径
	 */
	private int mRadius;
	/**
	 * 转盘的中心位置
	 */
	private int mCenter;
	/**
	 * 这里我们的padding直接以paddingLeft为准
	 */
	private int mPadding;
	
	/**
	 * 滚动的速度
	 */
	private double mSpeed;
	
	/**
	 * 保证线程间的可见性，使用volatile
	 */
	private volatile float mStartAngle = 0;
	
	/**
	 * 判断是否点击了停止按钮
	 */
	private boolean isShouldEnd;
	
	public LuckyPan(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		
		//获得焦点
		setFocusable(true);
		setFocusableInTouchMode(true);
		//设置常量
		setKeepScreenOn(true);
	}
	
	public LuckyPan(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
		
		mPadding = getPaddingLeft();
		//值径
		mRadius = width - mPadding * 2;
		//中心点
		mCenter = width / 2;
		
		setMeasuredDimension(width, width);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		//初始化绘制盘块的画笔
		mArcPaint = new Paint();
		mArcPaint.setAntiAlias(true);
		mArcPaint.setDither(true);
		
		//初始化绘制文本的画笔
		mTextPaint = new Paint();
		mTextPaint.setColor(0xffffffff);
		mTextPaint.setTextSize(mTextSize);
		
		//初始化盘块绘制的范围
		mRange = new RectF(mPadding, mPadding,
				mPadding + mRadius, mPadding + mRadius);
		
		//初始化图片
		mImgsBitmap = new Bitmap[mItemCount];
		
		for (int i = 0; i < mImgsBitmap.length; i++) {
			mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(),
					mImgs[i]);
		}
		
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
			//不断进行绘制
			long start = System.currentTimeMillis();
			draw();
			long end = System.currentTimeMillis();
			
			if (end - start < 50) {
				try {
					Thread.sleep(50 - (end - start));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void draw() {
		try {
			mCanvas = mHolder.lockCanvas();
			if (mCanvas != null) {
				//draw something
				//绘制背景
				drawBg();
				//绘制盘块
				float tmpAngle = mStartAngle;
				float sweepAngle = 360 / mItemCount;
				
				for (int i = 0; i < mItemCount; i++) {
					mArcPaint.setColor(mColors[i]);
					//绘制盘块
					mCanvas.drawArc(mRange, tmpAngle, sweepAngle,
							true, mArcPaint);
					//绘制文本
					drawText(tmpAngle, sweepAngle, mStrs[i]);
					//绘制Icon
					drawIcon(tmpAngle, mImgsBitmap[i]);
					
					tmpAngle += sweepAngle;
				}
				
				mStartAngle += mSpeed;
				
				//如果点击了停止按钮
				if (isShouldEnd) {
					mSpeed -= 1;
				}
				if (mSpeed <= 0) {
					mSpeed = 0;
					isShouldEnd = false;
				}
			}
		} catch (Exception e) 
		{
		} finally {
			if (mCanvas != null) {
				mHolder.unlockCanvasAndPost(mCanvas);
			}
		}
	}

	/**
	 * 点击启动旋转
	 */
	public void luckyStart(int index) {
		//计算每一项的角度
		float angle = 360 / mItemCount;
		//计算每一项中奖范围（当前index）
		//1->150 ~ 210
		//0->210 ~ 270
		float from = 270 - (index + 1) * angle;
		float end = from + angle;
		
		//设置停下来需要旋转的距离
		float targetFrom = 4 * 360 + from;
		float targetEnd = 4 * 360 + end;
		
		/**
		 * <pre>
		 * v1 -> 0
		 * 且每次-1
		 * (v1 + 0) * (v1 + 1) / 2 = targetFrom;
		 * v1 * v1 + v1 - 2 * targetFrom = 0;
		 * v1 = (-1 + Math.sqrt(1 + 8 * targetFrom)) / 2
		 * <pre>
		 */
		float v1 = (float) ((-1 + Math.sqrt(1 + 8 * targetFrom)) / 2);
		float v2 = (float) ((-1 + Math.sqrt(1 + 8 * targetEnd)) / 2);
		
		mSpeed = v1 + Math.random() * (v2 -v1);
		isShouldEnd = false;
	}
	
	/**
	 * 点击停止旋转
	 */
	public void luckyEnd() {
		mStartAngle = 0;
		isShouldEnd = true;
	}
	
	/**
	 * 转盘是否在旋转
	 * @return
	 */
	public boolean isStart() {
		return mSpeed != 0;
	}
	
	public boolean isShouldEnd() {
		return isShouldEnd;
	}
	
	/**
	 * 绘制Icon
	 * @param tmpAngle
	 * @param bitmap
	 */
	private void drawIcon(float tmpAngle, Bitmap bitmap) {
		//设置图片的宽度为直径的1/8
		int imgWidth = mRadius / 8;
		//Math.PI/180
		float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);
		
		//图片中心点的坐标
		int x = (int) (mCenter + mRadius / 4 * Math.cos(angle)); 
		int y = (int) (mCenter + mRadius / 4 * Math.sin(angle));
		
		//确定图片的位置
		Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2,
				x + imgWidth / 2, y + imgWidth / 2);
		
		mCanvas.drawBitmap(bitmap, null, rect, null);
	}

	/**
	 * 绘制每个盘块的文本
	 * @param tmpAngle
	 * @param sweepAngle
	 * @param string
	 */
	private void drawText(float tmpAngle, float sweepAngle, String string) {
		Path path = new Path();
		path.addArc(mRange, tmpAngle, sweepAngle);
		
		//利用水平偏移量让文字居中
		float textWidth = mTextPaint.measureText(string);
		int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
		//垂直偏移量
		int vOffset = mRadius / 12;
		
		mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
	}

	/**
	 * 绘制背景
	 */
	private void drawBg() {
		mCanvas.drawColor(0xFFFFFFFF);
		mCanvas.drawBitmap(mBgBitmap, null, new RectF(mPadding / 2, mPadding / 2,
				getMeasuredWidth() - mPadding / 2,
				getMeasuredWidth() - mPadding / 2), null);
	}
	
}
