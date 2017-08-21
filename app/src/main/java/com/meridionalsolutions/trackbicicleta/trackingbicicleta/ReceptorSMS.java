package com.meridionalsolutions.trackbicicleta.trackingbicicleta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Usuario on 10/6/2017.
 */

public class ReceptorSMS extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        SmsMessage[]    mensaje;
        String str = "", soloMensaje = "";

        if (extras!=null){
            Object[] pdus = (Object[]) extras.get("pdus");
            mensaje = new SmsMessage[pdus != null ? pdus.length : 0];
            for (int i=0; i<mensaje.length; i++){
                mensaje[i] = SmsMessage.createFromPdu((byte[]) (pdus != null ? pdus[i]:null));
                str += mensaje[i].getOriginatingAddress();
                str += ": ";
                str += mensaje[i].getMessageBody();
                str += "\n";
                soloMensaje = mensaje[i].getMessageBody();

            }
            //Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            Intent broadcastIntent  = new Intent();
            broadcastIntent.setAction("SMS_RECEIVED_ACTION");
            broadcastIntent.putExtra("MESSAGE",soloMensaje);
            context.sendBroadcast(broadcastIntent);

        }




    }
}