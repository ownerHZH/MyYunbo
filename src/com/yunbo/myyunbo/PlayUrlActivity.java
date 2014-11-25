package com.yunbo.myyunbo;

import com.ab.activity.AbActivity;
import com.ab.util.AbStrUtil;
import com.ab.util.AbToastUtil;
import com.yunbo.control.DyUtil;
import com.yunbo.control.HistoryUtil;
import com.yunbo.mode.History;
import com.yunbo.mode.Node;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayUrlActivity extends AbActivity {

	EditText urlText;
	TextView xl;
	TextView xf;
	
	Node nodePlay;
	boolean ismain;
	private static boolean isError = false;

	@Override
	protected void onPause() {
		isBack = true;
		super.onPause();
	}

	@Override
	protected void onResume() {
		isBack = false;
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		isBack = true;
		super.onDestroy();
	}

	public boolean isBack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_play_url);

		Bundle be = getIntent().getExtras();
		ismain = be != null;

		urlText = (EditText) findViewById(R.id.editText1);
		xl = (TextView) findViewById(R.id.textView1);
		xf = (TextView) findViewById(R.id.textView2);
		((TextView)findViewById(R.id.textView3)).setText("");
		findViewById(R.id.imageView1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					DyUtil.fileList.clear();
					toplay();
					// thunderPlay();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					AbToastUtil.showToast(PlayUrlActivity.this, "程序内存溢出，请退出重启");
					isError = true;
					finish();
				}
			}
		});
		xl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					toplay();
					// thunderPlay();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					AbToastUtil.showToast(PlayUrlActivity.this, "程序内存溢出，请退出重启");
					isError = true;
					finish();
				}
			}
		});
		xf.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					qqxfPlay();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					AbToastUtil.showToast(PlayUrlActivity.this, "程序内存溢出，请退出重启");
					isError = true;
					finish();
				}
			}
		});
		nodePlay = DyUtil.nodePlay;
		DyUtil.nodePlay = null;
		if (nodePlay != null) {
			urlText.setText(nodePlay.getUrl());
		}
		if (DyUtil.urlToPlay != null) {
			urlText.setText(DyUtil.urlToPlay);
			DyUtil.urlToPlay = null;
		}
		/**/
		if (ismain) {
			((Button) findViewById(R.id.button1))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							Intent intent = new Intent();
							intent.setClass(PlayUrlActivity.this,
									HomeActivity.class);
							startActivity(intent);
						}
					});
			((Button) findViewById(R.id.button2))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							Intent intent = new Intent();
							intent.setClass(PlayUrlActivity.this,
									Ed2kSSActivity.class);
							startActivity(intent);
						}
					});

			((Button) findViewById(R.id.button3))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							Intent intent = new Intent();
							intent.setClass(PlayUrlActivity.this,
									FilmActivity.class);
							startActivity(intent);
						}
					});
			((ImageView) findViewById(R.id.historyiv))
			.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					Intent intent = new Intent();
					intent.setClass(PlayUrlActivity.this,
							HistoryActivity.class);
					startActivity(intent);
				}
			});
		} else {
			findViewById(R.id.linearlayout1).setVisibility(View.GONE);
			((ImageView) findViewById(R.id.historyiv)).setVisibility(View.GONE);
		}

		infroTv=((TextView) findViewById(R.id.textView3));
				//.setText();
	}
	TextView infroTv;
	@Override
	public void onBackPressed() {
		if (ismain) {
			finish();
		} else {
			super.onBackPressed();
		}
		// System.exit(0);
		return;
	}

	private void thunderPlay() {
		// TODO Auto-generated method stub
		String url = urlText.getText().toString().trim() + "";
		if (check(url)) {

			if (nodePlay != null) {
				urlText.setText(url = nodePlay.getThunder());
			}
			url = DyUtil.changeED2K(url);
			DyUtil.thunderPlay(url, this);
		}
	}

	private boolean check(String url) {
		// TODO Auto-generated method stub
		if (AbStrUtil.isEmpty(url)) {

			AbToastUtil.showToast(this, "请输入正确格式链接");

			return false;
		}
		if (url.startsWith("ftp://") || url.startsWith("http://")
				|| url.startsWith("magnet:?xt=urn:btih:")
				|| url.startsWith("thunder://") || url.startsWith("ed2k://")) {
			return true;
		}
		AbToastUtil.showToast(this, "请输入正确格式链接");

		return false;
	}

	private void qqxfPlay() {
		// TODO Auto-generated method stub

		String url = urlText.getText().toString().trim() + "";
		if (check(url)) {

			if (nodePlay != null) {
				urlText.setText(url = nodePlay.getThunder());
			}
			url = DyUtil.changeED2K(url);
			DyUtil.myHandler = new Handler();
			DyUtil.qqxfPlay(url, this);
		}
	}

	private void toplay() {
		// TODO Auto-generated method stub
		infroTv.setText("");
		String url = urlText.getText().toString().trim() + "";
		if (check(url)) {

			if (nodePlay != null) {
				urlText.setText(url = nodePlay.getThunder());
			}
		if (ismain) {
			HistoryUtil.data=new History();
			HistoryUtil.setName(url);
		}
			url = DyUtil.changeED2K(url);
			
			if (DyUtil.myHandler ==null) {
				DyUtil.myHandler = new Handler();
			}
			DyUtil.toGetPlayUrl(url, this);
			
			/*
			if (url.startsWith("ftp://") || url.startsWith("http://")
					|| url.startsWith("thunder://")) {
				DyUtil.thunderPlay(url, this);
			} else {
				DyUtil.myHandler = new Handler();
				DyUtil.qqxfPlay(url, this);

			}*/
		}
	}

}
