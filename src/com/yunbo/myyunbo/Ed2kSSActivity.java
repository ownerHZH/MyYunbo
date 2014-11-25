package com.yunbo.myyunbo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ab.activity.AbActivity;
import com.ab.task.AbTask;
import com.ab.task.AbTaskItem;
import com.ab.task.AbTaskObjectListener;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbStrUtil;
import com.ab.util.AbToastUtil;
import com.ab.util.AbViewHolder;
import com.yunbo.control.DyUtil;
import com.yunbo.mode.Node;
import com.yunbo.mode.Urldata;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Ed2kSSActivity extends AbActivity implements OnClickListener {

	EditText word;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_ed2k_ss);
		word=(EditText)findViewById(R.id.editText1);
		String[] ss=new String[]{"ZEX-051","zizg-001","yrz-069","XV-851","VSPDS-574","TSDV-41458","TERA-005","SW-116"
				,"star-526","SW-047","STAR-418","STAR-362","star-297","star 409"
				,"SQTE-046","SNIS-258","SNIS-031","SNIS-012","SNIS012","RBD518","PGD-660","PGD-268","ODFW-006","n0753","mild-771"
				,"MIDE-008","MIAD-635","Miad 576","MDYD-789","JUC-510","JUC-375","jbs-006","ipz-015","IPTD-920","IPTD-651","IPTD-343"
				,"ibw248","GUILD-009","EKDV-273","dgl 008","BOD-277","ABP142","ABP-069","DVDES-662","iptd-788","ABP-090"
				,"ADN-021","ABS-147","AKB48","BBAN-008","BBI-142","CWM-087","DKDN-008","DPMI-001","DV-1575"};
		word.setText(ss[new Random().nextInt(ss.length)]);
		findViewById(R.id.button1).setOnClickListener(this);
		mListView = (ListView) this.findViewById(R.id.listView1);
		mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView.setAdapter(myListViewAdapter=new BaseAdapter() {
			 
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub

		          if(convertView == null){
			           //使用自定义的list_items作为Layout
			           convertView = mInflater.inflate(R.layout.sear_item, parent, false);
		          }
		          TextView itemsText = AbViewHolder.get(convertView,R.id.tx_item );
		          itemsText.setText(allList.get(position).split("\\|")[2]);
		         // itemsText.setTextColor(R.color.dodgerblue);
		         // itemsText.setTextColor(Color.parseColor("#1E90FF"));
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
				DyUtil.urlToPlay=allList.get(position);
				//DyUtil. getPlayUrl(allList.get(position).getThunder(),SearchActivity.this);
				//AbToastUtil.showToast(SearchActivity.this, playList.get(position));
				  Intent intent = new Intent(Ed2kSSActivity.this,PlayUrlActivity.class);  
				 startActivity(intent); 
			}
		});
		word.selectAll();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String wd=word.getText().toString().replace("-", " ").trim();
		try {
			wd=URLEncoder.encode(wd, "UTF-8").replace(" ", "+");
		} catch (Exception e) {
			// TODO: handle exception
		}
		final String url="http://www.yunbosou.com/ed2k/sou?wd="+wd;
		AbDialogUtil.showProgressDialog(this,R.drawable.progress_circular, "正在获取播放链接...");
		AbTask mAbTask = new AbTask();
		final AbTaskItem item = new AbTaskItem();
		item.setListener(new AbTaskObjectListener() {
			
			@Override
			public  void update(Object obj) {
				// TODO Auto-generated method stub
				AbDialogUtil.removeDialog(Ed2kSSActivity.this);
				 
				if (allList.size()==0) {
					AbToastUtil.showToast(abApplication, "查询结果为空");
				}else
				 myListViewAdapter.notifyDataSetChanged();
			}
			
			@Override
			public Object getObject() {
				// TODO Auto-generated method stub
				 
				try {
					Document doc = Jsoup.connect(url).
							userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 BIDUBrowser/7.0 Safari/537.36").
							timeout(10000).get();

					Elements as = doc.select("a") ;
					allList.clear();
					for (Element element : as) {
						String href=(element.attr("href")+"").trim();
						if (href.startsWith("ed2k://|file|")) {
							
						allList.add(href);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});

		mAbTask.execute(item);closeInputMethod() ;
	}
    private ArrayList<String>allList=new ArrayList<String>(); private ListView mListView = null;
    private LayoutInflater mInflater;
    private	BaseAdapter myListViewAdapter;

	private void closeInputMethod() {
	    InputMethodManager imm = (InputMethodManager) abApplication.getSystemService(Context.INPUT_METHOD_SERVICE);
	    boolean isOpen = imm.isActive();
	    if (isOpen) {
	        // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
	        imm.hideSoftInputFromWindow(word.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
}
