package cc.mightu.sms_forward;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.ArrayList;

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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action)){
                Log.i("sms", "on receive," + intent.getAction());
                if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                    String message_total = "";
                    String address = "";
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        String emailFrom = smsMessage.getEmailFrom();
                        address = smsMessage.getOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        Log.i("sms", "body: " + messageBody);
                        Log.i("sms", "address: " + address);
                        message_total = message_total + messageBody;
                    }

                    message_total = message_total + "\n[from " + address + "] ";
                    String number = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("number", "");
                    if (number == "") {
                        Log.i("sms", "phone number not set. ignore this one.");
                        return;
                    }
                    Log.i("sms", "sending to " + number);

                    Log.i("sms", "message send:" + message_total);
                    SmsManager sms = SmsManager.getDefault();
                    ArrayList<String> dividedMessages = sms.divideMessage(message_total);
                    sms.sendMultipartTextMessage(number, null, dividedMessages, null, null);
            }
        }
    }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("SmS Forwarding")
                    .setTicker("Smartphone Player")
                    .setContentText("Keep Me Alive")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
