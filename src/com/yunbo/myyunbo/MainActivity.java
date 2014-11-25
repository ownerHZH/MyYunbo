package com.yunbo.myyunbo;

import com.ab.activity.AbActivity;
import com.yunbo.control.DyUtil;
import com.yunbo.control.HistoryUtil;
import com.yunbo.mode.PageContent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends AbActivity {

	private ProgressBar pro;
	int i=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_main);
		pro=(ProgressBar) findViewById(R.id.progressBar1);
		pro.setMax(15);
		DyUtil.PATH =  getFilesDir()+"/";
		HistoryUtil.read(this);
		new Thread(new Runnable() { 
			@Override
			public void run() { 
				DyUtil.init();
				LanunyActivity.readkeys();

				PageContent data=DyUtil.getPageContent(1);
				 if (data!=null) { 
					 DyUtil.videos= data.getVideos();
				}
			}
		}).start();
		new Handler().postDelayed(new Runnable(	) {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putBoolean("ismain", true); 
				intent.setClass(MainActivity.this, PlayUrlActivity.class); 
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		}, 1500);
		/*
		((Button)findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent();
				intent.setClass(MainActivity.this, HomeActivity.class); 
				startActivity(intent);
			}
		});
((Button)findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent();
				intent.setClass(MainActivity.this, Ed2kSSActivity.class); 
				startActivity(intent);
			}
		});
( findViewById(R.id.imageView1)).setOnClickListener(new View.OnClickListener() {
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		Intent intent = new Intent();
		intent.setClass(MainActivity.this, VideoViewBuffer.class); 
		startActivity(intent);
	}
});*/
		handler.sendEmptyMessageDelayed(0, 100);
	}
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				pro.setProgress(i++);
				if(i<pro.getMax())
				handler.sendEmptyMessageDelayed(0, 100);
				break;
			}
		}
	}; 
	
	
}
