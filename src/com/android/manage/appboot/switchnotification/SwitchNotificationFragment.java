package com.android.manage.appboot.switchnotification;

import java.util.ArrayList;
import java.util.List;
import android.app.INotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import com.android.manage.appboot.R;

public class SwitchNotificationFragment extends Fragment {
	public static final String TAG = "SwitchNotifications";
	private PackageManager mPackageManager;
	private List<PackageInfo> mPackageInfoList;
	private ArrayList<AppInfo> mAppsList;
	private ArrayList<AppInfo> tempAppList;
	private ListView lv_apps;
	private AppsListAdapter mAppsAdp;
	private View mLayoutContainerLoading;
	private Thread mInitListThread;
	private Context mContext;
	public static final int SEND_INIT_LIST_MSG = 1000;
	public static final int SWITCH_LIST = SEND_INIT_LIST_MSG + 1;
	private Switch mSwitchAll;
	//标识是否switch的变动是用户点击造成的，如果是的话，需要更新listview，否则不需要更新，只需要更新switchallbutton即可。
	private boolean isChangedByUser = true;
	private boolean mPrevoiusSwitchState;
	private static final String FILENAME = "store_switchAll";
	private static String SWITCH_BUTTON_STATE = "previous_switchbutton_state";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.switchnotification_main,
				container, false);
		mSwitchAll = (Switch) rootView
				.findViewById(R.id.switch_all_Notifications);
		mSwitchAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				//如果只是由于list中所有的应用都选择上而导致switch状态发生改变
				//那么就不需要更改listview了，防止不断刷新listview。
				Log.i(TAG, "onCheckedChanged isChangedByuser = " + isChangedByUser);
				if(!isChangedByUser)
				{
					isChangedByUser = true;
					return;
				}
				/**
				 * 当开关打开的时候，就是屏蔽所有应用的通知。
				 * 直接将下面的listview灰显，并且改变下面list中每项的状态为真，同时记录所有的状态。
				 * 当开关关闭的时候，将所有应用的通知选项恢复成全部屏蔽之前的， 也就是需要存储屏蔽所有之前应用通知状态。
				 * 在没有确认的情况下，只需要将所有的应用置为打开就行了。可以用发送handle的方式来更新，免得阻塞UI界面
				 */
				mLayoutContainerLoading.setVisibility(View.VISIBLE);
				lv_apps.setVisibility(View.INVISIBLE);
				mSwitchAll.setClickable(false);
				Log.i(TAG, "onCheckedChanged switch all isChecked = "
						+ isChecked);
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						boolean isChecked = mSwitchAll.isChecked();
						int appsSize = mAppsList.size();
						for (int i = 0; i < appsSize; i++) {
							// 如果这个开关打开了，那么就关闭下面所有应用的通知开关。
							mAppsList.get(i).appCanNotification = !isChecked;
							mAppsList.get(i).setNotify(!isChecked);
						}
						Message msg = new Message();
						msg.what = SWITCH_LIST;
						mHandler.sendMessage(msg);
					}
				}).start();
			}
		});

		mContext = SwitchNotificationFragment.this.getActivity();
		mPackageManager = mContext.getPackageManager();
		mPackageInfoList = mPackageManager.getInstalledPackages(0);
		mAppsList = new ArrayList<AppInfo>();
		lv_apps = (ListView) rootView.findViewById(R.id.lv_apps);
		mLayoutContainerLoading = (View) rootView
				.findViewById(R.id.loading_container);
		mLayoutContainerLoading.setVisibility(View.VISIBLE);
		lv_apps.setVisibility(View.INVISIBLE);
		
		//初始化mSwitchAll按钮
		SharedPreferences sharedPref = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
		mPrevoiusSwitchState = sharedPref.getBoolean(SWITCH_BUTTON_STATE, false);
		
		lv_apps.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Log.i(TAG, "OnItemClickListener view = " + arg1.toString());

			}
		});

		Log.i(TAG, "onCreate");

		mInitListThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!Thread.currentThread().isInterrupted()) {
					Log.i(TAG, "Thread run() start initAppList");
					initAppsList();
				}
			}
		});
		mInitListThread.start();// 启动初始化列表线程
		


		return rootView;
	}

	/**
	 * 在程序刚启动的时候，初始化目标应用程序的各种数据 名称、图标、是否可以推送消息。因为有可能用户在管理应用程序这个
	 * 界面对是否可以推送消息进行了修改。
	 */
	private void initAppsList() {
		// 为了更友好的显示程序，要求在初始化列表的时候显示一个滚动条来提示用户等待
		Log.i(TAG, "initAppsList");
		INotificationManager nm = INotificationManager.Stub
				.asInterface(ServiceManager
						.getService(Context.NOTIFICATION_SERVICE));
		AppInfo tmpInfo = null;
		tempAppList = new ArrayList<AppInfo>();
		int size = mPackageInfoList.size();
		for (int i = 0; i < size; i++) {
			// Log.i(TAG, "initAppsList i = " + i);
			PackageInfo packageInfo = mPackageInfoList.get(i);
			// 将非系统应用添加到列表中来
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				tmpInfo = new AppInfo(mContext);
				tmpInfo.appName = packageInfo.applicationInfo.loadLabel(
						mPackageManager).toString();
				tmpInfo.packageName = packageInfo.packageName;
				tmpInfo.versionName = packageInfo.versionName;
				tmpInfo.versionCode = packageInfo.versionCode;
				tmpInfo.appIcon = packageInfo.applicationInfo
						.loadIcon(mPackageManager);

				// 获取当前应用通知状态，是否可以发送通知
				try {
					tmpInfo.appCanNotification = nm
							.areNotificationsEnabledForPackage(tmpInfo.packageName);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					tmpInfo.appCanNotification = false;
				}
				tempAppList.add(tmpInfo);
			}
		}
		Log.i(TAG, "initAppsList send a message to show listview");
		Message message = new Message();
		message.what = SwitchNotificationFragment.SEND_INIT_LIST_MSG;
		message.obj = tempAppList;
		mHandler.sendMessage(message);
	}

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case SEND_INIT_LIST_MSG:
				Log.i(TAG, "handleMessage SEND_INIT_LIST_MSG");
				// 特别注意，在非UI线程下不能够对主线程中listview绑定的list数据进行修改。只能创建一个temlist，然后在
				// handle里面赋值。
				mAppsList = (ArrayList<AppInfo>) msg.obj;
				mAppsAdp = new AppsListAdapter(mContext, mAppsList,
						new CallBack() {
							public void updateSwitchAllButton(boolean isChecked) {
								Log.i(TAG, "updateSwitchAllButton");
								isChangedByUser = false;
								mSwitchAll.setChecked(isChecked);
								isChangedByUser = true;
							}
						});
				lv_apps.setAdapter(mAppsAdp);
				mAppsAdp.notifyDataSetChanged();
				isChangedByUser = false;
				mSwitchAll.setChecked(mPrevoiusSwitchState);
				isChangedByUser = true;
				mLayoutContainerLoading.setVisibility(View.INVISIBLE);
				lv_apps.setVisibility(View.VISIBLE);
				break;
			case SWITCH_LIST:
				mLayoutContainerLoading.setVisibility(View.INVISIBLE);
				lv_apps.setVisibility(View.VISIBLE);
				mSwitchAll.setClickable(true);
				mSwitchAll.setActivated(false);
				mAppsAdp.notifyDataSetChanged();
				lv_apps.invalidate();
				if(mSwitchAll.isChecked())
				{
					mAppsAdp.setCheckedCount(0);
				}else
				{
					mAppsAdp.setCheckedCount(mAppsList.size());
				}
				break;
			}
			super.handleMessage(msg);
		}
	};


	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onStop");
		SharedPreferences sp = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(SWITCH_BUTTON_STATE, mSwitchAll.isChecked());
		editor.commit();
		super.onStop();
		
	}

}















