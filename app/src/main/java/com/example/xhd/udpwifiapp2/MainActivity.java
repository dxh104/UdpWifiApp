package com.example.xhd.udpwifiapp2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xhd.udpwifiapp2.thread.MultiReceiveThread;
import com.example.xhd.udpwifiapp2.thread.MultiSendThread;
import com.example.xhd.udpwifiapp2.util.NetWorkUtil;

public class MainActivity extends AppCompatActivity {

    private TextView tvIp;
    private EditText etPort;
    private Button btnSend;
    private Button btnReceive;
    private MultiSendThread multiSendThread;
    private MultiReceiveThread multiReceiveThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)//6.0权限
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
                            , Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH},
                    1);
        }
        tvIp.setText("本地ip"+NetWorkUtil.getIPAddress(this));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etPort.getText().toString().trim())) {
                    btnSend.setText("录音中...");
                    btnSend.setEnabled(false);
                    if (multiSendThread == null) {
                        multiSendThread = new MultiSendThread(etPort.getText().toString().trim(),10001);
                    }
                    multiSendThread.start();
                } else {
                    Toast.makeText(MainActivity.this, "请输入对方ip", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (multiReceiveThread == null) {
                    multiReceiveThread = new MultiReceiveThread(10001);
                }
                multiReceiveThread.start();
                btnReceive.setText("监听对方语音中...");
                btnReceive.setEnabled(false);
            }
        });
    }

    private void initView() {
        tvIp = (TextView) findViewById(R.id.tvIp);
        etPort = (EditText) findViewById(R.id.etPort);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnReceive = (Button) findViewById(R.id.btnReceive);
    }
}
