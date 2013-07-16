package com.android.manage.appboot.bootapps;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.os.RemoteException;
import android.os.Binder;
import android.os.IBinder;
import java.io.IOException;

import com.android.manage.appboot.R;


import android.app.AppGlobals;
import android.content.pm.IPackageManager;


public class ApplicationInfoAdapter extends BaseAdapter{
	//自定义适配器类，提供给listView的自定义view  
	private List<AppInfo> mlistAppInfo = null;  
	LayoutInflater infater = null;  
	private ViewHolder holder;
   	private final static String USER_FLAG = "use";
	PackageManager pm ;
	Context mContext;
	SharedPreferences mSharedPreferences;
	SharedPreferences.Editor mEditor;
	
	public ApplicationInfoAdapter(Context context,  List<AppInfo> apps) {  
		infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		mlistAppInfo = apps ; 
		mContext = context;
	}
	
	@Override  
	public int getCount() {  
		// TODO Auto-generated method stub  
		return mlistAppInfo.size();  
	}  
	@Override  
	public Object getItem(int position) {  
		// TODO Auto-generated method stub  
		return mlistAppInfo.get(position);  
	}  
	@Override  
	public long getItemId(int position) {  
		// TODO Auto-generated method stub  
		return 0;  
	}

	@Override  
	public View getView(int position, View convertview, ViewGroup arg2) {  
		View view = null;  

//		if (convertview == null || convertview.getTag() == null) {  
			view = infater.inflate(R.layout.boot_acceleration_app_item, null);  
			holder = new ViewHolder(view);
//			view.setTag(holder);  
//		}   
//		else{  
//			view = convertview ;  
//			holder = (ViewHolder) convertview.getTag() ;  
//		}  
		AppInfo appInfo = (AppInfo) getItem(position);  
		holder.appIcon.setImageDrawable(appInfo.getAppIcon());  
		holder.tvAppLabel.setText(appInfo.getAppLabel());  
		holder.tvSwitch.setChecked((Boolean)ManageBootAppsFragment.appStatus.get(appInfo.getPkgName()));

		holder.tvSwitch.setOnCheckedChangeListener(new SwitchButtonChangeListener(position));
		return view;  
	}
	
	class SwitchButtonChangeListener implements OnCheckedChangeListener {
        private int position;

        SwitchButtonChangeListener(int pos){
            position= pos;
        }

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			AppInfo appInfo = (AppInfo) getItem(position);
            int vid = buttonView.getId();
            mSharedPreferences = mContext.getSharedPreferences("UserSetting",Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
            if(vid == holder.tvSwitch.getId()){
				Intent intent = new Intent();
            	intent.setAction("my.broadcast.stop.app.count");
            	if(isChecked){
            		//开机自启动
					final int userIdTemp = Binder.getCallingUid();
					final int userId = userIdTemp/100000;
					long callingId = Binder.clearCallingIdentity();
					try {
						IPackageManager pm = AppGlobals.getPackageManager();
						try {
							pm.setPackageStoppedState(appInfo.getPkgName(), false, userId);
						} catch (RemoteException e) {
						} 
					} finally {
						Binder.restoreCallingIdentity(callingId);
					}
					ManageBootAppsFragment.appStatus.put(appInfo.getPkgName(),true);
					intent.putExtra("stopCount",-1);
            		mContext.sendBroadcast(intent);
		           	mEditor.remove(appInfo.getPkgName());
		           	mEditor.commit();
            	}
            	else{
            		//关闭自启动
            		ManageBootAppsFragment.appStatus.put(appInfo.getPkgName(),false);
            		intent.putExtra("stopCount",+1);
            		mContext.sendBroadcast(intent);
            		mEditor.putString(appInfo.getPkgName(),appInfo.getPkgName());
            		mEditor.commit(); 
            	}
            }	
		}
    }

	class ViewHolder {  
		ImageView appIcon;  
		TextView tvAppLabel;  
		Switch tvSwitch;

		public ViewHolder(View view) {  
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);  
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);  
			this.tvSwitch = (Switch)view.findViewById(R.id.auto_switch);
		}  
	}  
}
