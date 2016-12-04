package roxma.org.sms_forward;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String number = getSharedPreferences("data", Context.MODE_PRIVATE).getString("number", "");
        Log.d("log","number: " + number);
        EditText editText = (EditText) findViewById(R.id.edit_phone_number);
        editText.setText(number, TextView.BufferType.EDITABLE);
    }

    public void sendSMS(View v)
    {
        EditText editText = (EditText) findViewById(R.id.edit_phone_number);
        String number = editText.getText().toString();

        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putString("number", number);
        editor.commit();

        String message  = "This is a test message to " + number;
        Log.i("sms","message send:" + message);
        SmsManager.getDefault().sendTextMessage(number,null,message,null,null);
    }

}
