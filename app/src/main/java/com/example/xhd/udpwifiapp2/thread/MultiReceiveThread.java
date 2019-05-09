package com.example.xhd.udpwifiapp2.thread;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by XHD on 2019/05/07
 */
public class MultiReceiveThread extends Thread {

    private int port;//端口

    public MultiReceiveThread(int port) {
        this.port = port;
    }

    private DatagramSocket datagramSocket;//UDP协议的Socket
    private byte[] buffer;//缓存数据
    private AudioTrack audioTrk; //音频跟踪器

    private void initAudioTracker() {
        //扬声器播放
        int streamType = AudioManager.STREAM_MUSIC;//当前3(0-7)
        //播放的采样频率 和录制的采样频率一样
        int sampleRate = 8000;
        //和录制的一样的
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        //流模式
        int mode = AudioTrack.MODE_STREAM;
        //录音用输入单声道  播放用输出单声道
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int recBufSize = AudioTrack.getMinBufferSize(
                sampleRate,//-------采样率，每秒8000个采样点-------
                channelConfig,//-------录音用输入单声道-------
                audioFormat);//-------设置音频数据块是8位还是16位。这里设置为16位。
        System.out.println("****playRecBufSize = " + recBufSize);
        audioTrk = new AudioTrack(
                streamType,// -------指定流的类型--->当前3(0-7)-------
                sampleRate,// -------设置音频数据的采样率，假设是44.1k就是8000-------
                channelConfig,// -------设置输出声道为双声道立体声，而CHANNEL_OUT_MONO类型是单声道(录音用输入单声道  播放用输出单声道)-------
                audioFormat,// -------设置音频数据块是8位还是16位。这里设置为16位。
                recBufSize,//-------根据要播放的音频文件的参数获得最小的buffer大小-------
                mode);//-------流模式-------
        audioTrk.setStereoVolume(AudioTrack.getMaxVolume(),
                AudioTrack.getMaxVolume());//-------设置音量-------
        buffer = new byte[recBufSize];//-------缓存数据-------

    }

    @Override
    public void run() {
        super.run();
        try {
            initAudioTracker();//-------初始化音频追踪器-------
            datagramSocket = new DatagramSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioTrk.play(); //-------从文件流读数据-------
        while (true) {
            try {
                //-------数据报-------
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                // 接收数据，同样会进入阻塞状态
                datagramSocket.receive(datagramPacket);
                audioTrk.write(datagramPacket.getData(), 0, datagramPacket.getLength());//写入音频数据
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
