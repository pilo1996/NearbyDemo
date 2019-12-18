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
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.nearby.messages.NearbyPermissions;

import java.io.Serializable;

public class Discoverer extends AppCompatActivity {
    private Context c;
    private static final String TAG = "Discoverer";
    private final String EndpointId = "GroundStation";
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            Log.d(TAG, "D: RICEVUTO! ID Payload: "+payload.getId()+". Tipo payload: "+payload.getType());
            Log.d(TAG, "D: Messaggio dice:");
            String str = payload.asBytes().toString();
            Log.d(TAG, str);
            Log.d(TAG, "D: *** Fine messaggio ***");
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
            Log.d(TAG, "D: Trasferimento di ("+payloadTransferUpdate.getPayloadId()+").");
            Log.d(TAG, "D: Stato ("+payloadTransferUpdate.getPayloadId()+"): "+payloadTransferUpdate.getStatus());
            double completamento = payloadTransferUpdate.getBytesTransferred()/payloadTransferUpdate.getTotalBytes();
            Log.d(TAG, "D: Completamento: "+completamento*100.0+"%");
        }
    };

    private ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String s, ConnectionInfo connectionInfo) {
            Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(connectionInfo.getEndpointName(), mPayloadCallback);
            Log.d(TAG, "D: Connessione stabilita.");
            Log.d(TAG, "D: String s: "+s);
            Log.d(TAG, "D: Auth Token: "+connectionInfo.getAuthenticationToken());
            Log.d(TAG, "D: EndPoint Name: "+connectionInfo.getEndpointName());
        }

        @Override
        public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Log.d(TAG, "Discover: OK! Si possono trasferire dati.");
                    welcome();
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Log.d(TAG, "Discover: NOPE! Connessione rifiutata.");
                    break;
                default:
                    Log.d(TAG, "Discover: Connessione persa prima di avere risposta :(");
                    break;
            }
        }

        @Override
        public void onDisconnected(String s) {
            Log.d(TAG, "Discover: Disconnessione avvenuta.");
            Log.d(TAG, "Discover - String s:"+s);
        }
    };


    private EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String s, DiscoveredEndpointInfo discoveredEndpointInfo) {
            if(EndpointId.equals(discoveredEndpointInfo.getEndpointName())){
                Log.d(TAG, "PROVO A CONNETTERMI. s: "+s);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), "Connetto... ("+s+")", duration);
                toast.show();
                Nearby.getConnectionsClient(getApplicationContext()).requestConnection(
                        EndpointId,
                        "com.teamrocket.protocomm",
                        mConnectionLifecycleCallback
                );
            }
        }

        @Override
        public void onEndpointLost(String s) {
            Log.d(TAG, "D: boh perso endpoint cose... "+s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discoverer);
        Intent i = getIntent();
        Button restart = findViewById(R.id.restartDiscovery);
        c = this;

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(
                        /* serviceId= */ "com.teamrocket.protocomm",
                        mEndpointDiscoveryCallback,
                        new DiscoveryOptions(Strategy.P2P_STAR)
                );
            }
        });

        Nearby.getConnectionsClient(this).startDiscovery(
                        /* serviceId= */ "com.teamrocket.protocomm",
                        mEndpointDiscoveryCallback,
                        new DiscoveryOptions(Strategy.P2P_STAR)
        );

    }

    public void welcome(){
        String benvenuto = "Benvenuto sono TEAM ROCKET";
        byte[] serial = benvenuto.getBytes();
        Nearby.getConnectionsClient(this).sendPayload(EndpointId, Payload.fromBytes(serial));
    }
}
