package com.google.flowwall.service;

import java.io.File;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.flowwall.R;
import com.google.flowwall.power.WallApplication;
import com.google.flowwall.utils.Constants;
import com.google.flowwall.utils.FileUtil;

public class FlowMonitor extends Service {

	private Handler myHandler = new Handler();

	private String[] ethData = { "0", "0", "0", "0", "0", "0", "0", "0", 
								 "0", "0", "0", "0", "0", "0", "0", "0" };

	private String[] gprsData = { "0", "0", "0", "0", "0", "0", "0", "0", 
								  "0", "0", "0", "0", "0", "0", "0", "0" };

	private String[] wifiData = { "0", "0", "0", "0", "0", "0", "0", "0", 
								  "0", "0", "0", "0", "0", "0", "0", "0" };

	private String data = "0,0,0,0,0,0,0,0,0,0,0,0";

	private int year;
	
	private int month;
	
	private int day;
	
	private String date;

	private StringBuffer sb = new StringBuffer();
	
	private StringBuffer newTotalFlow = new StringBuffer();
	
	private StringBuffer beforeTotalFlow = new StringBuffer();
	
	private WindowManager wm = null;
	
	private WindowManager.LayoutParams wmParams = null;
	
	private View view;
	
	private TextView flowDataTextView = null;
	
	private float mTouchStartX;
	
	private float mTouchStartY;
	
	private float x;
	
	private float y;
	
