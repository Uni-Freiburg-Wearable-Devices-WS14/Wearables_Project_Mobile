package com.example.nfc_combine;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth.HexAsciiHelper;
import com.example.bluetooth.RFduinoService;
import com.example.nfc_combine.DatabaseHelper; // To Read from DB and Change Reminder
import com.example.bluetooth.BluetoothActivity; // For the Receiving Broadcast coming from BT

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID; // Not so sure

import com.example.nfc_combine.R;

import org.apache.http.util.ByteArrayBuffer;

public class ReminderService extends Service {

    private final static String TAG = ReminderService.class.getSimpleName();
    public final static String BROADCAST_UPDATEUI = "UpdateUI";
	private final static int mStartMode = Service.START_NOT_STICKY;
    private DatabaseHelper dbHelper;
    private NotificationManagerCompat mNotificationManager;
    private Intent g_sIntent = new Intent(BROADCAST_UPDATEUI);

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();
        dbHelper = new DatabaseHelper(getApplicationContext());
        //mNotificationManager = (NotificationManagerCompat) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent mIntent, int flags, int startId) {
    	Log.i(TAG, "onStart ReminderService");

    	if (mIntent.getBooleanExtra(RFduinoService.ACTION_DATA_SERVICE, false)) {
    		Log.i(TAG, "onStart ReminderService ResolveID");
            //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
            resolveID(mIntent.getByteArrayExtra(RFduinoService.EXTRA_DATA)); //Get NFC Data
    	}
        else {
            // TODO: KILL Service?? Given that it hasn't read anything
        	Log.i(TAG, "stopped");
            stopSelf();
        }
    	return mStartMode;
    }

    private void resolveID(byte[] byteArrayExtra) {
//        int tmpID = getDec(byteArrayExtra);
    	String hexID = HexAsciiHelper.bytesToAsciiMaybe(byteArrayExtra);
        //Log.i(TAG, HexAsciiHelper.bytesToAsciiMaybe(byteArrayExtra));
        //Log.i(TAG, HexAsciiHelper.bytesToHex(byteArrayExtra));
//        Log.i(TAG, String.valueOf(tmpID));
        Log.i(TAG, byteArrayExtra.toString());
        String[] tmpArray = getResources().getStringArray(R.array.tag_categories);
        // TODO: Handle Exceptions on DatabaseHelper
        String mObjectCat = dbHelper.checkIfObject(hexID);
        if(mObjectCat.equals(tmpArray[0])) { // Is a thing
            String mObjectName = dbHelper.toggleItem(hexID);            
            onMakeNotificationObject("Object touched", hexID, mObjectName);
            sendBroadcast(g_sIntent);
        }
        else if (mObjectCat.equals(tmpArray[1])) { // Is a Door - Necessary due to SEND ZERO Option
            List<NfcTag> mRemindList = dbHelper.getItemsToRemind();

            if (!mRemindList.isEmpty()) {
                Log.i(TAG,"List is not Empty");
                onMakeNotificationList("Items Missing", mRemindList); // List of ids!
            }
            else {Log.i(TAG,"List is Empty");}
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
        Log.i(TAG, "service destroyed");
        Toast.makeText(this,"Service Stopped", Toast.LENGTH_LONG).show();
    }

    //TODO String ID
    private void onMakeNotificationObject(CharSequence pTitle,String pId, String pItemName){
        // Create notification for new received object
        Intent notificationIntent = new Intent(ReminderService.this, DatabaseActivity.class); //Doubt about the main class
        notificationIntent.setAction("ReminderTest_CallToMain");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ReminderService.this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ReminderService.this)
                .setContentTitle(pTitle)
                .setTicker("NFC Tag was read")
                .setContentText("Data: " + pId + "-Name: " + pItemName)
//                .setContentText("Data:" + HexAsciiHelper.bytesToAsciiMaybe(characteristic.getValue()) + "\nOr: " + HexAsciiHelper.bytesToHex(characteristic.getValue()))
                .setSmallIcon(R.drawable.ic_launcher)
//                    .setLargeIcon(
                        // Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        mNotificationManager.notify(110, mBuilder.build());
    }

    private void onMakeNotificationList(CharSequence pTitle, List<NfcTag> pList) {
        Intent notificationIntent = new Intent(ReminderService.this, DatabaseActivity.class); //Doubt about the main class
        notificationIntent.setAction("ReminderList_CallToMain");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ReminderService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri mAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(pTitle); // Sets a title for the Inbox in expanded layout

        // Moves events into the expanded layout
        for (int i=0; i < pList.size(); i++) {
            inboxStyle.addLine(pList.get(i).getTagName());
        }
        //.setDefaults(Notification.DEFAULT_VIBRATE)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ReminderService.this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Remember!")
                .setContentText("It's dangerous to go alone, take this!")
                .setTicker("Don't forget!!")
                .setVibrate(new long[] {0, 1000, 5, 200})
                .setSound(mAlarmSound)
                .setAutoCancel(true)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent);

        mNotificationManager.notify(100, mBuilder.build());
    }

}