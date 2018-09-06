package com.litron.litronremote;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.media.AudioFormat;
import android.media.AudioManager;
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

    byte JumlahByte = 19;   //0xAA,jeda,jam,jeda,menit,jeda....ceksum = 15byte(8 data + 6 jeda)
    int SampleRate = 44100;
    byte TotalByte = 16;   //0xAA,jeda,jam,jeda,menit,jeda....ceksum,jeda = 16byte(8 data + 7 jeda)
    byte JumlahBit = 9;
    double freqHz = 19000;
    int durationuS = 3333;
    int TotalBuffer = (int)(((44100.0 * 2 * (durationuS / 1000000.0))*JumlahBit)*TotalByte) & ~1;
    int BufferPerByte = TotalBuffer/TotalByte;
    int idx = 0;
    byte ke = 0;
    byte[] samples = new byte[12000 * 9];

    public void newMethod(){
//        int durationuS = 1500;
        int count = (int)(((44100.0 * 2.0 * (durationuS / 1000000.0))*JumlahBit)*JumlahByte) & ~1;
        byte[] samples = new byte[count];
        for(int i = 0; i < count; i += 2){
//            short sample = (short)(Math.sin(2 * Math.PI * i / (SampleRate / freqHz)) * 0x7FFF);
            switch (ke){
                case 0:
//                    start
                    samples[i + 0] = (byte)255;
                    samples[i + 1] = (byte)127;
//                    samples[i + 1] = (byte)~255;
                    ke = 1;
                    break;
                case 1 :
//                    middle
                    samples[i + 0] = (byte)127;
                    samples[i + 1] = (byte)~255;
//                    samples[i + 1] = (byte)127;
                    ke = 2;
                    break;
                case 2 :
//                    end
                    samples[i + 0] = (byte)~255;
                    samples[i + 1] = (byte)255;
                    ke = 0;
                    break;
            }
        }
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
                count, AudioTrack.MODE_STATIC);
        track.write(samples, 0, count);
        track.play();
    }

    public void newMethod(byte[] data, boolean invers){
        byte d;
        if (invers){
            d = (byte) 255;
        }else{
            d = (byte) ~255;
        }
//        if (!audioManager.isWiredHeadsetOn()){
//            Toast.makeText(mContext, "Belum Ada Perangkat IR External pada Device Anda", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        adjustVolume(true);
        int count = (int)(((44100.0 * 2.0 * (durationuS / 1000000.0))*JumlahBit)*JumlahByte) & ~1;
        byte[] samples = new byte[count];
        int idx = 0;
        for (byte datas : data){
            for(int k = 0; k < count/(9*JumlahByte); k += 2){
                samples[idx + 0] = (byte)d;
                samples[idx + 1] = (byte)d;
                idx+=2;
            }
            for (int j=0;j<8;j++){
                for(int i = 0; i < count/(9*JumlahByte); i += 2){
                    if ((datas & 1)==1){
                        samples[idx + 0] = (byte)~d;
                        samples[idx + 1] = (byte)~d;
                    }else{
                        samples[idx + 0] = (byte)d;
                        samples[idx + 1] = (byte)d;
                    }
                    idx += 2;
                }
                datas = (byte) (datas >> (byte)1);

            }
            for(int i = 0; i < count/(JumlahByte); i += 2){
                samples[idx + 0] = (byte) ~d;
                samples[idx + 1] = (byte) ~d;
                idx += 2;
            }
        }
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
                count, AudioTrack.MODE_STATIC);
        track.write(samples, 0, count);
        track.play();
    }
}
