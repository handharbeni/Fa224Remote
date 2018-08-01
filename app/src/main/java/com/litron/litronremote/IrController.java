package com.litron.litronremote;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static android.content.ContentValues.TAG;

public class IrController {

    private ConsumerIrManager irService;
//    private AudioManager audioManager;
    private LircRemote currentRemote = null;

    private AudioTrack irAudioTrack = null;
    private AudioTrack track;
    private static final int SAMPLERATE = 48000;

    private Context mContext;

//    private int max, current;



    IrController(Context context) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            irService = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
        }
//        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        setData();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendCode(int[] code) {
        Log.d(TAG, "sendCode: UsingSendCode");
        if (irService.hasIrEmitter()){
            Log.d(TAG, "sendCode: UsingSendCode Have IrEmitter");

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

    public void sendCodeAudioOnly(int[] code){
        Log.d(TAG, "sendCode: UsingSendCodeAudioOnly");
        byte[] datas = new byte[]{
                (byte) code[0],
                (byte) code[1],
                (byte) code[2],
                (byte) code[3],
                (byte) code[4],
                (byte) code[5],
                (byte) code[6],
                (byte) code[7]
        };

        sendUsingAudio(datas);
    }

    public void sendCodeAudioCustom(int[] code){
        Log.d(TAG, "sendCode: UsingSendCodeAudioOnly");
        byte[] datas = new byte[]{
                (byte) code[0],
                (byte) code[1],
                (byte) code[2],
                (byte) code[3],
                (byte) code[4],
                (byte) code[5],
                (byte) code[6],
                (byte) code[7]
        };

        sendUsingAudioCustom(datas);
    }


    int bufSize;
    byte[] pAudio;
    private void setData(){
        LircRemote lircRemote = new LircRemote();
        lircRemote.setFrequency(44100);
        lircRemote.setFlagRc5(true);
        this.currentRemote = lircRemote;
    }
    private void sendUsingAudio(byte[] hexa){

            bufSize = 0;
            int[] p = new int[]{3333,3333,3333,3333,3333,3333,3333,3333,3333};
            int cf = 38000;

            pAudio = new byte[12000 * 9];
            double cfFactor = 0.5d;
            bufSize = msToAudio((int) Math.round(cfFactor * ((double) cf)), p, pAudio, hexa);
            if (this.irAudioTrack != null) {
                this.irAudioTrack.flush();
                this.irAudioTrack.release();
            }
//            this.irAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
//                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
//                    bufSize * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
            this.irAudioTrack = new AudioTrack(3, SAMPLERATE, 3, 3, bufSize, 0);
            this.irAudioTrack.write(pAudio, 0, bufSize);
            this.irAudioTrack.setStereoVolume(1000.0f, 1000.0f);
            this.irAudioTrack.play();
    }
    private void sendUsingAudioCustom(byte[] hexa){

        bufSize = 0;
        int[] p = new int[]{3333,3333,3333,3333,3333,3333,3333,3333,3333};
        int cf = 19000;

        pAudio = new byte[12000 * 9 * 2];
        double cfFactor = 0.5d;
        bufSize = msToAudioCustom((int) Math.round(cfFactor * ((double) cf)), p, pAudio, hexa);
        Log.d(TAG, "sendUsingAudio: BuffSize in MS " + bufSize);
        Log.d(TAG, "sendUsingAudio: BuffSize Length " + pAudio.length);
        if (this.irAudioTrack != null) {
            this.irAudioTrack.flush();
            this.irAudioTrack.release();
        }
        this.irAudioTrack = new AudioTrack(3, SAMPLERATE, AudioFormat.CHANNEL_OUT_STEREO, 2, bufSize, 0);
        this.irAudioTrack.write(pAudio, 0, bufSize);
        this.irAudioTrack.setStereoVolume(1000.0f, 1000.0f);
        this.irAudioTrack.play();
    }
    private void adjustVolume(boolean isPlay){
        if (isPlay){
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_PLAY_SOUND);
//            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//            Log.d(TAG, "adjustVolume: Playing "+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }else{
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, AudioManager.FLAG_PLAY_SOUND);
//            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
//            Log.d(TAG, "adjustVolume: Stopping "+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
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
    private int msToAudioCustom(int cf, int[] signals, byte[] outptr, byte[] datas) {

        int j = 0;
        for (byte dt : datas){
            boolean signalPhase = false;
            double carrierPosRad = 0.0d;
            double carrierStepRad = ((((double) cf) * 3.141592653589793d) * 2.0d) / 44100.0d;
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
                        out = (int) Math.round((Math.sin(carrierPosRad) * 32767.0d) + 32768.0d);
                    } else {
                        out = 32768;
                    }
                    j = j2 + 1;
                    outptr[j2] = (byte) out;
                    j2 = j + 1;
                    outptr[j] = (byte) (65536 - out);
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
                    out = 32768;
                    j = j2 + 1;
                    outptr[j2] = (byte) out;
                    j2 = j + 1;
                    outptr[j] = (byte) (65536 - out);
                    carrierPosRad += carrierStepRad;
                }
                i++;
                j = j2;
            }
        }
        return j;
    }
    byte TotalByte = 16;   //0xAA,jeda,jam,jeda,menit,jeda....ceksum,jeda = 16byte(8 data + 7 jeda)
    byte JumlahBit = 8;
    double freqHz = 19000;
    int durationuS = 1500;
    int TotalBuffer = (int)(((44100.0 * 2 * (durationuS / 1000000.0))*JumlahBit)*TotalByte) & ~1;
    int BufferPerByte = TotalBuffer/TotalByte;
    int idx = 0;
    short[] samples = new short[12000 * 9];

    public void sendUsingAudio16Bit(byte[] datas){
        TotalBuffer = (int)(((44100.0 * 2 * (durationuS / 1000000.0))*JumlahBit)*TotalByte) & ~1;
        TotalBuffer *= 2;
        TotalBuffer += 20;
        TotalBuffer *= 2;
        int[] p = new int[]{durationuS,durationuS,durationuS,durationuS,durationuS,durationuS,durationuS,durationuS,durationuS};
        boolean signalPhase;
        int j = 0;
        for (byte dt :datas){
            int i = 0;
            double carrierPosRad = 0.0d;
            int cf = (int) Math.round(0.5d * ((double) 19000));
            double carrierStepRad = ((((double) cf) * 3.141592653589793d) * 2.0d) / 44100.0d;
            while (j < 4800) {
                samples[j] = Byte.MIN_VALUE;
                j++;
            }

            while (i < p.length) {
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
                for (int currentSignal = p[i];
                     currentSignal > 0;
                     currentSignal = (int) (((double) currentSignal) - 20.833333333333332d)) {
                    int out;
                    if (signalPhase) {
                        out = (int) Math.round((Math.sin(carrierPosRad) * 127.0d) + 128.0d);
                    } else {
                        out = 128;
                    }
                    j = j2 + 1;
                    samples[j2] = (byte) out;
                    j2 = j + 1;
                    samples[j] = (byte) (256 - out);
                    carrierPosRad += carrierStepRad;
                }
                i++;
                j = j2;
            }
            i = 0;
            while (i < p.length) {
                int j2 = j;
                for (int currentSignal = p[i];
                     currentSignal > 0;
                     currentSignal = (int) (((double) currentSignal) - 20.833333333333332d)) {
                    int out;
                    out = 128;
                    j = j2 + 1;
                    samples[j2] = (byte) out;
                    j2 = j + 1;
                    samples[j] = (byte) (256 - out);
                    carrierPosRad += carrierStepRad;
                }
                i++;
                j = j2;
            }
        }
        if (track != null){
            track.flush();
            track.release();
        }
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
                TotalBuffer, AudioTrack.MODE_STATIC);
        track.write(samples, 0, TotalBuffer);
        track.setStereoVolume(1000.0f, 1000.0f);
        try{

            track.play();
        }catch (Exception e){
            track.flush();
            track.release();
        }
    }


    public interface IrRemote {
        int getCarrierFrequency();

        Set<Integer> getKeys();

        int[] getPattern(int i);
    }

    private class IrRemoteBaseImpl implements IrRemote {
        protected int freq;
        protected Map<Integer, int[]> keys;

        private IrRemoteBaseImpl() {
            this.keys = new HashMap();
        }

        public int[] getPattern(int key) {
            return (int[]) this.keys.get(Integer.valueOf(key));
        }

        public Set<Integer> getKeys() {
            return this.keys.keySet();
        }

        public int getCarrierFrequency() {
            return this.freq;
        }
    }
    private class LircRemote extends IrRemoteBaseImpl {
        int bits;
        boolean constLength;
        int gap;
        int minRepeats;
        int phead;
        int plead;
        int pone;
        int pre_data;
        int pre_data_bits;
        int ptrail;
        int pzero;
        boolean rc5;
        int repeatGap;
        int shead;
        int sone;
        int szero;
        int toggle_bit;
        int toggle_mask;

        LircRemote() {
            super();
            this.rc5 = false;
            this.constLength = false;
            this.bits = 0;
            this.minRepeats = 0;
            this.phead = -1;
            this.shead = -1;
            this.plead = -1;
            this.pone = -1;
            this.sone = -1;
            this.pzero = -1;
            this.szero = -1;
            this.ptrail = -1;
            this.pre_data_bits = 0;
            this.pre_data = -1;
            this.gap = -1;
            this.repeatGap = -1;
            this.toggle_mask = 0;
            this.toggle_bit = -1;
            this.freq = 38000;
        }

        public void setFlagRc5(boolean flag) {
            this.rc5 = flag;
        }

        public void setFlagConstLength(boolean flag) {
            this.constLength = flag;
        }

        public void setFrequency(int frequency) {
            this.freq = frequency;
        }

        public void setBits(int nBits) {
            this.bits = nBits;
        }

        public void setMinRepeat(int nRepeats) {
            this.minRepeats = nRepeats;
        }

        public void setHeaderCycles(int pulseCycles, int spaceCycles) {
            this.phead = convFromCycles(pulseCycles);
            this.shead = convFromCycles(spaceCycles);
        }

        public void setOneCycles(int pulseCycles, int spaceCycles) {
            this.pone = convFromCycles(pulseCycles);
            this.sone = convFromCycles(spaceCycles);
        }

        public void setPLeadCycles(int pulseCycles) {
            this.plead = convFromCycles(pulseCycles);
        }

        public void setZeroCycles(int pulseCycles, int spaceCycles) {
            this.pzero = convFromCycles(pulseCycles);
            this.szero = convFromCycles(spaceCycles);
        }

        public void setPTrailCycles(int pulseCycles) {
            this.ptrail = convFromCycles(pulseCycles);
        }

        public void setGapCycles(int spaceCycles) {
            this.gap = convFromCycles(spaceCycles);
        }

        public void setRepeatGapCycles(int spaceCycles) {
            this.repeatGap = convFromCycles(spaceCycles);
        }

        public void setHeaderMs(int pulseMs, int spaceMs) {
            this.phead = convFromMs(pulseMs);
            this.shead = convFromMs(spaceMs);
        }

        public void setPLeadMs(int pulseMs) {
            this.plead = convFromMs(pulseMs);
        }

        public void setOneMs(int pulseMs, int spaceMs) {
            this.pone = convFromMs(pulseMs);
            this.sone = convFromMs(spaceMs);
        }

        public void setZeroMs(int pulseMs, int spaceMs) {
            this.pzero = convFromMs(pulseMs);
            this.szero = convFromMs(spaceMs);
        }

        public void setPTrailMs(int pulseMs) {
            this.ptrail = convFromMs(pulseMs);
        }

        public void setGapMs(int spaceMs) {
            this.gap = convFromMs(spaceMs);
        }

        public void setRepeatGapMs(int spaceMs) {
            this.repeatGap = convFromMs(spaceMs);
        }

        public void setPreDataBits(int nBits) {
            this.pre_data_bits = nBits;
        }

        public void setPreData(int code) {
            this.pre_data = code;
        }

        public void setToggleMask(int bitmask) {
            this.toggle_mask = bitmask;
        }

        public void setToggleBit(int bit) {
            this.toggle_bit = bit;
        }

        public void addKey(int key, int code) {
            if (this.rc5) {
                addRc5Key(key, code);
                return;
            }
            int i;
            int length = 0;
            if (!(this.phead == -1 || this.shead == -1)) {
                length = 0 + 2;
            }
            length = (length + (this.pre_data_bits * 2)) + (this.bits * 2);
            if (!((this.gap == -1 && this.repeatGap == -1) || this.ptrail == -1)) {
                length += 2;
            }
            if (this.toggle_mask != 0) {
                length += this.bits * 2;
                if (!((this.gap == -1 && this.repeatGap == -1) || this.ptrail == -1)) {
                    length += 2;
                }
            } else if (this.minRepeats > 0) {
                length *= this.minRepeats + 1;
            }
            int[] sequence = new int[length];
            int i2 = 0;
            if (!(this.phead == -1 || this.shead == -1)) {
                sequence[0] = this.phead;
                i2 = 0 + 1;
                sequence[i2] = this.shead;
                i2++;
            }
            i2 = setCode(code, this.bits, setCode(this.pre_data, this.pre_data_bits, i2, sequence), sequence);
            if (this.ptrail != -1) {
                sequence[i2] = this.ptrail;
                i2++;
            }
            if (this.gap != -1 || (this.repeatGap != -1 && this.minRepeats > 0)) {
                if (this.ptrail != -1) {
                    if (this.repeatGap == -1 || this.minRepeats <= 0) {
                        sequence[i2] = sequence[i2] + this.gap;
                    } else {
                        sequence[i2] = sequence[i2] + this.repeatGap;
                    }
                    i2++;
                } else if (i2 - 1 > 0) {
                    if (this.repeatGap == -1 || this.minRepeats <= 0) {
                        i = i2 - 1;
                        sequence[i] = sequence[i] + this.gap;
                    } else {
                        i = i2 - 1;
                        sequence[i] = sequence[i] + this.repeatGap;
                    }
                }
            }
            if (this.toggle_mask != 0) {
                i2 = setCode(code ^ this.toggle_mask, this.bits, i2, sequence);
                if (this.ptrail != -1) {
                    sequence[i2] = this.ptrail;
                    i2++;
                }
                if (this.gap != -1) {
                    if (this.ptrail != -1) {
                        sequence[i2] = this.gap;
                        i2++;
                    } else if (i2 - 1 > 0) {
                        i = i2 - 1;
                        sequence[i] = sequence[i] + this.gap;
                    }
                }
            } else if (this.minRepeats > 0) {
                for (int r = 0; r < this.minRepeats; r++) {
                    System.arraycopy(sequence, 0, sequence, (r + 1) * i2, i2);
                }
                if (!(this.gap == -1 || this.repeatGap == -1)) {
                    int p = sequence.length - 1;
                    sequence[p] = (sequence[p] - this.repeatGap) + this.gap;
                }
            }
            this.keys.put(Integer.valueOf(key), sequence);
        }

        private void addRc5Key(int key, int code) {
            boolean lastOne;
            Vector<Integer> vector = new Vector();
            if (((1 << (this.bits - 1)) & code) == 1) {
                vector.add(Integer.valueOf(this.plead));
                vector.add(Integer.valueOf(this.sone));
                lastOne = true;
            } else {
                vector.add(Integer.valueOf(this.plead + this.pzero));
                lastOne = false;
            }
            for (int nBits = this.bits - 2; nBits >= 0; nBits--) {
                if (((1 << nBits) & code) == 1) {
                    if (lastOne) {
                        vector.add(Integer.valueOf(this.pone));
                        vector.add(Integer.valueOf(this.sone));
                    } else {
                        vector.add(Integer.valueOf(this.szero + this.sone));
                    }
                    lastOne = true;
                } else {
                    if (lastOne) {
                        vector.add(Integer.valueOf(this.pone + this.pzero));
                    } else {
                        vector.add(Integer.valueOf(this.szero));
                        vector.add(Integer.valueOf(this.pzero));
                    }
                    lastOne = false;
                }
            }
            if (lastOne) {
                vector.add(Integer.valueOf(this.pone));
            }
            int s = vector.size();
            int sum = 0;
            int[] sequence = new int[(s + 1)];
            while (0 < s) {
                sequence[0] = ((Integer) vector.get(0)).intValue();
                sum += sequence[0];
                s++;
            }
            if (this.gap == -1) {
                sequence[s] = this.szero;
            } else if (this.constLength) {
                int g = this.gap - sum;
                if (g > 0) {
                    sequence[s] = g;
                } else {
                    sequence[s] = this.szero;
                }
            } else {
                sequence[s] = this.gap;
            }
        }

        private int setCode(int code, int nBits, int index, int[] sequence) {
            for (nBits--; nBits >= 0; nBits--) {
                if (((1 << nBits) & code) == 0) {
                    sequence[index] = this.pzero;
                    index++;
                    sequence[index] = this.szero;
                    index++;
                } else {
                    sequence[index] = this.pone;
                    index++;
                    sequence[index] = this.sone;
                    index++;
                }
            }
            return index;
        }

        private int convFromMs(int pulseMs) {
            return pulseMs;
        }

        private int convFromCycles(int pulseCycles) {
            return (int) Math.round((((double) pulseCycles) * 1000000.0d) / ((double) this.freq));
        }

        public void addRawKey(int key, int[] code) {
            int[] sequence = new int[code.length];
            for (int i = 0; i < code.length; i++) {
                sequence[i] = convFromMs(code[i]);
            }
            this.keys.put(Integer.valueOf(key), sequence);
        }
    }
}
