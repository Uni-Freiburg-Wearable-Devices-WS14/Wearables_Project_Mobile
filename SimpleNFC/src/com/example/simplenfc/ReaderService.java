package com.example.simplenfc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.util.Log;

public class ReaderService extends Service {
	
	private static String TAG = "ReaderService";
	private BroadcastReceiver tagReceiver;
	private static final int NOTIFICATION_ID = 3;
	
	public int onStartCommand(Intent intent, int flags, int startId){
		
		Log.i(TAG, "onStartCommand()");
		
		return START_NOT_STICKY;
		
	}
	
	public void onCreate(){
		super.onCreate();
		Log.i(TAG, "onCreate()");
		
		IntentFilter NFCfilter = new IntentFilter();
		NFCfilter.addAction("android.nfc.action.TAG_DISCOVERED");
		NFCfilter.addCategory("android.intent.category.DEFAULT");
		
		this.tagReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				
				Log.i(TAG, "new Tag detected!");
				resolveTag(intent);
				
			}
			
		};
		
		this.registerReceiver(tagReceiver, NFCfilter);
		Log.i(TAG, "BroadcastReceiver registered!");
	}
	
	
	private void resolveTag(Intent intent) {
		// TODO Auto-generated method stub
		
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
			Log.i(TAG, "resolveIntent");
					
			byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
			if(id != null){
				Log.i(TAG, "EXTRA_ID detected");
				long temp = getDec(id);
				Log.i(TAG, Long.toString(temp));
				
				makeNotify(temp);
			}
		} //else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {...
		  //for NDEF messages... ; left out for the moment
		
	}
	
	
	private void makeNotify(long temp) {
		// TODO Auto-generated method stub
		
		final Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
		final PendingIntent pendingInt = PendingIntent.getActivity(getApplicationContext(),
				0, toMain, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Notification.Builder notificationBuilder = new Notification.Builder(
				getApplicationContext())
				.setTicker("You scanned a Tag!")
				.setContentTitle("Here is your ID (in hex)")
//				.setContentText(Long.toString(temp))
				.setContentText(Long.toHexString(temp))
				.setSmallIcon(android.R.drawable.ic_dialog_info)
				
				.setContentIntent(pendingInt)
				.setAutoCancel(true);
		
		NotificationManager mManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		mManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		
	}

	
	private long getDec(byte[] bytes) {
		// TODO Auto-generated method stub
	long result = 0;
    long factor = 1;
    for (int i = 0; i < bytes.length; ++i) {
        long value = bytes[i] & 0xffl;
        result += value * factor;
        factor *= 256l;
    }
    return result;		
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onDestroy(){
		Log.i(TAG, "onDestroy()");
		this.unregisterReceiver(tagReceiver);
	}

}
