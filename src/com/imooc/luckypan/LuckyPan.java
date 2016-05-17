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
	 * ���ڻ��Ƶ��߳�
	 */
	private Thread t;
	/**
	 * �̵߳Ŀ��ƿ���
	 */
	private boolean isRunning;
	/**
	 * ת�̵Ľ���
	 */
	private String[] mStrs = new String[]{"�������", "IPAD",
			"��ϲ����", "iphone", "��װһ��", "��ϲ����"};
	/**
	 * �����ͼƬ
	 */
	private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.f040, 
			R.drawable.iphone, R.drawable.meizi, R.drawable.f015};
	/**
	 * ��ͼƬ��Ӧ��Bitmap����
	 */
	private Bitmap[] mImgsBitmap;
	
	private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),
			R.drawable.bg2);
	/**
	 * �������ɫ
	 */
	private int[] mColors = new int[]{0xFFFFC300, 0XFFF1701,
			0xFFFFC300, 0XFFF1701, 0xFFFFC300, 0XFFF170E};
	
	private int mItemCount = 6;
	/**
	 * �����̿�Ļ���
	 */
	private Paint mArcPaint;
	/**
	 * �����ı��Ļ���
	 */
	private Paint mTextPaint;
	
	private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
			20, getResources().getDisplayMetrics());
	/**
	 * �����̿�ķ�Χ
	 */
	private RectF mRange = new RectF();
	/**
	 * �����̿��ֱ��
	 */
	private int mRadius;
	/**
	 * ת�̵�����λ��
	 */
	private int mCenter;
	/**
	 * �������ǵ�paddingֱ����paddingLeftΪ׼
	 */
	private int mPadding;
	
	/**
	 * �������ٶ�
	 */
	private double mSpeed;
	
	/**
	 * ��֤�̼߳�Ŀɼ��ԣ�ʹ��volatile
	 */
	private volatile float mStartAngle = 0;
	
	/**
	 * �ж��Ƿ�����ֹͣ��ť
	 */
	private boolean isShouldEnd;
	
	public LuckyPan(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		
		//��ý���
		setFocusable(true);
		setFocusableInTouchMode(true);
		//���ó���
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
		//ֵ��
		mRadius = width - mPadding * 2;
		//���ĵ�
		mCenter = width / 2;
		
		setMeasuredDimension(width, width);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		//��ʼ�������̿�Ļ���
		mArcPaint = new Paint();
		mArcPaint.setAntiAlias(true);
		mArcPaint.setDither(true);
		
		//��ʼ�������ı��Ļ���
		mTextPaint = new Paint();
		mTextPaint.setColor(0xffffffff);
		mTextPaint.setTextSize(mTextSize);
		
		//��ʼ���̿���Ƶķ�Χ
		mRange = new RectF(mPadding, mPadding,
				mPadding + mRadius, mPadding + mRadius);
		
		//��ʼ��ͼƬ
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
			//���Ͻ��л���
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
				//���Ʊ���
				drawBg();
				//�����̿�
				float tmpAngle = mStartAngle;
				float sweepAngle = 360 / mItemCount;
				
				for (int i = 0; i < mItemCount; i++) {
					mArcPaint.setColor(mColors[i]);
					//�����̿�
					mCanvas.drawArc(mRange, tmpAngle, sweepAngle,
							true, mArcPaint);
					//�����ı�
					drawText(tmpAngle, sweepAngle, mStrs[i]);
					//����Icon
					drawIcon(tmpAngle, mImgsBitmap[i]);
					
					tmpAngle += sweepAngle;
				}
				
				mStartAngle += mSpeed;
				
				//��������ֹͣ��ť
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
	 * ���������ת
	 */
	public void luckyStart(int index) {
		//����ÿһ��ĽǶ�
		float angle = 360 / mItemCount;
		//����ÿһ���н���Χ����ǰindex��
		//1->150 ~ 210
		//0->210 ~ 270
		float from = 270 - (index + 1) * angle;
		float end = from + angle;
		
		//����ͣ������Ҫ��ת�ľ���
		float targetFrom = 4 * 360 + from;
		float targetEnd = 4 * 360 + end;
		
		/**
		 * <pre>
		 * v1 -> 0
		 * ��ÿ��-1
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
	 * ���ֹͣ��ת
	 */
	public void luckyEnd() {
		mStartAngle = 0;
		isShouldEnd = true;
	}
	
	/**
	 * ת���Ƿ�����ת
	 * @return
	 */
	public boolean isStart() {
		return mSpeed != 0;
	}
	
	public boolean isShouldEnd() {
		return isShouldEnd;
	}
	
	/**
	 * ����Icon
	 * @param tmpAngle
	 * @param bitmap
	 */
	private void drawIcon(float tmpAngle, Bitmap bitmap) {
		//����ͼƬ�Ŀ��Ϊֱ����1/8
		int imgWidth = mRadius / 8;
		//Math.PI/180
		float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);
		
		//ͼƬ���ĵ������
		int x = (int) (mCenter + mRadius / 4 * Math.cos(angle)); 
		int y = (int) (mCenter + mRadius / 4 * Math.sin(angle));
		
		//ȷ��ͼƬ��λ��
		Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2,
				x + imgWidth / 2, y + imgWidth / 2);
		
		mCanvas.drawBitmap(bitmap, null, rect, null);
	}

	/**
	 * ����ÿ���̿���ı�
	 * @param tmpAngle
	 * @param sweepAngle
	 * @param string
	 */
	private void drawText(float tmpAngle, float sweepAngle, String string) {
		Path path = new Path();
		path.addArc(mRange, tmpAngle, sweepAngle);
		
		//����ˮƽƫ���������־���
		float textWidth = mTextPaint.measureText(string);
		int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
		//��ֱƫ����
		int vOffset = mRadius / 12;
		
		mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
	}

	/**
	 * ���Ʊ���
	 */
	private void drawBg() {
		mCanvas.drawColor(0xFFFFFFFF);
		mCanvas.drawBitmap(mBgBitmap, null, new RectF(mPadding / 2, mPadding / 2,
				getMeasuredWidth() - mPadding / 2,
				getMeasuredWidth() - mPadding / 2), null);
	}
	
}
