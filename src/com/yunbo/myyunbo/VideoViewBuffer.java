package com.yunbo.myyunbo;

import java.util.ArrayList;
import java.util.List;

import com.ab.util.AbToastUtil;   
import com.yunbo.control.DyUtil;
import com.yunbo.control.HistoryUtil;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoViewBuffer extends Activity implements OnInfoListener,
		OnBufferingUpdateListener, OnErrorListener, OnCompletionListener {

	/**
	 * TODO: Set the path variable to a streaming video URL or a local media
	 * file path.
	 */
	private String path = "http://gdl.lixian.vip.xunlei.com/download?fid=f2DY/mkuCFD5GUyNz63T0UK3lV0tYJAcAAAAANUajmIGCFQIN1wah36auVVQM5UT&mid=666&threshold=150&tid=D304B9A1B18BC06F497296362BFA6B24&srcid=4&verno=1&g=D51A8E6206085408375C1A877E9AB95550339513&scn=t20&dt=17&ui=340538966&s=479223853&n=0E&it=1416791230&cc=8089487440268664372&p=0&specid=225536";
	private Uri uri;
	private VideoView mVideoView;
	private ProgressBar pb;
	private TextView downloadRateView, loadRateView;
	TextView fileNameTV,ratetv;
	MediaController controller;
	private List<String> files=new ArrayList<String>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		path = DyUtil.playUrl;
		if (HistoryUtil.seek!=0) {
			seek=HistoryUtil.seek;
			HistoryUtil.seek=0L;
		}
		files=DyUtil.fileList; 
		System.out.println("ok");
		setContentView(R.layout.videobuffer);
		mVideoView = (VideoView) findViewById(R.id.buffer);
		pb = (ProgressBar) findViewById(R.id.probar);

		downloadRateView = (TextView) findViewById(R.id.download_rate);
		loadRateView = (TextView) findViewById(R.id.load_rate);

		fileNameTV=(TextView) findViewById( R.id.nametv);
		ratetv=(TextView) findViewById( R.id.ratetv);
		fileNameTV.setText(HistoryUtil.data.getName());

		if (path == "") {
			// Tell the user to provide a media file URL/path.
			Toast.makeText(VideoViewBuffer.this, "视频无法播放！", Toast.LENGTH_LONG)
					.show();
			return;
		} else {
			/*
			 * Alternatively,for streaming media you can use
			 * mVideoView.setVideoURI(Uri.parse(URLstring));
			 */
			// fileNameTV.setText("");
			// fileNameTV.;
			uri = Uri.parse(path);
			mVideoView.setVideoURI(uri);
			mVideoView.setMediaController(controller=new MediaController(this));
			mVideoView.requestFocus();
			mVideoView.setOnInfoListener(this);
			mVideoView.setOnBufferingUpdateListener(this);
			mVideoView
					.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mediaPlayer) {
							// optional need Vitamio 4.0
							mediaPlayer.setPlaybackSpeed(1.0f);
							if (seek != 0L&&seek<mediaPlayer.getDuration()) {
								mediaPlayer.seekTo(seek);
								seek = 0L;
							}
						}
					});
			mVideoView.setOnErrorListener(this);
			mVideoView.setOnCompletionListener(this);
			//controller.setFileName(name);
			controller.setOnShownListener(new MediaController.OnShownListener() {
				
				@Override
				public void onShown() {
					// TODO Auto-generated method stub
					fileNameTV.setVisibility(View.VISIBLE);
				}
			});
			controller.setOnHiddenListener(new MediaController.OnHiddenListener() {
				
				@Override
				public void onHidden() {
					// TODO Auto-generated method stub
					fileNameTV.setVisibility(View.GONE);
				}
			});
			
			

			mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
			mOperationBg = (ImageView) findViewById(R.id.operation_bg);
			mOperationPercent = (ImageView) findViewById(R.id.operation_percent);			

			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			
			mGestureDetector = new GestureDetector(this, new MyGestureListener());
			
		}

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			if (mVideoView.isPlaying()) {
				mVideoView.pause();
				pb.setVisibility(View.VISIBLE);
				downloadRateView.setText("");
				loadRateView.setText("");
				downloadRateView.setVisibility(View.VISIBLE);
				loadRateView.setVisibility(View.VISIBLE);
				fileNameTV.setVisibility(View.VISIBLE);
				controller.show(30*1000);
			}
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			mVideoView.start();
			pb.setVisibility(View.GONE);
			downloadRateView.setVisibility(View.GONE);
			loadRateView.setVisibility(View.GONE);
			fileNameTV.setVisibility(View.GONE);
			controller.hide();
			break;
		case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
			//downloadRateView.setText("" + extra + "kb/s" + "  ");
			ratetv.setText("" + extra + "kb/s" + "  ");
			break;
		}
		return true;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		loadRateView.setText(percent + "%");

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (loadRateView != null) {
			pb.setVisibility(View.VISIBLE);
			ratetv.setText("" + 0 + "kb/s" + "  ");
			loadRateView.setText("正在连接播放服务器");
			loadRateView.setVisibility(View.VISIBLE);
			if (seek != 0L) {
				loadRateView.setText("正在恢复播放进度");

			}
		}
		if (mVideoView != null)
		{
			if(isPause){
				try {
					mVideoView.resume();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
		}
		if (controller!=null) {
			controller.show();
		}
	} 

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (mVideoView != null)
				mVideoView.stopPlayback();			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	int fileindex=0;
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		if (files.size()==0) {
			files=DyUtil.readFiles(path, getFilesDir()+"/");
		}
		if (files.size()!=0&&fileindex==0) {
			fileindex=1;

			uri = Uri.parse(files.get(0));
			mVideoView.setVideoURI(uri);
			return true;
		}
		if (what == 0) {

			Toast.makeText(this, "未知错误。", Toast.LENGTH_LONG).show();
			 
		} else {
			Toast.makeText(this, "视频类型不支持或者文件路径错误。", Toast.LENGTH_LONG).show();
		}
		finish();
		return false;
	}

	long seek = 0L;
    private boolean isPause=false;
	@Override
	protected void onPause() {
		seek = mVideoView.getCurrentPosition();
		HistoryUtil.data.setSeek(seek);
		HistoryUtil.data.setUrl(path);
		HistoryUtil.save(this);
		super.onPause();
		try {
			if (mVideoView != null){
				mVideoView.pause();
				isPause=true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if (files.size()!=0&&fileindex>0) {
			if (fileindex<files.size()) {
				uri = Uri.parse(files.get(fileindex));
				mVideoView.setVideoURI(uri);
				fileindex++;

				pb.setVisibility(View.VISIBLE);
				loadRateView.setText("正在加载下一个视频("+fileindex+"/"+files.size()+")");
				loadRateView.setVisibility(View.VISIBLE);
				return;
			}
		}
		AbToastUtil.showToast(this, "播放完成。");
		finish();
	}
	
	
	
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;

		// 处理手势结束
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endGesture();
			break;
		}

		return super.onTouchEvent(event);
	}
 
 
	
	/** 手势结束 */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 500);
	}

	private class MyGestureListener extends SimpleOnGestureListener {

		/** 双击 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
				mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			else
				mLayout++;
			if (mVideoView != null)
				mVideoView.setVideoLayout(mLayout, 0);
			return true;
		}

		/** 滑动 */
		@SuppressWarnings("deprecation")
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			float mOldX = e1.getX(), mOldY = e1.getY();
			int y = (int) e2.getRawY();
			Display disp = getWindowManager().getDefaultDisplay();
			int windowWidth = disp.getWidth();
			int windowHeight = disp.getHeight();

			if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
				onVolumeSlide((mOldY - y) / windowHeight);
			else if (mOldX < windowWidth / 5.0)// 左边滑动
				onBrightnessSlide((mOldY - y) / windowHeight);

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	/** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};

	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 滑动改变亮度
	 * 
	 * @param percent
	 */
	private void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}
	

	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 当前缩放模式 */
	private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	private GestureDetector mGestureDetector; 

}
