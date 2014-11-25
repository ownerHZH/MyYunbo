package com.yunbo.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Base64;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.ab.task.AbTask;
import com.ab.task.AbTaskItem;
import com.ab.task.AbTaskObjectListener;
import com.ab.util.AbDateUtil;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbStrUtil;
import com.ab.util.AbToastUtil;
import com.google.gson.Gson;
import com.yunbo.mode.Dydata;
import com.yunbo.mode.Dyres;
import com.yunbo.mode.Movie;
import com.yunbo.mode.Node;
import com.yunbo.mode.PageContent;
import com.yunbo.mode.Urldata;
import com.yunbo.mode.Video;
import com.yunbo.myyunbo.PlayActivity;
import com.yunbo.myyunbo.PlayUrlActivity;
import com.yunbo.myyunbo.R;
import com.yunbo.myyunbo.SearchActivity;
import com.yunbo.myyunbo.SelectPlayerActivity;
import com.yunbo.myyunbo.VideoViewBuffer;

public class DyUtil {
	public static String PATH;
	public static String playUrl;
	public static ArrayList<Urldata> urls;
	public static List<Video> videos;

	public static void init() {

		File dir = new File(PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, "urls.obj");
		if (file.exists()) {

			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new FileInputStream(file));
				urls = (ArrayList<Urldata>) in.readObject();

				in.close();

				/*
				 * for (Urldata urldata : urls) {
				 * System.out.println(urldata.getMsg());
				 * System.out.println(urldata.getUrl()); }
				 */

			} catch (Exception e) {
				e.printStackTrace();
				try {
					file.delete();
				} catch (Exception e2) {
					// TODO: handle exception
				}

			}
		}
		if (urls == null) {
			urls = new ArrayList<Urldata>();
		}
	}

	public static String getHtml(String getUrl, int outtime, String charsetName) {
		String html = "";
		URL url;
		try {
			url = new URL(getUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection
					.setRequestProperty("User-Agent",
							"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; CIBA)");
			// connection.setRequestProperty("Connection", "Keep-Alive");
			// connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setConnectTimeout(outtime);
			connection.connect();
			InputStream inStrm = connection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					inStrm, charsetName));
			String temp = "";

			while ((temp = br.readLine()) != null) {
				html = html + (temp + '\n');
			}
			try {
				br.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				connection.disconnect();
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return html;
	}

	public static void save() {

		File dir = new File(PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, "urls.obj");

		try {

			file.createNewFile();
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file));
			out.writeObject(urls);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Urldata readUrl(String thunder) {
		// init();
		if (urls == null) {
			init();
		}
		for (Urldata data : urls) {

			if (data.getMsg().equals(thunder)) {
				Urldata temp = new Urldata();
				temp.setMsg("");
				temp.setUrl(data.getUrl());
				return temp;
			}
		}
		return new Urldata();
	}

	public static List<String> readFiles(String http, String path) {
		// init();
		PATH = path;
		List<String> list = new ArrayList<String>();
		if (urls == null) {
			init();
		}
		for (Urldata data : urls) {

			if (data.getMsg().equals(http)) {

				for (String string : data.getUrl().split("-")) {
					if (!AbStrUtil.isEmpty(string)) {
						list.add(string);
					}
				}
				// temp.setUrl(data.getUrl());
				return list;
			}
		}
		return list;
	}

	public static Urldata getDyPlayUrl(final String thunder) {
		Urldata playUrl = readUrl(thunder);
		if (!AbStrUtil.isEmpty(playUrl.getUrl())) {
			return playUrl;
		}
		try {
			String temp = URLEncoder.encode(thunder, "UTF-8");
			temp = URLEncoder.encode(temp, "UTF-8");
			String url = "http://vod.dychao.com/i/geturl?callback=jQuery1_2&url="
					+ temp + "&_=" + System.currentTimeMillis();

			// Document doc = Jsoup.connect(url).get();
			String text = getHtml(url, 10000, "utf-8");
			if (!AbStrUtil.isEmpty(text) && text.contains("jQuery1_2")) {
				// System.out.println(text);
				text = text.trim();
				text = text.substring(10, text.length() - 1);
				// System.out.println(text);
				Gson gson = new Gson();
				Urldata data = gson.fromJson(text, Urldata.class);
				playUrl = data;

				if (!AbStrUtil.isEmpty(playUrl.getUrl())) {
					data = new Urldata();

					data.setMsg(thunder);
					data.setUrl(playUrl.getUrl());
					urls.add(data);
					save();
				}
			} else {
				playUrl.setMsg("获取失败!");
			}
			// System.out.println(text);
			// System.out.println(allList.size());
		} catch (Exception e) {
			playUrl.setMsg("获取失败!");
			e.printStackTrace();
		}
		return playUrl;

	}

	public static void getPlayUrl(final String thunder,
			final PlayUrlActivity packageContext) {
		AbDialogUtil.showProgressDialog(packageContext,
				R.drawable.progress_circular, "正在获取播放链接...");
		AbTask mAbTask = new AbTask();
		final AbTaskItem item = new AbTaskItem();
		item.setListener(new AbTaskObjectListener() {

			@Override
			public void update(Object obj) {
				// TODO Auto-generated method stub
				if (packageContext.isBack)
					return;
				AbDialogUtil.removeDialog(packageContext);
				Urldata data = (Urldata) obj;
				if (!AbStrUtil.isEmpty(data.getMsg())) {
					// System.out.println(playUrl);
					String msg = data.getMsg();
					if (msg.contains("downloading")) {
						msg = "资源正在下载中";
					}
					if (msg.equals("url null")) {
						msg = "链接错误或者版权原因无法播放";
					}
					AbToastUtil.showToast(packageContext, msg);
				}
				if (!AbStrUtil.isEmpty(data.getUrl())) {
					String playurl = data.getUrl();
					// AbToastUtil.showToast(SearchActivity.this, playurl);
					DyUtil.playUrl = playurl.replace("\\/", "/");
					;
					// Bundle bundle = new Bundle();
					// bundle.putString("playurl", playurl);
					Intent intent = new Intent(packageContext,
							SelectPlayerActivity.class);

					packageContext.startActivity(intent);
				} else {
					// AbToastUtil.showToast(SearchActivity.this, "无法播放！");
				}
			}

			@Override
			public Urldata getObject() {
				// TODO Auto-generated method stub
				PATH = packageContext.getFilesDir() + "/";
				return DyUtil.getDyPlayUrl(thunder);
			}
		});

		mAbTask.execute(item);
	}

	public static Dyres getDyres(String wd) {
		try {
			// ArrayList<String>keys=new ArrayList<String>();
			String url = "http://www.dychao.com/movie/api.php?ap=res&callback=back&wd="
					+ URLEncoder.encode(wd, "UTF-8");

			Document doc = Jsoup.connect(url).get();
			String text = doc.text();
			if (!AbStrUtil.isEmpty(text) && text.contains("back")) {
				text = text.trim().substring(5, text.length() - 2);
				Gson gson = new Gson();
				Dyres dy = gson.fromJson(text, Dyres.class);
				return dy;
			}
			System.out.println(text);
			// System.out.println(allList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Dydata getDydata(int page, String wd) {
		try {
			// ArrayList<String>keys=new ArrayList<String>();
			String url = "http://vod.dychao.com/search?p=" + page
					+ "&sort=8&wd=" + URLEncoder.encode(wd, "UTF-8")
					+ "&ie=utf-8&callback=jQuery1_2&_="
					+ System.currentTimeMillis();

			Document doc = Jsoup.connect(url).get();
			String text = doc.text();
			if (!AbStrUtil.isEmpty(text) && text.contains("jQuery1_2")) {
				text = text.trim().substring(10, text.length() - 1);
				Gson gson = new Gson();
				Dydata dy = gson.fromJson(text, Dydata.class);
				return dy;
			}
			// System.out.println(text);
			// System.out.println(allList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static PageContent getPageContent(int page) {
		try {
			String url = "http://www.dychao.com/movie/api.php?callback=jQuery1_2&ap=list&type=all&area=all&year=all&order=1&complete=&page="
					+ page + "&_=" + System.currentTimeMillis();

			Document doc = Jsoup.connect(url).get();
			String text = doc.text();
			text = u2s(text);
			if (!AbStrUtil.isEmpty(text) && text.contains("jQuery1_2")) {
				text = text.trim().substring(10, text.length() - 2);
				Gson gson = new Gson();
				PageContent data = gson.fromJson(text, PageContent.class);
				return data;
			}
			// System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String u2s(String u) {
		Pattern pat = Pattern.compile("[\\\\U|\\\\u]([0-9a-fA-F]{4})");
		Matcher mat = pat.matcher(u);
		while (mat.find()) {
			String HEX = mat.group(1);
			char v = (char) (Integer.parseInt(HEX.toUpperCase(), 16));
			mat = pat.matcher(u = u.replace("\\" + mat.group(), "" + v));
		}
		return u;
	}

	public static Video showVideo;
	public static Node nodePlay;
	public static String urlToPlay;

	public static void thunderPlay(final String thunder,
			final PlayUrlActivity packageContext) {
		String url = thunder;
		if (thunder.startsWith("ftp://") || thunder.startsWith("http://")) {
			url = enThunder(url);
		}
		getPlayUrl(url, packageContext);
	}

	public static String enThunder(String url) {
		url = "AA" + url + "ZZ";
		try {
			byte[] bytes = url.getBytes("GBK");
			String temp = "" + new String(Base64.encode(bytes, Base64.DEFAULT));
			url = "";
			char[] base64EncodeChars = new char[] { 'A', 'B', 'C', 'D', 'E',
					'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
					'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
					'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
					'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0',
					'1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', '=' };
			for (int i = 0; i < temp.length(); i++)
				for (int j = 0; j < base64EncodeChars.length; j++) {
					if ((int) temp.charAt(i) == (int) base64EncodeChars[j]) {
						url = url + temp.charAt(i);
						break;
					}
				}
			url = "thunder://" + url;
			// System.out.println(key);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	public static void qqxfPlay(final String url,
			final PlayUrlActivity packageContext) {
		// TODO Auto-generated method stub
		PATH = packageContext.getFilesDir() + "/";
		AbDialogUtil.showProgressDialog(packageContext,
				R.drawable.progress_circular, "正在获取播放链接...");
		AbTask mAbTask = new AbTask();
		final AbTaskItem item = new AbTaskItem();
		item.setListener(new AbTaskObjectListener() {

			@Override
			public void update(Object obj) {
				// TODO Auto-generated method stub
				if (packageContext.isBack)
					return;
				AbDialogUtil.removeDialog(packageContext);
				Urldata data = (Urldata) obj;
				if (!"ok".equals(data.getMsg())) {
					// System.out.println(playUrl);
					String msg = data.getMsg();
					if (msg.contains("downloading")) {
						msg = "资源正在下载中";
					}
					if (msg.equals("url null")) {
						msg = "链接错误或者版权原因无法播放";
					}
					AbToastUtil.showToast(packageContext, msg);
					((TextView) packageContext.findViewById(R.id.textView3))
							.setText(msg);
				}
				if (!AbStrUtil.isEmpty(data.getUrl())) {
					String playurl = data.getUrl();
					// AbToastUtil.showToast(SearchActivity.this, playurl);
					DyUtil.playUrl = playurl.replace("\\/", "/");
					// Bundle bundle = new Bundle();
					// bundle.putString("playurl", playurl);
					Intent intent = new Intent(packageContext,
							SelectPlayerActivity.class);

					packageContext.startActivity(intent);
				} else {
					// AbToastUtil.showToast(SearchActivity.this, "无法播放！");
				}
			}

			@Override
			public Urldata getObject() {
				// TODO Auto-generated method stub
				Urldata urldata = DyUtil.getQQxfPlayUrl(url, packageContext);
				if (packageContext.isBack)
					return null;

				if (!"ok".equals(urldata.getMsg()))
					urldata = getPlayurl(url);
				return urldata;
			}
		});

		mAbTask.execute(item);
	}

	static WebView mWebView = null;
	public static Handler myHandler = null;

	protected static Urldata getQQxfPlayUrl(String url,
			PlayUrlActivity packageContext) {		
		final String ori = url;
		PATH = packageContext.getFilesDir() + "/";
		Urldata urldata = readUrl(url);
		if (!AbStrUtil.isEmpty(urldata.getUrl())) {
			urldata.setMsg("ok");
			return urldata;
		}
		urldata.setMsg("获取失败");
		String temp;
		try {

			String text;
			String method;
			Pattern pat;
			Matcher mat;
			// if (AbStrUtil.isEmpty(valueString)) {
			String apiurl = "";
			try {
				Document doc = Jsoup.connect(
						"http://www.dayunbo.com/api/api.php").get();
				apiurl = doc.getElementById("frompost").attr("action");
				System.out.println(apiurl);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				urldata.setMsg("连接服务器失败，请检查网络或者稍后重试。");
				return urldata;
			}
			// text =
			// getHtml("http://api.dayunbo.com:8090/vod/api_2.php?method=high&u=",5000,"utf-8");
			if (AbStrUtil.isEmpty(apiurl)) {
				urldata.setMsg("获取api服务器失败，请检查网络或者稍后重试。");
				return urldata;
			}
			if (packageContext.isBack)
				return null;
			/*
			 * method = getMethod(text, packageContext); if
			 * (AbStrUtil.isEmpty(method)) { method = "&method3311080="; pat =
			 * Pattern .compile("method \\+= \"ri_ni_jia_([\\w_]+)\";"); mat =
			 * pat.matcher(text); if (mat.find()) { method += mat.group(1);
			 * 
			 * } else { urldata.setMsg("匹配不到获取方法"); return urldata; } }
			 */

			// } else method = valueString;
			// temp = URLEncoder.encode(url, "UTF-8").replace(" ", "+") +
			// method;
			// text = getHtml("http://api.dayunbo.com:8090/vod/playapi2.php?u="+
			// temp,3000,"utf-8");
			// temp=URLEncoder.encode( temp, "UTF-8");
			text = "";
			try {
				Document doc = Jsoup.connect(apiurl).timeout(30000)
						.data("u", url).post();
				text = doc.html();
				try {
					String nameStr=doc.select("div em").first().text();
					if (!nameStr.contains("不影响播放")
						&& AbStrUtil.isEmpty(HistoryUtil.data.getName())) {
						HistoryUtil.data.setName(nameStr);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				urldata.setMsg("获取hash值失败，请检查网络或者稍后重试。");
				return urldata;
			}

			if (AbStrUtil.isEmpty(text)) {
				// System.out.println(temp);
				urldata.setMsg("该视频暂时无法播放！");
				return urldata;
			}

			// http://api.dayunbo.com:8090/vod/api_2.php?method=high&u=
			// System.out.println(text);
			System.out.println(text);
			if (text.contains("<font color=\"red\"><b>影片转码中！请更换链接或稍后重试！</b>")) {
				urldata.setMsg("影片转码中！请更换链接或稍后重试！");
				return urldata;
			}
			if (text.contains("<font color=\"red\"><b>暂无种子信息！请更换链接或稍后重试！</b></font>")) {
				urldata.setMsg("暂无种子信息！请更换链接或稍后重试");
				return urldata;
			}
			pat = Pattern.compile("tPlayHash = '(\\w+)';");
			mat = pat.matcher(text);
			if (mat.find()) {
				String HEX = mat.group(1);
				// valueString = method;
				String back = "jsonp" + System.currentTimeMillis()
						+ (1000 + new Random().nextInt(8999));
				// http://api.dayunbo.com:8090/vod/get_playurl.php?type=html5&callback=jsonp&url=
				apiurl = apiurl.substring(0, apiurl.lastIndexOf("/"));
				System.out.println(apiurl);
				url = apiurl + "/get_playurl_api.php?type=html5&callback="
						+ back + "&url=" + HEX;
				// http://api.dayunbo.com:8092/vod/get_playurl_api.php?type=flash&url=&time=
				final String flashurl = apiurl
						+ "/get_playurl_api.php?type=flash&url=" + HEX
						+ "&time=" + System.currentTimeMillis();

				// System.out.println(url);
				text = getHtml(url, 3000, "utf-8");
				// System.out.println(text);
				// Document doc = Jsoup.connect(url).get();
				if (!AbStrUtil.isEmpty(text) && text.contains(back)) {

					text = text.trim();
					text = text.substring(back.length() + 1, text.length() - 1);
					System.out.println(text);
					if (text.contains("{\"msg\":\"url null\"}")) {
						urldata.setMsg("暂无播放地址，请换个链接试试");
						return urldata;
					}
					Gson gson = new Gson();
					final Urldata data = gson.fromJson(text, Urldata.class);
					// /playUrl=data;

					if ("ok".equals(data.getMsg())
							&& !AbStrUtil.isEmpty(data.getUrl())) {
						getflash(flashurl, data.getUrl());
						//new Thread(new Runnable() {
						//	@Override
						//	public void run() {
						//		// TODO Auto-generated method stub								
						//	} 
						//}).start();/**/
						Urldata xxx = new Urldata();

						xxx.setMsg(ori);
						xxx.setUrl(data.getUrl());
						urls.add(xxx);
						save();

						return data;

					}

				} else
					urldata.setMsg("视频暂时无法播放");
			} else
				urldata.setMsg("链接错误");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urldata;
	}
	
	public static void getflash(final String flashurl,
			final String orgurl) {
		try {
			String s = "";
			Document doc = Jsoup.connect(flashurl)
					.timeout(30000).get();
			Elements files = doc.select("file");
			for (Element element : files) {
				String t = element.text()
						.replace("<![CDATA[", "")
						.replace("]]>", "");
				DyUtil.fileList.add(t);
				s = s + t + '-';
			}
			if (DyUtil.fileList.size() > 0) {
				System.out.println(s);
				Urldata x = new Urldata();
				x.setMsg(orgurl);
				x.setUrl(s);
				urls.add(x);
				save();
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(flashurl);
			e.printStackTrace();
		}
	}

	// private static String htmlData;
	private static String valueString;
	public static List<String> fileList = new ArrayList<String>();

	// private static valueString;

	private static String getMethod(final String htmlData,
			final Context packageContext) {
		// TODO Auto-generated method stub
		if (mWebView == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					myHandler.post(new Runnable() {
						@Override
						public void run() {
							mWebView = new WebView(packageContext);
							mWebView.getSettings().setJavaScriptEnabled(true);
							mWebView.getSettings().setDefaultTextEncodingName(
									"UTF -8");
							mWebView.setWebViewClient(new WebViewClient() {
								public boolean shouldOverrideUrlLoading(
										WebView view, String url) {
									// view.loadUrl(url);
									if (url.startsWith("http://api.dayunbo.com:8090/vod/playapi2.php")) {
										System.out.println(url);
										if (url.contains("&")) {
											valueString = url.substring(url
													.indexOf("&"));
										}
									}
									return true;
								}
							});
						}
					});
				}
			}).start();
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				myHandler.post(new Runnable() {
					public void run() {
						mWebView.loadData(htmlData, "text/html; charset=UTF-8",
								null);
					}
				});
			}
		}).start();
		int c = 0;
		while (AbStrUtil.isEmpty(valueString) && c < 2) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			c++;
		}
		return valueString;
	}

	public static String changeED2K(String url) {
		// TODO Auto-generated method stub
		Pattern pat = Pattern
				.compile("ed2k:\\/\\/\\|file\\|([^\\|]+?)\\|(\\d+)\\|([A-Z0-9]{32})\\|(h=[A-Z0-9]{32}\\|)?\\/?");
		Matcher mat = pat.matcher(url);
		// System.out.println(url);
		if (mat.find()) {
			String name = "" + mat.group(1);

			if (name.contains(".")) {

				String temp = "1" + name.substring(name.lastIndexOf("."));
				url = url.replace(name, temp);
				// System.out.println(url);
			}
		}
		if (url.startsWith("magnet:?xt=urn:btih:") && url.contains("&")) {
			url = url.substring(0, url.indexOf("&")).toLowerCase();
		}
		return url;
	}

	private static Urldata getPlayurl(String url) {
		String apiurl = "";
		String text = "";
		Urldata data = new Urldata();
		data.setMsg("该视频暂时无法播放！获取API失败");
		try {
			Document doc = Jsoup.connect("http://www.huoyan.tv/api.php").get();
			apiurl = "http://www.huoyan.tv/"
					+ doc.getElementById("frompost").attr("action");
			// System.out.println(apiurl);
			// System.out.println(apiurl.substring(0, apiurl.lastIndexOf("/"))
			// );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Map<String, String> datas = new HashMap<String, String>();
			datas.put("u", url);
			datas.put("class", "api");
			Document doc = Jsoup.connect(apiurl).timeout(30000)
					.referrer("http://www.huoyan.tv/api.php").data(datas)
					.userAgent(userAgent).followRedirects(true).post();
			text = doc.html();
			// System.out.println(text);
			try {
				String name = doc.select(".left").first().text();
				if (!name.contains("不影响播放")
						&& AbStrUtil.isEmpty(HistoryUtil.data.getName())) {
					HistoryUtil.data.setName(name);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			Pattern pat = Pattern.compile("f:'(\\S+)',");
			Matcher mat = pat.matcher(text);
			if (mat.find()) {
				String HEX = "http://www.huoyan.tv/" + mat.group(1);
				System.out.println(HEX);
				String Location = null;
				try {
					Location = Jsoup
							.connect(HEX)
							.timeout(30000)
							.referrer(apiurl)
							.userAgent(userAgent)
							.header("Accept",
									"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
							.followRedirects(false).execute()
							.header("Location");
					System.out.println(Location);
				} catch (Exception e) {
					// TODO: handle exception
				}
				if (AbStrUtil.isEmpty(Location)) {
					Location = Jsoup
							.connect(HEX)
							.timeout(30000)
							.referrer(apiurl)
							.userAgent(userAgent)
							.header("Accept",
									"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
							.followRedirects(false).execute()
							.header("Location");
					System.out.println(Location);
				}
				if (!AbStrUtil.isEmpty(Location)) {

					data.setMsg("ok");
					data.setUrl(Location);
					Urldata x = new Urldata();
					x.setMsg(url);
					x.setUrl(Location);
					urls.add(x);
					save();
				}
			} else {
				String err = doc.select(".errinfo").first().text()
						.replace("可按[F5]刷新几次试试；如果依旧不能播放，请过3、5个小时后再试", "");
				System.out.println(err);
				System.out.println(url);
				data.setMsg(err);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			data.setMsg("该视频暂时无法播放！获取播放数据失败");
		}
		return data;
	}

	private static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";

	public static List<Node> getp2psearch(String wd) {
		// TODO Auto-generated method stub
		List<Node> all=new ArrayList<Node>();
		try {
			
			String getUrl = "http://api.p2psearchers.com/?ap=dht&wd=" + URLEncoder.encode(wd, "UTF-8") ;
			String html = "";
			URL url = new URL(getUrl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection(); 
				connection.setRequestProperty("Connection", "Keep-Alive");
				// connection.setRequestProperty("Cache-Control", "no-cache");
				 
				connection.connect();
				InputStream inStrm = connection.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						inStrm, "UTF-8"));
				String temp = "";

				while ((temp = br.readLine()) != null) {
					html = html + (temp + '\n');
				}
			Document doc = Jsoup.parse(html);
			 
			for (Element ele : doc.select("item")) {
				 Node node=new Node();
				 node.setTitle(ele.select("title").first().text());
				 node.setUrl(ele.select("url").first().text());
				 all.add(node);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return all;
	}
	
	public static void toGetPlayUrl(  String urlT,
			final PlayUrlActivity packageContext) {
		// TODO Auto-generated method stub
		DyUtil.fileList=new ArrayList<String>();
		PATH = packageContext.getFilesDir() + "/";
		if (urlT.startsWith("ftp://") || urlT.startsWith("http://")) {
			urlT = enThunder(urlT);
		}
		final String url=urlT;
		AbDialogUtil.showProgressDialog(packageContext,
				R.drawable.progress_circular, "正在获取播放链接...");
		AbTask mAbTask = new AbTask();
		final AbTaskItem item = new AbTaskItem();
		item.setListener(new AbTaskObjectListener() {

			@Override
			public void update(Object obj) {
				// TODO Auto-generated method stub
				if (packageContext.isBack)
					return;
				AbDialogUtil.removeDialog(packageContext);
				Urldata data = (Urldata) obj;
				if (!"ok".equals(data.getMsg())) {
					// System.out.println(playUrl);
					String msg = data.getMsg();
					if (msg.contains("downloading")) {
						msg = "资源正在下载中";
					}
					if (msg.equals("url null")) {
						msg = "链接错误或者版权原因无法播放";
					}
					AbToastUtil.showToast(packageContext, msg);
					((TextView) packageContext.findViewById(R.id.textView3))
							.setText(msg);
				}
				if (!AbStrUtil.isEmpty(data.getUrl())) {
					String playurl = data.getUrl();
					// AbToastUtil.showToast(SearchActivity.this, playurl);
					DyUtil.playUrl = playurl.replace("\\/", "/");
					// Bundle bundle = new Bundle();
					// bundle.putString("playurl", playurl);
					Intent intent = new Intent(packageContext,
							SelectPlayerActivity.class);

					packageContext.startActivity(intent);
				} else {
					// AbToastUtil.showToast(SearchActivity.this, "无法播放！");
				}
			}

			@Override
			public Urldata getObject() {
				// TODO Auto-generated method stub
				Urldata urldata =DyUtil.getDyPlayUrl(url);
				if (packageContext.isBack)
					return null;
				if (AbStrUtil.isEmpty(urldata.getMsg())) {
					urldata.setMsg("ok");
					return urldata;
				}
				  urldata = DyUtil.getQQxfPlayUrl(url, packageContext);
				if (packageContext.isBack)
					return null;

				if (!"ok".equals(urldata.getMsg()))
					urldata = getPlayurl(url);
				return urldata;
			}
		});

		mAbTask.execute(item);
	}
}
