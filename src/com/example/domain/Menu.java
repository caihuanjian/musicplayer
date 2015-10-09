package com.example.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.musicadpter.MenuAdapter;
import com.example.mymusicplayer.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 自定义菜单
 * 
 * @author hj
 * 
 */
public class Menu {
	private Context context;
	private List<GridView> contents; // 标签GridView
	private List<List<int[]>> datas; // 0:图标，1:标题
	private List<TextView> tabs; // 标签标题
	private int index = 0; // 显示标签索引
	private LinearLayout layout;
	private PopupWindow popwindow;

	public Menu(Context context) {
		this.context = context;
		tabs = new ArrayList<TextView>();
		contents = new ArrayList<GridView>();
		datas = new ArrayList<List<int[]>>();

		layout = new LinearLayout(context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.setGravity(Gravity.CLIP_HORIZONTAL);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundResource(R.color.menu_bg_focus);
	}

	public void addItem(String oftenuse, List<int[]> data,
			MenuAdapter.ItemListener listener) {
		tabs.add(createTextView(oftenuse)); // 添加标题,添加一个textview
		datas.add(data); // 一个data装了一个gridview的内容
		contents.add(createGridView(data, listener));// 添加一个生成的gridview
	}

	private TextView createTextView(String title) {
		TextView textView = new TextView(context);
		textView.setText(title);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(0, 10, 0, 10);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(15);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
				LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		textView.setLayoutParams(params);
		textView.setOnClickListener(clickListener);
		return textView;
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int i = Integer.valueOf(v.getTag().toString());
			if (index != i) {
				tabs.get(index).setBackgroundResource(R.color.menu_bg_normal);
				tabs.get(i).setBackgroundResource(0);// 删除背景
				contents.get(index).setVisibility(View.GONE);
				contents.get(i).setVisibility(View.VISIBLE);
				index = i;
			}
		}
	};

	private GridView createGridView(List<int[]> data,
			final MenuAdapter.ItemListener listener) {
		GridView gridView = new GridView(context);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(params);
		gridView.setNumColumns(4);
		gridView.setHorizontalSpacing(15);
		gridView.setVerticalSpacing(15);
		gridView.setPadding(15, 15, 15, 15);
		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		gridView.setGravity(Gravity.CENTER);
		gridView.setOnItemClickListener(new OnItemClickListener() {// 设置点击事件

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listener.onClickListener(position, view);//
			}
		});
		gridView.setAdapter(new MenuAdapter(context, data)
				.setmItemListener(listener));// 传递一个实现了的接口.为什么不直接gridView.setonitemcliklistener
		return gridView;					//这样子响应的是每一项的按钮的监听
	}

	public void setDefaultTab(int index) {
		this.index = index;
	}

	public PopupWindow create() {
		popwindow = new PopupWindow(context);
		LinearLayout tab_layout = new LinearLayout(context);
		tab_layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		tab_layout.setOrientation(LinearLayout.HORIZONTAL);

		for (int i = 0, len = tabs.size(); i < len; i++) {
			final TextView textView = tabs.get(i);
			textView.setTag(i);                 //设置每个textview的id，点击事件时用到
			final GridView gridView = contents.get(i);
			if (index != i) {
				textView.setBackgroundResource(R.color.menu_bg_normal);//不是当前页设置背景色
				gridView.setVisibility(View.GONE);          //不是当前页就隐藏内容
			}
			tab_layout.addView(textView);//水平方向加了三个textview，当前页的颜色不一样
			if (i == 0 ) {
				layout.addView(tab_layout);
			}
			layout.addView(gridView);
		}

		popwindow.setWidth(LayoutParams.MATCH_PARENT);
		popwindow.setHeight(LayoutParams.WRAP_CONTENT);
		popwindow.setFocusable(true);
		popwindow.setAnimationStyle(R.style.popwindow_anim_style);
		ColorDrawable dw = new ColorDrawable(-00000);
		popwindow.setBackgroundDrawable(dw);
		popwindow.setContentView(layout);
		return popwindow;
	}

	/**
	 * 判断popwindow是否正在显示
	 * 
	 * @return
	 */
	public boolean isShowing() {
		return popwindow.isShowing();
	}

	public void showAtLocation(View parent, int gravity, int x, int y) {
		popwindow.showAtLocation(parent, gravity, x, y);
	}

	public void cancel() {
		popwindow.dismiss();
	}

}