	private String TAG = "FlowMonitorService";

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			refresh();
			myHandler.postDelayed(runnable, Constants.FLUSH_RATE);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		view = LayoutInflater.from(this).inflate(R.layout.floating, null);
		flowDataTextView = (TextView) view.findViewById(R.id.float_data_id);
		FileUtil.writeFile("0,0,0,0,0,0,0,0,0,0,0,0", getFilesDir().getPath()
				+ File.separator + Constants.ON_NAME);
		createView();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		myHandler.postDelayed(runnable, 0);
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		myHandler.removeCallbacks(runnable);
		super.onDestroy();
	}

	private void refresh() {
		FileUtil.readDev(ethData, gprsData, wifiData);
		data = ethData[0] + "," + ethData[1] + "," + ethData[8] + "," + ethData[9] + "," + 
			   gprsData[0] + "," + gprsData[1] + ","	+ gprsData[8] + "," + gprsData[9] + "," + 
			   wifiData[0] + "," + wifiData[1] + "," + wifiData[8] + "," + wifiData[9];
		String onstr = FileUtil.getFileInfo(FlowMonitor.this.getFilesDir()
				+ File.separator + Constants.ON_NAME);// 读取on.txt记录到onstr里
		String ondata[] = onstr.split(",");// 将onstr各项分离 放到ondata里
		// 计算增量
		int[] delta = new int[12];
		delta[0] = Integer.parseInt(ethData[0]) - Integer.parseInt(ondata[0]);
		delta[1] = Integer.parseInt(ethData[1]) - Integer.parseInt(ondata[1]);
		delta[2] = Integer.parseInt(ethData[8]) - Integer.parseInt(ondata[2]);
		delta[3] = Integer.parseInt(ethData[9]) - Integer.parseInt(ondata[3]);
		delta[4] = Integer.parseInt(gprsData[0]) - Integer.parseInt(ondata[4]);
		delta[5] = Integer.parseInt(gprsData[1]) - Integer.parseInt(ondata[5]);
		delta[6] = Integer.parseInt(gprsData[8]) - Integer.parseInt(ondata[6]);
		delta[7] = Integer.parseInt(gprsData[9]) - Integer.parseInt(ondata[7]);
		delta[8] = Integer.parseInt(wifiData[0]) - Integer.parseInt(ondata[8]);
		delta[9] = Integer.parseInt(wifiData[1]) - Integer.parseInt(ondata[9]);
		delta[10] = Integer.parseInt(wifiData[8])
				- Integer.parseInt(ondata[10]);
		delta[11] = Integer.parseInt(wifiData[9])
				- Integer.parseInt(ondata[11]);
		// 获取当前时间
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR); // 获取当前年份
		month = c.get(Calendar.MONTH) + 1;// 获取当前月份
		day = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
		date = year + "-" + month + "-" + day;

		String text=FileUtil.getFileInfo(FlowMonitor.this.getFilesDir()
				+ File.separator + Constants.LOG_NAME);//将log.txt的内容读到text字符串中
		String [] line=text.split("\n");   
		String today=line[line.length-1];//获得今日已记录流量   
		String [] beToday=today.split(",");
		//检查文件最后一行是否为今天的流量记录信息   
	    if(!beToday[0].equals(date)) {//判断今日流量是否已经记录,如果今日流量没有记录  
	          text=text+date+",0,0,0,0,0,0,0,0,0,0,0,0\n";  
	          FileUtil.writeFile(text,FlowMonitor.this.getFilesDir() + File.separator + Constants.LOG_NAME);  
	          line=text.split("\n");  
	          today=line[line.length-1];//获得今日已记录流量   
	          beToday=today.split(",");   
	    }  
	    int i;  
	    //处理今日流量   
	    int [] newTodaydata=new int [12];//表示今日流量   
	    sb.delete(0, sb.length());
	    sb.append(date);
	    for(i=0;i<=11;i++){  //更新今日流量   
	          newTodaydata[i]=Integer.parseInt(beToday[i+1])+delta[i];  
	          sb.append("," + newTodaydata[i]);
	     }  
          sb.append("\n");
	      String [] beTotal=line[0].split(",");  
	      int [] newTotaldata=new int [12];//表示总流量数值   
	      //更新第一行   
	      newTotalFlow.delete(0, newTotalFlow.length());
	      newTotalFlow.append("total");
	      for(i=0;i<=11;i++)//更新今日流量和总流量   
	      {    
	          newTotaldata[i]=Integer.parseInt(beTotal[i+1])+delta[i];//总流量数值+delta[i]更新   
	          newTotalFlow.append("," + newTodaydata[i]);
	      }  
	      newTotalFlow.append("\n");
	      //处理中间不变的部分   
	      beforeTotalFlow.delete(0, beforeTotalFlow.length());//beforeTotalFlow为之前的从第1行到昨天的流量记录   
	      for(i=1;i<=line.length-2;i++) {
	    	  beforeTotalFlow.append(line[i] +"\n");//代表中间不变的部分   
	      }
	      String newlog=newTotalFlow.toString() + beforeTotalFlow.toString() + sb.toString();  
	      flowDataTextView.setText(getString(R.string.show_today_total_flow_data, FileUtil.formetFileSize(newTotaldata[1])));
	      FileUtil.writeFile(data,FlowMonitor.this.getFilesDir() + File.separator + Constants.ON_NAME);//更新流量记录   
	      FileUtil.writeFile(newlog,FlowMonitor.this.getFilesDir() + File.separator + Constants.LOG_NAME);//更新log*/   
	}  

	private void createView() {
		// 获取WindowManager
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		// 设置LayoutParams(全局变量）相关参数
		wmParams =  ((WallApplication) getApplication()).getMywmParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;// 该类型提供与用户交互，置于所有应用程序上方，但是在状态栏后面
		wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 不接受任何按键事件
		wmParams.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = wm.getDefaultDisplay().getWidth();
		wmParams.y = 0;
		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = PixelFormat.RGBA_8888;

		wm.addView(view, wmParams);

		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// 获取相对屏幕的坐标，即以屏幕左上角为原点
				x = event.getRawX();
				// 25是系统状态栏的高度,也可以通过方法得到准确的值，自己微调就是了
				y = event.getRawY()-25 ; 
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchStartX = event.getX();
					mTouchStartY = event.getY()+view.getHeight()/2;
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition();
					mTouchStartX = mTouchStartY = 0;
					break;
				}
				return true;
			}

		});
	}
	
	private void updateViewPosition() {
		// 更新浮动窗口位置参数
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(view, wmParams);
	}
}
