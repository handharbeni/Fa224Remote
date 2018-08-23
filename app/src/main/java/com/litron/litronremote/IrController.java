package com.litron.litronremote;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

public class IrController {

    private ConsumerIrManager irService;

    private AudioTrack irAudioTrack = null;
    private static final int SAMPLERATE = 48000;

    private Context mContext;


    IrController(Context context) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            irService = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendCode(int[] code) {
        if (irService.hasIrEmitter()){
            for (int i:code){
                int[] codeTransmit = new int[34];
                for (int c=0;c<codeTransmit.length;c=c+2){
                    codeTransmit[c] = 1;
                    codeTransmit[c+1] = 3333;
                }
                int is = 2;
                codeTransmit[0]=3333;
                codeTransmit[1]=1;
                for (int ix=0;ix<=7;ix++){
                    if ((i&1)==1){
                        codeTransmit[is] = 1;
                        is++;
                        codeTransmit[is] = 3333;
                        is++;
                    }else{
                        codeTransmit[is] = 3333;
                        is++;
                        codeTransmit[is] = 1;
                        is++;
                    }
                    i = (byte) (i >> (byte)1);
                }
                irService.transmit(38000, codeTransmit);
            }
        }else{
            Toast.makeText(mContext, "Tidak terdapat Infrared dalam device anda", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendUsingAudio(byte[] hexa){

        int bufSize;
            int[] p = new int[]{3333,3333,3333,3333,3333,3333,3333,3333,3333};
            int cf = 38000;

        byte[] pAudio = new byte[12000 * 9];
            double cfFactor = 0.5d;
            bufSize = msToAudio((int) Math.round(cfFactor * ((double) cf)), p, pAudio, hexa);
            if (this.irAudioTrack != null) {
                this.irAudioTrack.flush();
                this.irAudioTrack.release();
            }
            this.irAudioTrack = new AudioTrack(3, SAMPLERATE, 3, 3, bufSize, 0);
            this.irAudioTrack.write(pAudio, 0, bufSize);
            this.irAudioTrack.setStereoVolume(1000.0f, 1000.0f);
            this.irAudioTrack.play();
    }
    private int msToAudio(int cf, int[] signals, byte[] outptr, byte[] datas) {
        int j = 0;
        for (byte dt : datas){
            boolean signalPhase = false;
            double carrierPosRad = 0.0d;
            double carrierStepRad = ((((double) cf) * 3.141592653589793d) * 2.0d) / 48000.0d;
            while (j < 4800) {
                outptr[j] = Byte.MIN_VALUE;
                j++;
            }
            int i = 0;

            while (i < signals.length) {
                if (i == 0){
                    signalPhase = true;
                }else{
                    if ((dt & 1) == 1){
                        signalPhase = false;
                    }else{
                        signalPhase = true;
                    }
                    dt = (byte) (dt >> (byte)1);
                }
                int j2 = j;
                for (int currentSignal = signals[i];
                     currentSignal > 0;
                     currentSignal = (int) (((double) currentSignal) - 20.833333333333332d)) {
                    int out;
                    if (signalPhase) {
                        out = (int) Math.round((Math.sin(carrierPosRad) * 127.0d) + 128.0d);
                    } else {
                        out = 128;
                    }
                    j = j2 + 1;
                    outptr[j2] = (byte) out;
                    j2 = j + 1;
                    outptr[j] = (byte) (256 - out);
                    carrierPosRad += carrierStepRad;
                }
                i++;
                j = j2;
            }
            i = 0;
            while (i < signals.length) {
                int j2 = j;
                for (int currentSignal = signals[i];
                     currentSignal > 0;
                     currentSignal = (int) (((double) currentSignal) - 20.833333333333332d)) {
                    int out;
                    out = 128;
                    j = j2 + 1;
                    outptr[j2] = (byte) out;
                    j2 = j + 1;
                    outptr[j] = (byte) (256 - out);
                    carrierPosRad += carrierStepRad;
                }
                i++;
                j = j2;
            }
        }
        return j;
    }
}
