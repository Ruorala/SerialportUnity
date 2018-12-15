package com.iview.serialportjar;

import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

/**
 * Created by llm on 18-12-10.
 */

public class SerialActivity extends UnityPlayerActivity {

    private static String TAG = "SerialActivity";
    SerialPortUtil serialPortUtil = null;
    boolean bMotorInit = false;

    double previousAlpha = 0;
    double previousBeta = 0;
    int motor1direction;
    int motor2direction;
    int finalAlpha;
    int finalBeta;
    float xAnglePStep = 0.24f;
    float yAnglePStep = 0.4f;
    int motor1time;
    int motor2time;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public void test1() {

    }

    public void initMotorControl() {
        serialPortUtil = new SerialPortUtil();
        serialPortUtil.openSerialPort();
        bMotorInit = true;
    }

    public void releaseMotorControl() {
        if (serialPortUtil != null) {
            serialPortUtil.closeSerialPort();
            serialPortUtil = null;
        }

        bMotorInit = false;
    }

    public int controlMotorMulti(int direction1, int direction2, int step1, int step2, int pps1, int pps2) {

        if (bMotorInit == false) {
            initMotorControl();
        }
        byte data[] = new byte[16];
        data[0] = 0x69;
        data[1] = 0x76;
        data[2] = 0x69;

        data[3] = 0x01;
        data[4] = 0x05;


        data[5] = (byte) direction1;
        data[6] = (byte) step1;
        data[7] = (byte) direction2;
        data[8] = (byte) step2;
        data[9] = 0x00;

        int temp = 0;
        for (int i = 0; i < 10; i++) {
            temp ^= data[i];
        }
        data[10] = (byte) temp;

        data[11] = 0x0c;
        data[12] = (byte) pps1;
        data[13] = (byte) pps2;
        data[14] = (byte) 0xff;
        data[15] = 0x00;

        serialPortUtil.sendSerialPort(data);

        return 0;
    }

    public void controlMotor(String sAlpha, String sBeta, String sTime) {

        Log.e(TAG, "receive controlMotor :" + sAlpha + "," + sBeta + "," + sTime);
        float alpha = Float.parseFloat(sAlpha);
        float beta = Float.parseFloat(sBeta);
        int time = Integer.parseInt(sTime);

        if (bMotorInit == false) {
            initMotorControl();
        }



        motor1direction=0x00;
        motor2direction=0x00;

        if (beta > 0){
            motor1direction = 0x01;
        }
        if (alpha > 0){
            motor2direction= 0x01;
        }


        float alphaVal = (float) alpha / xAnglePStep;
        if (alpha == 0) {
            alphaVal=0;
        }
        float betaVal = (float) beta / yAnglePStep;
        if (beta == 0) {
            betaVal=0;
        }

        finalAlpha=(int)(alphaVal + 0.5);
        finalBeta=(int)(betaVal + 0.5);

        if(betaVal>0){
            if((betaVal-previousBeta) > betaVal){
                finalBeta=Math.abs(finalBeta) + 6;
                Log.e(TAG, "in : betaPos[ipos]>0" + finalAlpha +";" );
            }
        }else if(betaVal<0){
            if((betaVal-previousBeta) < betaVal){
                finalBeta=Math.abs(finalBeta) + 6;
                Log.e(TAG, "in : betaPos[ipos]>0" + finalAlpha +";" );

            }
        }
        finalAlpha=Math.min( Math.abs(finalAlpha), 255);
        finalBeta= Math.min( Math.abs(finalBeta), 255);

        if (time == 0) {
            motor1time = 0;
            motor2time = 0;
        } else {
            motor1time =  4 * finalBeta * 1000 / (int) time;
            motor2time =  4 * finalAlpha * 1000 / (int) time;
        }

        serialPortUtil.setMotorRuning(finalAlpha, finalBeta, true);

        Log.e(TAG, "direction1 :" + motor1direction + ",direction2:" + motor2direction + ", step1:" + finalBeta + ", step2:" + finalAlpha + ", pps1:" + motor1time + ",pps2:" + motor2time);
        controlMotorMulti(motor1direction, motor2direction, finalBeta, finalAlpha, motor1time , motor2time);

        previousAlpha=alphaVal;
        previousBeta=betaVal;
/*
        while (serialPortUtil.isMotorRuning()) {
            Log.e(TAG, " motoris running");
        }*/
    }

    boolean isbMotorRuning() {
        return serialPortUtil.isMotorRuning();
    }

    boolean rTrue() {
        return true;
    }

    boolean rFalse() {
        return false;
    }
}
