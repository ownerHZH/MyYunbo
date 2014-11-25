package com.yunbo.myyunbo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ab.activity.AbActivity;
import com.ab.task.AbTask;
import com.ab.task.AbTaskItem;
import com.ab.task.AbTaskListListener;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbStrUtil;
import com.ab.util.AbToastUtil;
import com.ab.util.AbViewHolder;
import com.yunbo.control.DyUtil;
import com.yunbo.control.HistoryUtil;
import com.yunbo.mode.Dydata;
import com.yunbo.mode.History;
import com.yunbo.mode.Movie;
import com.yunbo.mode.Node;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FilmActivity extends AbActivity {

	private ListView mListView = null;
	private LayoutInflater mInflater;
	private BaseAdapter myListViewAdapter;
	private ArrayList<Movie> allList = new ArrayList<Movie>();

	private LinearLayout loadingpro;
	private EditText textpage;

	private int page = 0;
	private String tempHtmlpath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_film);
		tempHtmlpath = getCacheDir() + File.pathSeparator + "temp.html";
		mListView = (ListView) this.findViewById(R.id.mListView);
		textpage = (EditText) this.findViewById(R.id.editText1);
		loadingpro = (LinearLayout) this.findViewById(R.id.loadingpro);
		mInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Button gobutton = (Button) this.findViewById(R.id.button1);
		mListView.setAdapter(myListViewAdapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub

				if (convertView == null) {
					// 使用自定义的list_items作为Layout
					convertView = mInflater.inflate(R.layout.sear_item, parent,
							false);
				}
				TextView itemsText = AbViewHolder
						.get(convertView, R.id.tx_item);
				itemsText.setText(allList.get(position).getName());
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return allList.get(position);
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return allList.size();
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				show(allList.get(position).getUrl());
			}
		});
		gobutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				allList.clear();
				page = Integer.parseInt(textpage.getText().toString()) - 1;
				doGo();
			}

		});
		(button2 = (Button) findViewById(R.id.button2))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						doGo();
					}

				});
		page = 0;
		doGo();
	}

	Button button2;

	protected void show(final String url) {
		// TODO Auto-generated method stub
		AbDialogUtil.showProgressDialog(this, R.drawable.progress_circular,
				"正在获取链接...");
		AbTask mAbTask = new AbTask();
		final AbTaskItem item = new AbTaskItem();
		item.setListener(new AbTaskListListener() {
			boolean iscontextok = true;

			@Override
			public List<?> getList() {
				// TODO Auto-generated method stub

				List<Movie> movies = new ArrayList<Movie>();
				try {

					Document doc = Jsoup.connect(url).timeout(30000).get();
					String html = doc.html();
					Pattern pat = Pattern
							.compile("ed2k:\\/\\/\\|file\\|([^\\|]+?)\\|(\\d+)\\|([A-Z0-9]{32})\\|(h=[A-Z0-9]{32}\\|)?\\/?");
					Matcher mat = pat.matcher(html);
					// System.out.println(url);
					while (mat.find()) {
						String name = "" + mat.group(1);
						String text = "" + mat.group(0);
						html = html.replace(text, "");
						name = URLDecoder.decode(name);
						Movie movie = new Movie();
						movie.setName(name);
						movie.setUrl(text);
						movies.add(movie);
					}
					try {
						Element temp23 = doc.select(".temp23").first();
						temp23.select("a").remove();
						String content = temp23.html();
						content = content.substring(0,
								content.indexOf("<!-- Baidu Button BEGIN -->"));
						File file = new File(tempHtmlpath);
						if (!file.exists()) {
							file.createNewFile();
						}
						BufferedOutputStream bi = new BufferedOutputStream(
								new FileOutputStream(file));
						bi.write(content.getBytes("utf-8"));
						bi.flush();
						bi.close();

					} catch (Exception e) {
						// TODO: handle exception
						iscontextok = false;
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				return movies;
			}

			@Override
			public void update(List<?> paramList) {
				// TODO Auto-generated method stub

				AbDialogUtil.removeDialog(FilmActivity.this);

				if (paramList == null) {
					return;
				}
				if (paramList.size() == 0) {
					AbToastUtil.showToast(FilmActivity.this, "查询结果为空！");
					return;
				}
				final List<Movie> list = (List<Movie>) paramList;
				View mView = mInflater.inflate(R.layout.dia_list, null);
				ListView listView = (ListView) mView
						.findViewById(R.id.listView1);
				String[] mStrings = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					mStrings[i] = list.get(i).getName();
				}
				ArrayAdapter<String> listViewAdapter;
				listView.setAdapter(listViewAdapter = new ArrayAdapter<String>(
						FilmActivity.this, R.layout.dialog_list_item_1,
						mStrings));
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// AbDialogUtil.removeDialog(FilmActivity.this);

						HistoryUtil.data = new History();
						HistoryUtil.data.setName(list.get(position).getName());
						DyUtil.urlToPlay = list.get(position).getUrl();
						Intent intent = new Intent(FilmActivity.this,
								PlayUrlActivity.class);
						startActivity(intent);
					}
				});
				WebView mWebView = (WebView) mView.findViewById(R.id.webView1);
				if (iscontextok) {
					WebSettings webSettings = mWebView.getSettings();
					webSettings.setDefaultTextEncodingName("utf-8");
					mWebView.loadUrl("file://" + tempHtmlpath);
				} else {
					mWebView.setVisibility(View.GONE);
				}
				AbDialogUtil.showAlertDialog(mView);
			}
		});
		mAbTask.execute(item);

	}

	private void closeInputMethod() {
		InputMethodManager imm = (InputMethodManager) abApplication
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			// imm.toggleSoftInput(0,
			// InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
			imm.hideSoftInputFromWindow(textpage.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	private void doGo() {
		// TODO Auto-generated method stub
		page++;
		loadingpro.setVisibility(View.VISIBLE);
		button2.setVisibility(View.GONE);
		handler.sendEmptyMessageDelayed(0, 500);
		AbTask mAbTask = new AbTask();
		final AbTaskItem item = new AbTaskItem();
		item.setListener(new AbTaskListListener() {

			@Override
			public List<?> getList() {
				// TODO Auto-generated method stub
				String url = "http://www.gegepa.com/down/ed2k/index"
						+ (page == 1 ? "" : "_" + page) + ".html";
				try {

					Document doc = Jsoup.connect(url).timeout(30000).get();
					Elements zxsyts = doc.select(".zxsyt");
					if (zxsyts.size() <= 0) {
						return null;
					}
					List<Movie> movies = new ArrayList<Movie>();
					for (Element element : zxsyts) {
						Element a = element.select("a").first();
						Movie movie = new Movie();
						movie.setName(a.text());
						movie.setUrl(a.attr("abs:href"));
						movies.add(movie);
					}
					return movies;
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void update(List<?> paramList) {
				// TODO Auto-generated method stub
				loadingpro.setVisibility(View.GONE);
				button2.setVisibility(View.VISIBLE);
				closeInputMethod();
				if (paramList == null) {
					page--;
					return;
				}
				if (paramList.size() == 0) {
					AbToastUtil.showToast(FilmActivity.this, "查询结果为空！");
					return;
				}
				textpage.setText("" + page);
				List<Movie> list = (List<Movie>) paramList;
				allList.addAll(list);
				myListViewAdapter.notifyDataSetChanged();
			}
		});
		mAbTask.execute(item);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				closeInputMethod();
				break;
			}
		}
	};

}
