package com.example.watchcovid;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    public static String id = "test_channel_01";
    int notificationID = 1;

    private static final String TAG = "PostDetailActivity";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    DatabaseReference myRef1 = myRef.child("mudancas");

    public static final String TIPO = "com.example.watchcovid.TIPO";
    public static final String RESPOSTA = "com.example.watchcovid.RESPOSTA";
    public static final String NUMEROATUAL = "com.example.watchcovid.NUMEROATUAL";
    public static final String TOTAL = "com.example.watchcovid.TOTAL";


    private long lavou_sim;
    private long lavou_nao;
    private long mascara_sim;
    private long mascara_nao;
    private long total;

    private int tipo=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);


        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                lavou_sim = dataSnapshot.child("lavei").child("sins").getValue(Long.class);
                lavou_nao = dataSnapshot.child("lavei").child("naos").getValue(Long.class);
                mascara_sim = dataSnapshot.child("mascaras").child("sim").getValue(Long.class);
                mascara_nao = dataSnapshot.child("mascaras").child("nao").getValue(Long.class);

                total = dataSnapshot.child("total").getValue(Long.class);
                mTextView.setText(  "Maos Lavadas: " + lavou_sim +
                        "Mascaras Usadas: " + mascara_sim +
                        "Total: " + total);
                //Log.d(TAG, "Value is: " + lavou_sim);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // Enables Always-on
        setAmbientEnabled();


        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int random = new Random().nextInt(100);
                if(random < 50) {
                    addbuttonNoti(1,"Lava as mãos", "Não te esqueças de lavar as mão!", "Lavei", "Mais Tarde");
                }
                else {
                    addbuttonNoti(2,"Usa máscara", "Estás a usar máscara?", "Sim", "Não");
                }
            }

        }, 1000, 10000);


        createchannel();
    }

    void addbuttonNoti(int tipo, String titulo, String mensagem, String acaoPositiva, String acaoNegativa) {

        NotificationCompat.Action.WearableExtender inlineActionForWear2 =
                new NotificationCompat.Action.WearableExtender()
                        .setHintDisplayActionInline(true)
                        .setHintLaunchesActivity(true);



        //BOTAO SIM
        Intent shimIntent = new Intent(this, NotificationReceiver.class);
        shimIntent.putExtra(getPackageName(), notificationID);
        shimIntent.putExtra(TIPO, tipo);
        shimIntent.putExtra(RESPOSTA, 1);
        shimIntent.putExtra(TOTAL, total);

        if(tipo == 1)
            shimIntent.putExtra(NUMEROATUAL, lavou_sim);
        else if(tipo == 2)
            shimIntent.putExtra(NUMEROATUAL, mascara_sim);
        PendingIntent shimPendingIntent =
                PendingIntent.getActivity(this, 0, shimIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action Shim =
                new NotificationCompat.Action.Builder(
                        R.mipmap.ic_launcher, //FRANCO -> ICON DO SIM
                        //R.drawable.ic_action_time,
                        acaoPositiva,
                        shimPendingIntent)
                        .extend(inlineActionForWear2)
                        .build();


        //BOTAO NAO
        Intent naoIntent = new Intent(this, NotificationReceiver.class);
        naoIntent.putExtra(getPackageName(), notificationID);
        naoIntent.putExtra(TIPO, tipo);
        naoIntent.putExtra(RESPOSTA, 2);
        naoIntent.putExtra(TOTAL, total);
        if(tipo == 1)
            naoIntent.putExtra(NUMEROATUAL, lavou_nao);
        else if(tipo == 2)
            naoIntent.putExtra(NUMEROATUAL, mascara_nao);
        PendingIntent naoPendingIntent =
                PendingIntent.getActivity(this, 0, naoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action Nao =
                new NotificationCompat.Action.Builder(
                        R.mipmap.ic_launcher, //FRANCO -> ICON DO NAO
                        //R.drawable.ic_action_time,
                        acaoNegativa,
                        naoPendingIntent)
                        .extend(inlineActionForWear2)
                        .build();

        //Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.mipmap.ic_launcher) //FRANCO -> ICON DA NOTIFICAÇÃO (ACHO EU)
                        .setContentTitle(titulo)
                        .setContentText(mensagem)
                        .setChannelId(id)
                        .addAction(Shim)
                        .addAction(Nao);


        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationID, notificationBuilder.build());
        notificationID++;

    }


    private void createchannel() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(id,
                getString(R.string.channel_name),  //name of the channel
                NotificationManager.IMPORTANCE_HIGH);   //importance level
        //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
        // Configure the notification channel.
        mChannel.setDescription(getString(R.string.channel_description));
        mChannel.enableLights(true);
        //Sets the notification light color for notifications posted to this channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setShowBadge(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        nm.createNotificationChannel(mChannel);

    }
}