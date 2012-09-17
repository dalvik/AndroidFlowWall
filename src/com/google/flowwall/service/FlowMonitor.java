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
				+ File.separator + Constants.ON_NAME);// ��ȡon.txt��¼��onstr��
		String ondata[] = onstr.split(",");// ��onstr������� �ŵ�ondata��
		// ��������
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
		// ��ȡ��ǰʱ��
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR); // ��ȡ��ǰ���
		month = c.get(Calendar.MONTH) + 1;// ��ȡ��ǰ�·�
		day = c.get(Calendar.DAY_OF_MONTH);// ��ȡ��ǰ�·ݵ����ں���
		date = year + "-" + month + "-" + day;

		String text=FileUtil.getFileInfo(FlowMonitor.this.getFilesDir()
				+ File.separator + Constants.LOG_NAME);//��log.txt�����ݶ���text�ַ�����
		String [] line=text.split("\n");   
		String today=line[line.length-1];//��ý����Ѽ�¼����   
		String [] beToday=today.split(",");
		//����ļ����һ���Ƿ�Ϊ�����������¼��Ϣ   
	    if(!beToday[0].equals(date)) {//�жϽ��������Ƿ��Ѿ���¼,�����������û�м�¼  
	          text=text+date+",0,0,0,0,0,0,0,0,0,0,0,0\n";  
	          FileUtil.writeFile(text,FlowMonitor.this.getFilesDir() + File.separator + Constants.LOG_NAME);  
	          line=text.split("\n");  
	          today=line[line.length-1];//��ý����Ѽ�¼����   
	          beToday=today.split(",");   
	    }  
	    int i;  
	    //�����������   
	    int [] newTodaydata=new int [12];//��ʾ��������   
	    sb.delete(0, sb.length());
	    sb.append(date);
	    for(i=0;i<=11;i++){  //���½�������   
	          newTodaydata[i]=Integer.parseInt(beToday[i+1])+delta[i];  
	          sb.append("," + newTodaydata[i]);
	     }  
          sb.append("\n");
	      String [] beTotal=line[0].split(",");  
	      int [] newTotaldata=new int [12];//��ʾ��������ֵ   
	      //���µ�һ��   
	      newTotalFlow.delete(0, newTotalFlow.length());
	      newTotalFlow.append("total");
	      for(i=0;i<=11;i++)//���½���������������   
	      {    
	          newTotaldata[i]=Integer.parseInt(beTotal[i+1])+delta[i];//��������ֵ+delta[i]����   
	          newTotalFlow.append("," + newTodaydata[i]);
	      }  
	      newTotalFlow.append("\n");
	      //�����м䲻��Ĳ���   
	      beforeTotalFlow.delete(0, beforeTotalFlow.length());//beforeTotalFlowΪ֮ǰ�Ĵӵ�1�е������������¼   
	      for(i=1;i<=line.length-2;i++) {
	    	  beforeTotalFlow.append(line[i] +"\n");//�����м䲻��Ĳ���   
	      }
	      String newlog=newTotalFlow.toString() + beforeTotalFlow.toString() + sb.toString();  
	      flowDataTextView.setText(getString(R.string.show_today_total_flow_data, FileUtil.formetFileSize(newTotaldata[1])));
	      FileUtil.writeFile(data,FlowMonitor.this.getFilesDir() + File.separator + Constants.ON_NAME);//����������¼   
	      FileUtil.writeFile(newlog,FlowMonitor.this.getFilesDir() + File.separator + Constants.LOG_NAME);//����log*/   
	}  

	private void createView() {
		// ��ȡWindowManager
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		// ����LayoutParams(ȫ�ֱ�������ز���
		wmParams =  ((WallApplication) getApplication()).getMywmParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;// �������ṩ���û���������������Ӧ�ó����Ϸ���������״̬������
		wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// �������κΰ����¼�
		wmParams.gravity = Gravity.LEFT | Gravity.TOP; // �����������������Ͻ�
		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		wmParams.x = wm.getDefaultDisplay().getWidth();
		wmParams.y = 0;
		// �����������ڳ�������
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = PixelFormat.RGBA_8888;

		wm.addView(view, wmParams);

		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// ��ȡ�����Ļ�����꣬������Ļ���Ͻ�Ϊԭ��
				x = event.getRawX();
				// 25��ϵͳ״̬���ĸ߶�,Ҳ����ͨ�������õ�׼ȷ��ֵ���Լ�΢��������
				y = event.getRawY()-25 ; 
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// ��ȡ���View�����꣬���Դ�View���Ͻ�Ϊԭ��
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
		// ���¸�������λ�ò���
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(view, wmParams);
	}
}
