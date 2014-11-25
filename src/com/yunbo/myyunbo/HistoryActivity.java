package com.yunbo.myyunbo;

import java.util.ArrayList;

import com.ab.activity.AbActivity; 
import com.ab.util.AbViewHolder;
import com.yunbo.control.DyUtil;
import com.yunbo.control.HistoryUtil;
import com.yunbo.mode.History;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener; 
import android.content.Context;
import android.content.Intent;

public class HistoryActivity extends AbActivity {

	private ListView mListView;
	private LayoutInflater mInflater;
	private BaseAdapter myListViewAdapter;

	private ArrayList<History> allList = new ArrayList<History>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_history);
		mInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mListView = (ListView) this.findViewById(R.id.listView1);
		mListView.setAdapter(myListViewAdapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub

				if (convertView == null) {
					// 使用自定义的list_items作为Layout
					convertView = mInflater.inflate(R.layout.his_item, parent,
							false);
				}
				TextView itemsText = AbViewHolder
						.get(convertView, R.id.textView1);
				String name=allList.get(position).getName();
				if (name.startsWith("[")&&!name.endsWith("]")&&name.contains("]")) {
					try {
						name=name.substring(name.indexOf("]")+1);
					} catch (Exception e) {
						// TODO: handle exception
						name=name.substring(name.indexOf("]"));
					}					
				}
				if (name.startsWith("【")&&!name.endsWith("】")&&name.contains("】")) {
					try {
						name=name.substring(name.indexOf("】")+1);
					} catch (Exception e) {
						// TODO: handle exception
						name=name.substring(name.indexOf("】 "));
					}					
				}
				if (name.startsWith("/")&&name.length()>1) {
					name=name.substring(1);
				}
				itemsText.setText(name);

				TextView itemsTextsub = AbViewHolder
						.get(convertView, R.id.textView2);
				itemsTextsub.setText(allList.get(position).getDate()
						+" 播放至 "+HistoryUtil.GetTime(allList.get(position).getSeek()));
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
				HistoryUtil.data=new History();
				HistoryUtil.data.setName(allList.get(position).getName());
				DyUtil.playUrl=allList.get(position).getUrl();
				HistoryUtil.seek=allList.get(position).getSeek();
				Intent intent = new Intent(HistoryActivity.this,
						VideoViewBuffer.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.textView1)
		.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		allList= new ArrayList<History>();
		for (int i = HistoryUtil.allList.size()-1; i >=0; i--) {
			allList.add(HistoryUtil.allList.get(i));
		}
		myListViewAdapter.notifyDataSetChanged();
		
	}

}
