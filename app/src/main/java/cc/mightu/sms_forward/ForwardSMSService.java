package cc.mightu.sms_forward;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;

public class ForwardSMSService extends Service {
    private static final String LOG_TAG = "ForwardSMSService";

    private static final String SMS_INBOX_URI = "content://sms/inbox";
//    private static Uri uriSMS = Uri.parse("content://mms-sms/conversations/");

    private static final String[] PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
    };

    private long mReceivedMsgDate = 0;

    private ContentObserver SmsContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i("sms", "sms inbox change detected");
            Cursor cursor = getContentResolver().query(Uri.parse(SMS_INBOX_URI), PROJECTION, null, null, "date DESC");
            if (cursor != null && !cursor.isClosed()) {
                if (cursor.moveToFirst()) {
                    final long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    String recv_date = DateFormat.format("h:mm:ss", new Date(date)).toString();
                    String address = cursor.getString(1);
                    String messageBody = cursor.getString(2);

                    String message = "[" + address + "on: " + recv_date + "] " + messageBody;

                    String number = getSharedPreferences("data", Context.MODE_PRIVATE).getString("number", "");
                    SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
                    Log.i("sms", "message send:" + message);
                }
            }
            cursor.close();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            getContentResolver().registerContentObserver(Uri.parse(SMS_INBOX_URI), true, SmsContentObserver);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("SmS Forwarding")
                    .setTicker("Truiton Music Player")
                    .setContentText("My Music")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            getContentResolver().unregisterContentObserver(SmsContentObserver);
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().registerContentObserver(Uri.parse(SMS_INBOX_URI), true, SmsContentObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(SmsContentObserver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

