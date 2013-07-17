package com.android.manage.appboot.switchnotification;

import java.util.List;

import com.android.manage.appboot.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private List<AppInfo> mData;
	private ViewHolder viewHolder;
	public static final int THE_POSITION_CHANGED = 8;
	private int mCheckedCounts = 0;//记录消息推送打开的应用个数
	private CallBack mCb;
	
	public AppsListAdapter(Context context, List<AppInfo> data, CallBack cb)
	{
		mContext = context;
		mData = data;
		mCb = cb;
		mLayoutInflater = LayoutInflater.from(context);
		//初始化当前系统允许应用发送通知的个数
		mCheckedCounts = 0;
		for(int i = 0; i < mData.size(); i ++)
		{
//			Log.i(ManagerActivity.TAGS, "init mChangePosition");
			if(mData.get(i).appCanNotification)
			{
				mCheckedCounts ++;
			}
		}
		//初始化的时候，当所有的应用都被选中，那么设置mSwitchAll的状态。
		if(mCheckedCounts == mData.size())
		{
			Log.i(SwitchNotificationFragment.TAG, "size is full");
			mCb.updateSwitchAllButton(false);
		}else if(mCheckedCounts == 0)
		{
			Log.i(SwitchNotificationFragment.TAG, "size is 0");
			mCb.updateSwitchAllButton(true);
		}
		Log.i(SwitchNotificationFragment.TAG, "当前系统中允许通知的应用个数： mCheckedCounts = " + mCheckedCounts
				+ ", mData.size = " + mData.size());
	}

	public int getCheckedCount()
	{
		return mCheckedCounts;
	}
	
	public void setCheckedCount(int counts)
	{
		mCheckedCounts = counts;
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
		viewHolder = null;
		if(convertView == null)
		{
			convertView = mLayoutInflater.inflate(R.layout.switchnotification_app_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.iv_app = (ImageView)convertView.findViewById(R.id.iv_app_icon);
			viewHolder.tv_app = (TextView)convertView.findViewById(R.id.tv_app_name);
			viewHolder.s_change = (Switch)convertView.findViewById(R.id.auto_switch);
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
//		private INotificationManager nm;
		
		public ListButtonOnClickListener(int pos) {
			// TODO Auto-generated constructor stub
			mPosition = pos;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Switch s_ListItem = (Switch)v;
			//如果应用被选中，那么在此基础上加一，否则减一
			if(s_ListItem.isChecked())
			{
				mCheckedCounts ++;
				Log.i(SwitchNotificationFragment.TAG, "++ mCheckedCounts = " + mCheckedCounts);
			}else
			{
				mCheckedCounts --;
				Log.i(SwitchNotificationFragment.TAG, "-- mCheckedCounts = " + mCheckedCounts);
			}
			if(mCheckedCounts == mData.size())
			{
				Log.i(SwitchNotificationFragment.TAG, "onClick mCheckedCounts = " + mCheckedCounts);
				mCb.updateSwitchAllButton(false);
			}else if(mCheckedCounts == 0)
			{
				mCb.updateSwitchAllButton(true);
			}
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
	}
}
