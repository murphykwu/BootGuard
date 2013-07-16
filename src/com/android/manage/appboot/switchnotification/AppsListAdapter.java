package com.android.manage.appboot.switchnotification;

import java.util.ArrayList;
import java.util.List;

import com.android.manage.appboot.R;

import android.app.INotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<AppInfo> mData;
	private ViewHolder viewHolder;
//	private Handler uiHandler;
	//存放数据改变过的位置，并且记录起来，在列表滑动的时候进行修改
	public int[] mChangePosition;
	public static final int THE_POSITION_CHANGED = 8;	
	
	public AppsListAdapter(Context context, List<AppInfo> data)
	{
		mContext = context;
		mData = data;
//		uiHandler = handler;
		mLayoutInflater = LayoutInflater.from(context);
		//初始化记录某项是否点击的数组，如果点击过后就置为1，在重画的时候就会重新获取改变后的值而不是使用缓存值。
		mChangePosition = new int[mData.size()];
		for(int i = 0; i < mData.size(); i ++)
		{
//			Log.i(ManagerActivity.TAGS, "init mChangePosition");
			mChangePosition[i] = 0;
		}
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//如果是有改变的行，那么就不使用缓存数据，直接重新赋值
//		if(mChangePosition[position] == 8)
//		{
//			Log.i(ManagerActivity.TAGS, "值有改变的行mChangePosition[" + position + "]");
//			if(convertView != null)
//			{
//				Log.i(ManagerActivity.TAGS, "视图不为空，设置其为空 position = " + position);
//			}
//			convertView = null;
//			//需要重置标志位。置标志位是为了更新数据源，当更新完之后，状态再次改变就再次更新。所以需要重置，提高效率
//			mChangePosition[position] = 0;
//		}
		viewHolder = null;
		if(convertView == null)
		{
			convertView = mLayoutInflater.inflate(R.layout.switchnotification_app_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.iv_app = (ImageView)convertView.findViewById(R.id.iv_app_icon);
			viewHolder.tv_app = (TextView)convertView.findViewById(R.id.tv_app_name);
			viewHolder.s_change = (Switch)convertView.findViewById(R.id.auto_switch);
//			viewHolder.sbtn_app = (SwitchButton)convertView.findViewById(R.id.sb_switch);
			convertView.setTag(viewHolder);
		}else
		{
			viewHolder = (ViewHolder)convertView.getTag();
		}

		//设置图标、名称、消息通知可否
		viewHolder.iv_app.setImageDrawable(mData.get(position).appIcon);
		viewHolder.tv_app.setText(mData.get(position).appName);
//		Log.v(ManagerActivity.TAGS, "显示每一行的数据getView Position = " + position
//				+ ", pkgName = " + mData.get(position).appName
//				+ ", appCanNotification = " + mData.get(position).appCanNotification);
		viewHolder.s_change.setOnClickListener(new ListButtonOnClickListener(position));
		viewHolder.s_change.setChecked(mData.get(position).appCanNotification);
//		viewHolder.sbtn_app.setOnClickListener(new ListButtonOnClickListener(position));
//		viewHolder.sbtn_app.setChecked(mData.get(position).appCanNotification);
		//如果要针对每一行的switchbutton响应滑动操作，就需要针对每个按钮设置监听函数。为了区别是哪个应用
		//需要传入position来确定
//		viewHolder.sbtn_app.setOnCheckedChangeListener(new SwitchButtonOnCheckedChangeListener(position));
		
		return convertView;
	}
	
	
	class ListButtonOnClickListener implements OnClickListener
	{
		int mPosition;
		private INotificationManager nm;
		
		public ListButtonOnClickListener(int pos) {
			// TODO Auto-generated constructor stub
			mPosition = pos;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			SwitchButton sBtn = (SwitchButton)v;
//			mData.get(mPosition).appCanNotification = sBtn.isChecked();
			Switch s_ListItem = (Switch)v;
			mData.get(mPosition).appCanNotification = s_ListItem.isChecked();
			AppsListAdapter.this.notifyDataSetChanged();
//			Toast.makeText(mContext, "position " + mPosition
//					+ ", pkgName = " + mData.get(mPosition).appName
//					+ ", isChecked = " + mData.get(mPosition).appCanNotification, Toast.LENGTH_SHORT).show();
			mData.get(mPosition).setNotify(mData.get(mPosition).appCanNotification);
		}
	}
	
	private final static class ViewHolder{
		ImageView iv_app;
		TextView  tv_app;
		Switch s_change;
//		SwitchButton sbtn_app;
	}
}
