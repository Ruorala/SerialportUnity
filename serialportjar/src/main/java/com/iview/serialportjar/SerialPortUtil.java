package com.iview.serialportjar;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * Created by llm on 18-12-4.
 */

public class SerialPortUtil {
    private static String TAG = "SerialPortUti";
    private SerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private ReceiveThread mReceiveThread = null;
    private boolean isStart = false;
    private boolean isMotorRuning = false;
    private boolean isMotor1Runing = false;
    private boolean isMotor2Runing = false;
    float motor1 = 0;
    float motor2 = 0;

    public void openSerialPort() {
        try {
            serialPort = new SerialPort(new File("/dev/ttyS4"), 38400, 0);

            inputStream = serialPort.getInputStream();
            if (inputStream == null) {
                Log.e(TAG, "inputStream is null");
            }
            outputStream = serialPort.getOutputStream();

            isStart = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        getSerialPort();
    }

    public void closeSerialPort() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSerialPort (byte[] data) {
        try {
            for (int i = 0; i < data.length; i++) {
                Log.e(TAG, "Send Serial data byte[" + i + "] = " + Integer.toHexString(data[i] & 0xff));
            }

            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSerialPort() {
        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread();
        }
        mReceiveThread.start();
    }

    private class ReceiveThread  extends Thread {
        @Override
        public void run() {
            Log.e(TAG, "start ReceiveThread");
            while (isStart) {
                Log.e(TAG, "isStart");
                if (inputStream == null) {
                    Log.e(TAG, "Input Stream is null");
                    return;
                }

                byte[] readData = new byte[1024];
                try {
                    int size = inputStream.read(readData);
                    Log.e(TAG, "ReceiveThread size = " + size);
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            Log.e(TAG, "read Serial data byte[" + i + "] = " + Integer.toHexString(readData[i] & 0xff));
                        }

                        if (readData[0] == 1) {
                            isMotor1Runing = false;
                        }

                        if (readData[0] == 2) {
                            isMotor2Runing = false;
                        }

                        if (isMotor1Runing == false && isMotor2Runing == false) {
                            isMotorRuning = false;
                        }

                        Log.e(TAG, "receive serial : " + isMotor1Runing + "," + isMotor2Runing + "," + isMotorRuning);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setMotorRuning(float alpha, float beta, boolean bRuning) {
        isMotorRuning = bRuning;
        isMotor1Runing = bRuning;
        isMotor2Runing = bRuning;

        if (beta == 0) {
            isMotor1Runing = false;
        }

        if (alpha == 0) {
            isMotor2Runing = false;
        }

        if (alpha == 0 && beta == 0) {
            isMotorRuning = false;
            isMotor1Runing = false;
            isMotor2Runing = false;
        }

        Log.e(TAG, "setMotorRuning : " + isMotor1Runing + "," + isMotor2Runing + "," + isMotorRuning);
    }

    public boolean isMotorRuning() {
        Log.e(TAG, "isMotorRuning :" + isMotorRuning);
        return isMotorRuning;
    }
}
