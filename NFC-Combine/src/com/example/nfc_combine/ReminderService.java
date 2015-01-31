package com.example.nfc_combine;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth.HexAsciiHelper;
import com.example.bluetooth.RFduinoService;
import com.example.nfc_combine.DatabaseHelper; // To Read from DB and Change Reminder
import com.example.bluetooth.BluetoothActivity; // For the Receiving Broadcast coming from BT

import java.util.List;
import java.util.UUID; // Not so sure

import com.example.nfc_combine.R;

public class ReminderService extends Service {

    private final static String TAG = ReminderService.class.getSimpleName();
	private final static int mStartMode = Service.START_NOT_STICKY;
    private DatabaseHelper dbHelper;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent mIntent, int flags, int startId) {
    	Log.i(TAG, "onStartCommand()");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

    	if (mIntent.getBooleanExtra(RFduinoService.ACTION_DATA_AVAILABLE, false)) {
            resolveID(mIntent.getByteArrayExtra(RFduinoService.EXTRA_DATA)); //Get NFC Data
    	}
        else {
            // TODO: KILL Service?? Given that it hasn't read anything
            stopSelf();
        }
    	return mStartMode;
    }

    private void resolveID(byte[] byteArrayExtra) {
        int tmpID = getDec(byteArrayExtra);
        if(dbHelper.checkIfObject(tmpID) == Resources.getSystem().getStringArray(R.array.tag_categories)[0]) {
            onMakeNotificationObject("Object Taken", tmpID);
        }
        else
        {
            List<NfcTag> mRemindList = dbHelper.getItemsToRemind();
            if (!mRemindList.isEmpty()) {
                onMakeNotificationList("Items Missing", mRemindList); // List of ids!
            }
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

    private void onMakeNotificationObject(CharSequence pTitle,int pId){
        // Create notification for new received data
        Intent notificationIntent = new Intent(ReminderService.this, DatabaseActivity.class); //Doubt about the main class
        notificationIntent.setAction("ReminderTest_CallToMain");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ReminderService.this, 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ReminderService.this)
                .setContentTitle(pTitle)
                .setTicker("NFC Tag was read")
                .setContentText("Data: " + pId)
//                .setContentText("Data:" + HexAsciiHelper.bytesToAsciiMaybe(characteristic.getValue()) + "\nOr: " + HexAsciiHelper.bytesToHex(characteristic.getValue()))
                .setSmallIcon(R.drawable.ic_launcher)
//                    .setLargeIcon(
                        // Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        mNotificationManager.notify(110, mBuilder.build());
    }

    private void onMakeNotificationList(CharSequence pTitle, List<NfcTag> pList) {

    }

}