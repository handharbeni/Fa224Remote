package com.litron.litronremote;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.suke.widget.SwitchButton;

import java.util.Calendar;
import java.util.Date;

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


//    @BindView(R.id.etWaktuOn)
//    EditText etWaktuOn;
//    @BindView(R.id.etWaktuOff)
//    EditText etWaktuOff;
    @BindView(R.id.btnKirim)
    AppCompatButton btnKirim;

    @BindView(R.id.JamOn)
    Spinner JamOn;

    @BindView(R.id.JamOff)
    Spinner JamOff;

    @BindView(R.id.MenitOn)
    Spinner MenitOn;

    @BindView(R.id.MenitOff)
    Spinner MenitOff;

    @BindView(R.id.txtBantuan)
    TextView txtBantuan;

    @BindView(R.id.choose_ir)
    SwitchButton choose_ir;




    int[] a = new int[8];

    private static String S_WAKTU_ON = "WaktuON";
    private static String S_WAKTU_OFF = "WaktuOFF";
    private static String S_SWITCH_BUTTON = "SwitchBUTTON";
    String sWaktuOn, sWaktuOff, sButton;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("LITRONRemote", Context.MODE_PRIVATE);
        initPermission();

        ir = new IrController(this);

        setContentView(R.layout.second_layout);
        ButterKnife.bind(this);

        initDataShared();
    }

    private void initDataShared(){
        ArrayAdapter<CharSequence> jamOn = ArrayAdapter.createFromResource(this, R.array.jam, android.R.layout.simple_spinner_item);
        jamOn.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> jamOff = ArrayAdapter.createFromResource(this, R.array.jam, android.R.layout.simple_spinner_item);
        jamOff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> menitOn = ArrayAdapter.createFromResource(this, R.array.menit, android.R.layout.simple_spinner_item);
        menitOn.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> menitOff = ArrayAdapter.createFromResource(this, R.array.menit, android.R.layout.simple_spinner_item);
        menitOff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        JamOn.setAdapter(jamOn);
        JamOn.setSelection(jamOn.getPosition(getsWaktuOn().split(":")[0].length()==1?"0"+getsWaktuOn().split(":")[0]:getsWaktuOn().split(":")[0]));
        MenitOn.setAdapter(menitOn);
        MenitOn.setSelection(menitOn.getPosition(getsWaktuOn().split(":")[1].length()==1?"0"+getsWaktuOn().split(":")[1]:getsWaktuOn().split(":")[1]));

        JamOff.setAdapter(jamOff);
        JamOff.setSelection(jamOff.getPosition(getsWaktuOff().split(":")[0].length()==1?"0"+getsWaktuOff().split(":")[0]:getsWaktuOff().split(":")[0]));
        MenitOff.setAdapter(menitOff);
        MenitOff.setSelection(menitOff.getPosition(getsWaktuOff().split(":")[1].length()==1?"0"+getsWaktuOff().split(":")[1]:getsWaktuOff().split(":")[1]));

        choose_ir.setChecked(getSwitchButton());

        choose_ir.setOnCheckedChangeListener((view, isChecked) -> setsSwitchButton(isChecked));
    }

    @OnClick(R.id.btnKirim)
    public void kirimData(){
        Date currentTime = Calendar.getInstance().getTime();

        int HourNow = currentTime.getHours();
        int MinuteNow = currentTime.getMinutes();

        int HourOn = Integer.valueOf(JamOn.getSelectedItem().toString());
        int MinuteOn = Integer.valueOf(MenitOn.getSelectedItem().toString());

        setsWaktuOn(HourOn+":"+MinuteOn);

        int HourOff = Integer.valueOf(JamOff.getSelectedItem().toString());
        int MinuteOff = Integer.valueOf(MenitOff.getSelectedItem().toString());

        setsWaktuOff(HourOff+":"+MinuteOff);

        btnKirim.setText("MENGIRIM DATA");
        btnKirim.setEnabled(false);

        Handler handler = new Handler();
        handler.post(() -> {
            int[] d = new int[]{170, HourNow, MinuteNow, HourOn, MinuteOn, HourOff, MinuteOff};
            byte sum = 0;
            int count = 0;
            for (int i: d){
                a[count] = i;
                sum += (byte)i;
                count++;
            }
            a[7] = ~sum;

            if (choose_ir.isChecked()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ir.sendCode(a);
                }
            }else{
                ir.sendCodeAudioOnly(a);
            }
        });
        handler.postDelayed(() -> {
            btnKirim.setEnabled(true);
            btnKirim.setText("SEND");
        }, 2000);
    }

    @OnClick(R.id.txtBantuan)
    public void showBantuan(){
        Intent i = new Intent(this,BantuanActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up, R.anim.stay);

    }

    private void initPermission(){
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean retVal = Settings.System.canWrite(this);
            if (retVal){
                initPutarLayar();
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
            initPutarLayar();
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

//    @Override
//    protected void onResume() {
//        super.onResume();
////        initPutarLayar();
//    }
//
//    @Override
//    protected void onPause() {
//        doPutarLayar(0);
//        super.onPause();
//    }

    @Override
    protected void onDestroy() {
        doPutarLayar(0);
        super.onDestroy();
    }
}
