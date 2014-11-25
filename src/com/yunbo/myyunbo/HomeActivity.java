package com.yunbo.myyunbo;

import java.util.ArrayList;
import java.util.List;

import com.ab.activity.AbActivity;  
import com.ab.image.AbImageLoader;
import com.ab.task.AbTask;
import com.ab.task.AbTaskItem;
import com.ab.task.AbTaskListListener;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbImageUtil;
import com.ab.util.AbToastUtil;
import com.ab.util.AbViewHolder;  
import com.yunbo.control.DyUtil;
import com.yunbo.mode.Dydata;
import com.yunbo.mode.Movie;
import com.yunbo.mode.Node;
import com.yunbo.mode.PageContent;
import com.yunbo.mode.Video; 

import android.content.Context;
import android.content.Intent;
import android.os.Bundle; 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter; 
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends AbActivity {

		private ListView mListView = null;
	    private LayoutInflater mInflater;
	    private ArrayList<Video>allList=new ArrayList<Video>();  
			
	    private	BaseAdapter myListViewAdapter;
	    private ImageButton moreButton ;
	    //图片下载器
	    private AbImageLoader mAbImageDownloader = null;
	    private int page=0;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_home);

		mListView = (ListView) this.findViewById(R.id.listView1);
		mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		moreButton = (ImageButton)this.findViewById(R.id.imageButton1);

        mAbImageDownloader = new AbImageLoader(this); 
        mAbImageDownloader.setMaxWidth(180);
        mAbImageDownloader.setMaxHeight(240); 
        mAbImageDownloader.setLoadingImage(R.drawable.image_loading);
		mAbImageDownloader.setEmptyImage(R.drawable.image_empty); 
        mAbImageDownloader.setErrorImage(R.drawable.image_error); 
/**/
		mListView.setAdapter(myListViewAdapter=new BaseAdapter() {
			 
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub

		          if(convertView == null){
			           //使用自定义的list_items作为Layout
			           convertView = mInflater.inflate(R.layout.item_list, parent, false);
		          }
		          ImageView itemsIcon =(ImageView) convertView.findViewById(R.id.itemsIcon); 
		          TextView itemsTitle =(TextView) convertView.findViewById(R.id.itemsTitle);
		          TextView itemsChildTitle =(TextView) convertView.findViewById(R.id.itemsChildTitle);
		          TextView itemsText =(TextView) convertView.findViewById(R.id.itemsText);
		          
				  //获取该行的数据
		          String imageUrl = getItem(position).getImg().replace("\\/", "/");
		          itemsTitle.setText(getItem(position).getTitle());
		          String arter="";
		          for (int i = 0; i < getItem(position).getActor().size(); i++) {
		        	  arter=arter+" "+getItem(position).getActor().get(i).getName();
				}
		          itemsChildTitle.setText(arter);
		          itemsText.setText(getItem(position).getIntro());
		          //设置加载中的View
		          mAbImageDownloader.setLoadingView(convertView.findViewById(R.id.progressBar));
		          //图片的下载
		          mAbImageDownloader.display(itemsIcon,imageUrl);
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}
			
			@Override
			public Video getItem(int position) {
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
				String key= allList.get(position).getTitle();
				if (key.contains("(")) {
					key=key.substring(0,key.indexOf("("));
				}
				if (key.contains("（")) {
					key=key.substring(0,key.indexOf("（"));
				}
				key=key.replace("大结局", "");
				DyUtil.showVideo=allList.get(position);
				Bundle bundle = new Bundle();
				bundle.putString("key",key);
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, SearchActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);} 
		});
		moreButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				moreLoading();
			}

		});
( findViewById(R.id.imageView1)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, LanunyActivity.class); 
				startActivity(intent);
			}
		});if (DyUtil.videos==null) {
			
		 moreLoading();
		}else {page++;
			allList.addAll(DyUtil.videos);
		 myListViewAdapter.notifyDataSetChanged(); 
		}
	}
 
			private void moreLoading() {
				page++;moreButton.setVisibility(View.GONE);
				AbDialogUtil.showProgressDialog(this,R.drawable.progress_circular, "正在获取电影列表...");
				AbTask mAbTask = new AbTask();
				final AbTaskItem item = new AbTaskItem();
				item.setListener(new AbTaskListListener() {

					@Override
					public List<?> getList() {
						// TODO Auto-generated method stub
						PageContent data=DyUtil.getPageContent(page);
						 if (data!=null) { 
							return data.getVideos();
						}
						return null;
					}

					@Override
					public void update(List<?> paramList) {
						// TODO Auto-generated method stub 
						moreButton.setVisibility(View.VISIBLE);
						AbDialogUtil.removeDialog(HomeActivity.this);
						if (paramList==null) {
							page--;
							return;
						}
						if (paramList.size()==0) {
							AbToastUtil.showToast(HomeActivity.this, "查询结果为空！");
							return;
						} 
						List<Video> list =(List<Video>) paramList;
						 allList.addAll(list ); 
						for (int i = 0; i < list.size(); i++) {
									//getPlayUrl(dy.getData().getNodes().get(i));
								}
						 myListViewAdapter.notifyDataSetChanged();  
					}
				});
				mAbTask.execute(item);
				
			}

}
