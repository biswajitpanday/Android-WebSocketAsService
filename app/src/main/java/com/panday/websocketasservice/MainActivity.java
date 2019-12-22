package com.panday.websocketasservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver updateUIReciver;
    TextView consoleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();
    }

    private void InitView() {
        consoleTextView = findViewById(R.id.consoletv);
        Button startServiceBtn = findViewById(R.id.startService);
        Button stopServiceBtn = findViewById(R.id.stopService);

        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartWebSocketService();
            }
        });
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopWebSocketService();
            }
        });
    }

    private void StartWebSocketService() {
        startService(new Intent(this, WebSocketService.class));
        RegisterWeSocketServiceReceiver();
    }

    private void StopWebSocketService() {
        stopService(new Intent(this, WebSocketService.class));
    }

    private void RegisterWeSocketServiceReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("service.to.activity.transfer");
        updateUIReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                if (intent != null) {
                    String message = intent.getStringExtra("wsdata");
                    consoleTextView.setText(message);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }


            }
        };
        registerReceiver(updateUIReciver, filter);
    }
}
