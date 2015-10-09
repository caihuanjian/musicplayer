package com.example.musicadpter;

import java.util.List;

import com.example.mymusicplayer.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * 自定义菜单适配器
 * @author Administrator
 *
 */
public class MenuAdapter extends BaseAdapter{
	private List<int[]> data;
	private Context context;
	private ItemListener mItemListener;
	
	
	public MenuAdapter(Context context, List<int[]> data) {
		this.context = context;
		this.data = data;
	}
	
	
	public MenuAdapter setmItemListener(ItemListener mItemListener) {
		this.mItemListener = mItemListener;         //将外部实现了的接口传递进来
		return this;
	}


	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.menu_item_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.btn_menu = (Button) convertView.findViewById(R.id.btn_menu);
			viewHolder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}
		final int[] d= data.get(position);
		viewHolder.btn_menu.setBackgroundResource(d[0]);//1为图片，2为文本
		viewHolder.btn_menu.setFocusable(false);//不可获得焦点
		viewHolder.btn_menu.setFocusableInTouchMode(false);
		viewHolder.tv_title.setText(d[1]);
		viewHolder.tv_title.setTextSize(12);
		viewHolder.tv_title.setTextColor(Color.WHITE);
		final View t_View = convertView;
		viewHolder.btn_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mItemListener != null) {
					mItemListener.onClickListener(position, t_View);//传递点击的position和点击的gridview的item
				}                                    //如何执行这个接口的方法？就是传递一个外部实现了的接口进来
			}
		});
		
		return convertView;
	}
	
	public class ViewHolder {
		public Button btn_menu;
		public TextView tv_title;
	}
	
	public interface ItemListener{
		public void onClickListener(int position,View view);
	}
}
