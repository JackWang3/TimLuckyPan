package com.imooc.luckypan;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	private LuckyPan mLuckyPan;
	private ImageView mStartBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mLuckyPan = (LuckyPan) findViewById(R.id.id_luckyPan);
		mStartBtn = (ImageView) findViewById(R.id.id_start_button);
		
		mStartBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (!mLuckyPan.isStart()) {
					mLuckyPan.luckyStart(1);
					mStartBtn.setImageResource(R.drawable.stop);
				} else {
					if (!mLuckyPan.isShouldEnd()) {
						mLuckyPan.luckyEnd();
						mStartBtn.setImageResource(R.drawable.start);
					}
				}
			}
		});
	}

}
