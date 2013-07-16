package com.android.manage.appboot.bootapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.manage.appboot.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ManageBootAppsFragment extends Fragment {
	
   	public static final int FILTER_ALL_APP = 0; // 所有应用程序  
   	public static final int FILTER_SYSTEM_APP = 1; // 系统程序  
   	public static final int FILTER_THIRD_APP = 2; // 第三方应用程序  
   	public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序  
   	public static final String BOOT_START_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";
   	
   	private ListView listview = null;
   	private TextView tips,textView;
   	private PackageManager pm; 
   	private int filter = FILTER_THIRD_APP;   
	private List<AppInfo> mlistAppInfo ;  
   	private ApplicationInfoAdapter browseAppAdapter = null ;  	   	
   	private String[] appPkgNames;
   	private String[] shareValues;
   	SharedPreferences mSharedPreferences;
   	SharedPreferences.Editor mEditor;
   	Context useCount;
   	private int stopCount;
   	static HashMap<String, Object> appStatus = new HashMap<String, Object>();
   	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = this.getActivity();
		View rootView = inflater.inflate(R.layout.boot_acceleration_main, container, false);
        mSharedPreferences = mContext.getSharedPreferences("UserSetting", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
  
	    //把所有的键值队放入Map,并合并为一个字符数组
        Map<String, ?>AppSet = mSharedPreferences.getAll();
        shareValues = getMapValue(AppSet).split("\\,");
        appPkgNames = shareValues; 
        
        textView = (TextView)rootView.findViewById(R.id.tips_none);
        tips = (TextView)rootView.findViewById(R.id.count_text);
        listview = (ListView) rootView.findViewById(R.id.listviewApp); 
        mlistAppInfo = queryFilterAppInfo(filter);
		return rootView;
	}
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals("my.broadcast.stop.app.count")){
				int count = intent.getIntExtra("stopCount", 0);
				if(count==-1){
					stopCount = stopCount-1;
					tips.setText(stopCount+getResources().getString(R.string.back_tips));
				}else if(count==1){
					stopCount = stopCount+1;
					tips.setText(stopCount+getResources().getString(R.string.back_tips));
				}
			}
		}
	};

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
        //把所有的键值队放入Map,并合并为一个字符数组
        mSharedPreferences = mContext.getSharedPreferences("UserSetting", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    	Map<String, ?>appMap = mSharedPreferences.getAll();
    	String appList = getMapValue(appMap);
    	Settings.System.putString(mContext.getContentResolver(), "appList", appList);
		mContext.unregisterReceiver(myReceiver);
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
        mSharedPreferences = mContext.getSharedPreferences("UserSetting", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
		
		IntentFilter intentfilter = new IntentFilter();  
        intentfilter.addAction("my.broadcast.stop.app.count");   
        mContext.registerReceiver(myReceiver, intentfilter);
		
	    //把所有的键值队放入Map,并合并为一个字符数组
        Map<String, ?>AppSet = mSharedPreferences.getAll();
        shareValues = getMapValue(AppSet).split("\\,");
        appPkgNames = shareValues; 
		stopCount = 0;
		mlistAppInfo = queryFilterAppInfo(filter); // 查询所有应用程序信息  
		for(int j=0;j<appPkgNames.length;j++){
			for(int i=0;i<mlistAppInfo.size();i++){
				//Log.v("appPkgNames",mlistAppInfo.get(i).getPkgName().toString());
				if((!appPkgNames[j].equals(mlistAppInfo.get(i).getPkgName().toString()))){
					if(i==mlistAppInfo.size()-1){
						mEditor.remove(appPkgNames[j]);
						appPkgNames[j].indexOf("");
						mEditor.commit();
						Log.v("appPkgNames", "remove:"+appPkgNames[j]+"mlistAppInfo:"+mlistAppInfo.get(i).getPkgName());
					}
				}else{
					break;
				}
			}
		}
		Log.v("appPkgNames","mlistAppInfo:"+mlistAppInfo.isEmpty());
        // 构建适配器，并且注册到listView
        if(!mlistAppInfo.isEmpty()){							
        	textView.setVisibility(View.GONE);
        	listview.setVisibility(View.VISIBLE);
	        browseAppAdapter = new ApplicationInfoAdapter(mContext, mlistAppInfo);  
	        listview.setAdapter(browseAppAdapter);
	        tips.setText(stopCount+getResources().getString(R.string.back_tips));
        }else{
        	tips.setVisibility(View.GONE);
        	listview.setVisibility(View.GONE);
        	textView.setText(getResources().getString(R.string.none_tips));
        	textView.setVisibility(View.VISIBLE);
        }
		super.onResume();
	}
	
	public static String getMapValue(Map<String, ?> AppSet){
    	String result="";
		for (Map.Entry<String, ?> m : AppSet.entrySet()){
    		String value = (String) m.getValue();
    		if(!value.equals("")){
    			result = result+value+",";
    		}
    	}
		return result;
	}
	
    // 根据查询条件，查询特定的ApplicationInfo  
    public List<AppInfo> queryFilterAppInfo(int filter) {
    	
        pm = mContext.getPackageManager();
    	
        // 查询所有已经安装的应用程序  
        List<ApplicationInfo> listAppcations = pm  
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);  
        Collections.sort(listAppcations,  
                new ApplicationInfo.DisplayNameComparator(pm));// 排序  
        List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo  
        appInfos.clear(); 
        
            for (ApplicationInfo app : listAppcations) {  
                //非系统程序  
                if (((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)&&PackageManager.PERMISSION_GRANTED==
                           mContext.getPackageManager().checkPermission(BOOT_START_PERMISSION, app.packageName)) {
                	
                	boolean flag = false;
                	if(!appPkgNames.equals(null)){
                		for(int i=0;i<appPkgNames.length;i++){
	         	        	//这里是程序名
	         		        if(app.packageName.equals(appPkgNames[i])){
	         		        	flag = false;
	         		        	appStatus.put(app.packageName, false);
	         		        	stopCount++;
	         		        	break;
	         		        }else{
	         		        	//如果遍历到最后一个都没有设置禁止，那么就让他状态为允许
	         		        	if(i==(appPkgNames.length-1)){
	         		        		flag = true;
	         		        		appStatus.put(app.packageName, true);
	         		        	}
	         		        }
	         	        }
                	}else{
                		flag = true;
                	}
                	appInfos.add(getAppInfo(app,flag));  
                }   
                //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了  
                else if (((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)&&PackageManager.PERMISSION_GRANTED==
                           mContext.getPackageManager().checkPermission(BOOT_START_PERMISSION, app.packageName)){  
                	boolean flag = false;
                	if(!appPkgNames.equals(null)){
                		for(int i=0;i<appPkgNames.length;i++){
	         	        	//这里是程序名
	         		        if(app.packageName.equals(appPkgNames[i])){
	         		        	flag = false;
	         		        	appStatus.put(app.packageName, false);
	         		        	break;
	         		        }else{
	         		        	//如果遍历到最后一个都没有设置禁止，那么就让他状态为允许
	         		        	if(i==(appPkgNames.length-1)){
	         		        		flag = true;
	         		        		appStatus.put(app.packageName,true);
	         		        	}
	         		        }
	         	        }
                	}else{
                		flag = true;
                	}
                	appInfos.add(getAppInfo(app,flag));  
                } 
            }  
        return appInfos;  
    }  
    // 构造一个AppInfo对象 ，并赋值  
    private AppInfo getAppInfo(ApplicationInfo app,boolean flag) {    
    	
        AppInfo appInfo = new AppInfo();  
        appInfo.setAppLabel((String) app.loadLabel(pm));  
        appInfo.setAppIcon(app.loadIcon(pm));  
        appInfo.setPkgName(app.packageName);
        appInfo.setAppFlag(flag);
        return appInfo;  
    } 
    //逐行读取文件内容
    public String getFromAssets(String fileName) throws IOException{ 
    	String line="";
    	String Result=""; 
    	InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
    	BufferedReader bufReader = new BufferedReader(inputReader);
        try { 
            	while((line = bufReader.readLine()) != null){
            		Result += line;
            	}       	
        } catch (Exception e) { 
            e.printStackTrace(); 
    }finally{
    	bufReader.close();
    	}
        return Result;
    }
}
