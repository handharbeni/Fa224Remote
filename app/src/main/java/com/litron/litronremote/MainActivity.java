package com.litron.litronremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.suke.widget.SwitchButton;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private IrController ir;

    private static String LABEL_SEPARATOR = ":";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private static String LabelPutarLayar = "PUTAR_LAYAR";


    @BindView(R.id.btnKirim)
    AppCompatButton btnKirim;

    @BindView(R.id.JamOn)
    WheelPicker JamOn;

    @BindView(R.id.JamOff)
    WheelPicker JamOff;

    @BindView(R.id.MenitOn)
    WheelPicker MenitOn;

    @BindView(R.id.MenitOff)
    WheelPicker MenitOff;

    @BindView(R.id.choose_ir)
    SwitchButton choose_ir;

    private int max, current;



    int[] a = new int[9];

    private static String S_WAKTU_ON = "WaktuON";
    private static String S_WAKTU_OFF = "WaktuOFF";
    private static String S_SWITCH_BUTTON = "SwitchBUTTON";

    private AudioManager audioManager;


    public String getsWaktuOn() {
        return sharedPreferences.getString(S_WAKTU_ON, "17:59");
    }

    public void setsWaktuOn(String sWaktuOn) {
        editor = sharedPreferences.edit();
        editor.putString(S_WAKTU_ON, sWaktuOn);
        editor.apply();
    }

    public String getsWaktuOff() {
        return sharedPreferences.getString(S_WAKTU_OFF, "04:59");
    }

    public void setsWaktuOff(String sWaktuOff) {
        editor = sharedPreferences.edit();
        editor.putString(S_WAKTU_OFF, sWaktuOff);
        editor.apply();
    }

    public boolean getSwitchButton(){
        return sharedPreferences.getBoolean(S_SWITCH_BUTTON, false);
    }

    public void setsSwitchButton(boolean switchButton){
        editor = sharedPreferences.edit();
        editor.putBoolean(S_SWITCH_BUTTON, switchButton);
        editor.apply();
    }

    private void setPutarLayar(int putarLayar){
        editor = sharedPreferences.edit();
        editor.putInt(LabelPutarLayar, putarLayar);
        editor.apply();
    }

    private int getPutarLayar(){
        return sharedPreferences.getInt(LabelPutarLayar, 0);
    }

    private List<String> getListJam(){
        return Arrays.asList(getResources().getStringArray(R.array.jam));
    }

    private List<String> getListMenit(){
        return Arrays.asList(getResources().getStringArray(R.array.menit));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("LITRONRemote", Context.MODE_PRIVATE);
//        initPermission();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);


        ir = new IrController(this);

        setContentView(R.layout.second_layout);
        ButterKnife.bind(this);


        initDataShared();
    }

    private void initDataShared(){
        JamOn.setData(getListJam());
        JamOn.setSelectedItemPosition(getListJam().indexOf(getsWaktuOn().split(":")[0].length()==1?"0"+getsWaktuOn().split(":")[0]:getsWaktuOn().split(":")[0]));

        MenitOn.setData(getListMenit());
        MenitOn.setSelectedItemPosition(getListMenit().indexOf(getsWaktuOn().split(":")[1].length()==1?"0"+getsWaktuOn().split(":")[1]:getsWaktuOn().split(":")[1]));

        JamOff.setData(getListJam());
        JamOff.setSelectedItemPosition(getListJam().indexOf(getsWaktuOff().split(":")[0].length()==1?"0"+getsWaktuOff().split(":")[0]:getsWaktuOff().split(":")[0]));

        MenitOff.setData(getListMenit());
        MenitOff.setSelectedItemPosition(getListMenit().indexOf(getsWaktuOff().split(":")[1].length()==1?"0"+getsWaktuOff().split(":")[1]:getsWaktuOff().split(":")[1]));

        choose_ir.setChecked(getSwitchButton());

        choose_ir.setOnCheckedChangeListener((view, isChecked) -> setsSwitchButton(isChecked));
    }

    @OnClick(R.id.btnKirim)
    public void kirimData(){
        Date currentTime = Calendar.getInstance().getTime();

        int HourNow = currentTime.getHours();
        int MinuteNow = currentTime.getMinutes();
        int SecondNow = currentTime.getSeconds();

        int HourOn = Integer.valueOf(getListJam().get(JamOn.getCurrentItemPosition()));
        int MinuteOn = Integer.valueOf(getListMenit().get(MenitOn.getCurrentItemPosition()));

        setsWaktuOn(HourOn+":"+MinuteOn);

        int HourOff = Integer.valueOf(getListJam().get(JamOff.getCurrentItemPosition()));
        int MinuteOff = Integer.valueOf(getListMenit().get(MenitOff.getCurrentItemPosition()));

        setsWaktuOff(HourOff+":"+MinuteOff);

        btnKirim.setText("MENGIRIM DATA");
        btnKirim.setEnabled(false);

        assert audioManager != null;
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int[] d = new int[]{170, HourNow, MinuteNow, SecondNow, HourOn, MinuteOn, HourOff, MinuteOff};
        byte sum = 0;
        int count = 0;
        for (int i: d){
            a[count] = i;
            sum += (byte)i;
            count++;
        }
        a[8] = ~sum;
        if (choose_ir.isChecked()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ir.sendCode(a);
            }else{
                choose_ir.toggle();
                Toast.makeText(this, "Not Support Operating System, Please Using IR External", Toast.LENGTH_SHORT).show();
            }
        }else{
            adjustVolume(true);

            byte[] datas = new byte[]{
                    (byte) a[0],
                    (byte) a[1],
                    (byte) a[2],
                    (byte) a[3],
                    (byte) a[4],
                    (byte) a[5],
                    (byte) a[6],
                    (byte) a[7],
                    (byte) a[8]
            };
            ir.newMethod(datas);
//            new Handler().post(() -> );
//            new Handler().postDelayed(() -> ir.newMethod(datas, true), 600);
//            ir.sendUsingAudio(datas);
        }

        new Handler().postDelayed(() -> {
            adjustVolume(false);
            btnKirim.setEnabled(true);
            btnKirim.setText("SEND");
        }, 1000);
    }

    public void showBantuan(){
        Intent i = new Intent(this,BantuanActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up, R.anim.stay);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_help) {
            showBantuan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void initPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
//                requestPermissions(
//                        new String[]{Manifest.permission.CAMERA,
//                                Manifest.permission.TRANSMIT_IR
//                        }, 1);
//            }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            boolean retVal = Settings.System.canWrite(this);
            if (retVal){
//                initPutarLayar();
            }else{
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 102);
            }
        }
    }

    private void buildDialog(){
        builder = new AlertDialog.Builder(getWindow().getContext());
        builder.setCancelable(false);
        builder.setMessage("Putar Layar?");
        builder.setPositiveButton("IYA", (dialog, which) -> {
            setPutarLayar(1);
            doPutarLayar(2);
        });
        builder.setNegativeButton("TIDAK",(dialog, which)-> {
            setPutarLayar(2);
            doPutarLayar(0);
        });
        alertDialog = builder.create();
    }

    private void showDialog(){
        alertDialog.show();
    }

    private void initPutarLayar(){
        if (getPutarLayar() == 0){
            doPutarLayar(0);
            buildDialog();
            showDialog();
        }else{
            if (getPutarLayar() == 1){
                doPutarLayar(2);
            }else if (getPutarLayar() == 2){
                doPutarLayar(0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+requestCode);
        if (requestCode == 102){
//            initPutarLayar();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doPutarLayar(int code){
        boolean retVal = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
            if (retVal){
                Log.d(TAG, "doPutarLayar: "+code);
                Handler handler = new Handler();
                handler.post(() -> Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, code));
            }
        }
    }
    private void adjustVolume(boolean isPlay){
//        audioManager.setSpeakerphoneOn(true);
        Log.d(TAG, "adjustVolume: Max "+max);
        Log.d(TAG, "adjustVolume: Current "+current);
        if (isPlay){
            if (current > (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2)){
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_VIBRATE);
            }else{
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2)-1, AudioManager.FLAG_VIBRATE);
            }
//            audioManager.setSpeakerphoneOn(false);
        }else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, AudioManager.FLAG_VIBRATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initPutarLayar();
    }

    @Override
    protected void onPause() {
        doPutarLayar(0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        doPutarLayar(0);
        super.onDestroy();
    }

}
