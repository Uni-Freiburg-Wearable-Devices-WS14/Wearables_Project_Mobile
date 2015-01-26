package com.example.nfc_combine;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth.RFduinoService;
import com.example.nfc_combine.DatabaseHelper; // To Read from DB and Change Reminder
import com.example.bluetooth.BluetoothActivity; // For the Receiving Broadcast coming from BT

import java.util.UUID; // Not so sure

import com.example.nfc_combine.R;

public class ReminderService extends Service {
	private final static String TAG = ReminderService.class.getSimpleName();
	private final static int mStartMode = Service.START_NOT_STICKY;

    private DatabaseHelper dbHelper;

	// private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
 //        @Override
 //        public void onReceive(Context context, Intent intent) {
 //            final String action = intent.getAction();
 //            Log.w("Main","rfduinoReceiver called with " + action);
 //            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
 //                upgradeState(STATE_CONNECTED);
 //            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
 //                downgradeState(STATE_DISCONNECTED);
 //            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
 //                addData(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
 //            }
 //        }
 //    };

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
        // mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent mIntent, int flags, int startId) {
    	Log.i(TAG, "onStartCommand()");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

    	if (mIntent.getBooleanExtra(RFduinoService.ACTION_DATA_AVAILABLE, false)) {
    		//Get NFC Tag Data
            resolveID(mIntent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
    	}
        else {
            // TODO: KILL Service?? Given that it hasn't read anything
            stopSelf();
        }
    	return mStartMode;
    }

    private void resolveID(byte[] byteArrayExtra) {
        int tmpID = getDec(byteArrayExtra);
        if(dbHelper.checkIfObject(tmpID)) {
            // TAG OBJECT THAT MEANS WE HAVE OBJECT
        }
        else
        {
            // MEANS YOU ARE AT DOOR AND YOU NEED TO CHECK YOUR LISTÇ
            // IF THE LIST IS EMPTY MEANS THAT YOU DON'T GET A NOTIFICATION
            // OTHERWISE YOU GET A NOTIFICATION
        }

    }

    private int getDec(byte[] bytes) {
        int result = 0;
        int factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            int value = (int) (bytes[i] & 0xffl);
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    @Override
    public IBinder onBind(Intent mIntent){
        // We don't provide binding So return null
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this,"Service Stopped", Toast.LENGTH_LONG).show();
    }

}