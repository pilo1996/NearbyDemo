package com.teamrocket.protocomm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Advertiser extends AppCompatActivity {

    private static final String TAG = "Advertiser";
    private final String endpointId = "GroundStation";
    private Context c = null;
    private PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            Log.d(TAG, "A: RICEVUTO! ID Payload: "+payload.getId()+". Tipo payload: "+payload.getType());
            Log.d(TAG, "A: Messaggio dice:");
            String str = Arrays.toString(payload.asBytes());
            Log.d(TAG, str);
            Log.d(TAG, "A: *** Fine messaggio ***");
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
            Log.d(TAG, "A: Trasferimento di ("+payloadTransferUpdate.getPayloadId()+").");
            Log.d(TAG, "A: Stato ("+payloadTransferUpdate.getPayloadId()+"): "+payloadTransferUpdate.getStatus());
            double completamento = payloadTransferUpdate.getBytesTransferred()/payloadTransferUpdate.getTotalBytes();
            Log.d(TAG, "A: Completamento: "+completamento*100.0+"%");
        }
    };

    private ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String s, ConnectionInfo connectionInfo) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), "Client vuole collegarsi...", duration);
            toast.show();
            Nearby.getConnectionsClient(getApplicationContext()).acceptConnection("GroundStation", mPayloadCallback);
            Log.d(TAG, "A: Connessione stabilita.");
            Log.d(TAG, "A: String s: "+s);
            Log.d(TAG, "A: Auth Token: "+connectionInfo.getAuthenticationToken());
            Log.d(TAG, "A: EndPoint Name: "+connectionInfo.getEndpointName());
        }

        @Override
        public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Log.d(TAG, "A: OK! Si possono trasferire dati.");
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Log.d(TAG, "A: NOPE! Connessione rifiutata.");
                    break;
                default:
                    Log.d(TAG, "A: Connessione persa prima di avere risposta :(");
                    break;
            }
        }

        @Override
        public void onDisconnected(String s) {
            Log.d(TAG, "A: Disconnessione avvenuta.");
            Log.d(TAG, "A - String s:"+s);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertiser);
        Intent i = getIntent();
        Button restart = findViewById(R.id.restartServer);
        c = this;
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                        /* endpointName= */ "GroundStation",
                        /* serviceId= */ "com.teamrocket.protocomm",
                        mConnectionLifecycleCallback,
                        new AdvertisingOptions(Strategy.P2P_STAR)
                );
            }
        });

        Nearby.getConnectionsClient(this).startAdvertising(
                /* endpointName= */ "GroundStation",
                /* serviceId= */ "com.teamrocket.protocomm",
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR)
        );

    }
}
