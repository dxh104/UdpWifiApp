package com.example.xhd.udpwifiapp2.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * Created by XHD on 2019/05/07
 */
public class MultiSendThread extends Thread {
    int port;//端口
    String addressIp;//Ip地址

    public MultiSendThread(String addressIp, int port) {
        this.addressIp = addressIp;
        this.port = port;
    }

    protected LinkedList<byte[]> mRecordQueue;//记录音频组数
    int minBufferSize;//采集音频文件最小的buffer大小
    private AcousticEchoCanceler aec;//回音消除
    private AutomaticGainControl agc;//自动增益控制
    private NoiseSuppressor nc;//噪声抑制
    private AudioRecord audioRec;//音频采集
    private byte[] buffer;//缓存数据

    //-------音频采集-------
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initAudio() {
        //播放的采样频率 和录制的采样频率一样
        int sampleRate = 8000;
        //和录制的一样的
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        //录音用输入单声道
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;

        minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,//-------采样频率-------
                channelConfig,//-------录音用输入单声道-------
                AudioFormat.ENCODING_PCM_16BIT);//-------设置音频数据块是8位还是16位。这里设置为16位。
        System.out.println("****RecordMinBufferSize = " + minBufferSize);
        audioRec = new AudioRecord(
                MediaRecorder.AudioSource.MIC,//-------设定录音来源为主麦克风。
                sampleRate,//-------采样频率-------
                channelConfig,//-------录音用输入单声道-------
                audioFormat,//-------设置音频数据块是8位还是16位。这里设置为16位。
                minBufferSize);//-------设置采集音频文件最小的buffer大小-------
        buffer = new byte[minBufferSize];//-------缓存数据-------

        //-------声学回声消除器 AcousticEchoCanceler 消除了从远程捕捉到音频信号上的信号的作用-------
        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(audioRec.getAudioSessionId());
            if (aec != null) {
                aec.setEnabled(true);
            }
        }

        //-------自动增益控制 AutomaticGainControl 自动恢复正常捕获的信号输出-------
        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(audioRec.getAudioSessionId());
            if (agc != null) {
                agc.setEnabled(true);
            }
        }

        //-------噪声抑制器 NoiseSuppressor 可以消除被捕获信号的背景噪音-------
        if (NoiseSuppressor.isAvailable()) {
            nc = NoiseSuppressor.create(audioRec.getAudioSessionId());
            if (nc != null) {
                nc.setEnabled(true);
            }
        }
        mRecordQueue = new LinkedList<>();
    }

    private DatagramSocket datagramSocket;//UDP协议的Socket

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void run() {
        try {
            initAudio();//-------初始化音频采集器-------
            datagramSocket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            audioRec.startRecording();//-------开始录制-------
            while (true) {
                try {
                    if (mRecordQueue.size() >= 2) {
//                          1.音频数据记录的音频数据写入的阵列
//                          2.音频数据中的offsetinbytes索引，数据从中写入，以字节表示
//                          3. sizeInBytes请求的字节数
                        int length = audioRec.read(buffer, 0, minBufferSize);//一段一段缓存数据到buffer
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, length);//1.数据 2.长度
                        datagramPacket.setAddress(InetAddress.getByName(addressIp));
                        datagramPacket.setPort(port);
                        System.out.println("AudioRTwritePacket = " + datagramPacket.getData().toString());
                        datagramSocket.send(datagramPacket);
                    }
                    if (mRecordQueue.size() < 2) mRecordQueue.add(buffer.clone());//收集部分录音数据（前两段不录）
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
