package com.example.watchcovid;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationReceiver extends WearableActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    DatabaseReference myRef1 = myRef.child("mudancas");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //super.finish();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        int tip = intent.getIntExtra(MainActivity.TIPO , 0);
        long numAtual = intent.getLongExtra(MainActivity.NUMEROATUAL , 0);
        long total = intent.getLongExtra(MainActivity.TOTAL, 0);

        String resposta = intent.getAction();


        Log.i("DEBUG JUAN:", "RESPOSTAS: " + intent.getAction());

        myRef1.child("total").setValue(total+1);

        if(tip == 1) { //MAOS
            if(resposta == "shim")
                myRef1.child("lavei").child("sins").setValue(numAtual+1);
            else
                myRef1.child("lavei").child("naos").setValue(numAtual+1);
        }
        else if(tip == 2) { //MASCARAS
            if(resposta == "shim")
                myRef1.child("mascaras").child("sim").setValue(numAtual+1);
            else
                myRef1.child("mascaras").child("nao").setValue(numAtual+1);
        }


        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();

        super.finish();

    }
}
