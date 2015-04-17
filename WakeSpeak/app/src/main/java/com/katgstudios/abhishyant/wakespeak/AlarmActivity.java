package com.katgstudios.abhishyant.wakespeak;

import android.support.v7.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class AlarmActivity extends ActionBarActivity implements OnInitListener{
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech mTTS;
    private PendingIntent alarmPendingIntent;
    private ShareActionProvider mShare;
    public static ArrayList<String> mAlarmNames = new ArrayList<String>();

    private ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "KLPNm4OOkvYB3G8AMns1HwiPjNqPzJ0o0M0Nz2jO", "cZbvnOZuTwgONSzaL42LJhi39ENCAkEMp55DpSLA");
        ActionBar bar = (ActionBar)getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0070B8")));

        setContentView(R.layout.activity_alarm);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AlarmFragment())
                    .commit();
        }

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        Intent alarmIntent = new Intent(this,AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(this,1,alarmIntent,0);
        Intent setAlarmIntent = getIntent();
        addAlarm(setAlarmIntent);

        //PushService.setDefaultPushCallback(this, AlarmActivity.class);
       // mAlarmNames = new ArrayList<>();



        //int hour =29;
       // setAlarm(hour,10,true);
       // Log.d("TIME MINUTES",Integer.toString(hour));
        //comment for git testing
        registerReceiver(receiver, new IntentFilter("SPEAK"));
    }

    public void showDeleteAlarm(){

    }

    public void setAlarm(Calendar calendar,boolean isRepeating){
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int AM_PM = calendar.get(Calendar.AM_PM);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(AM_PM == 1 && hour>=1) {


            hour += 12;
            Log.d("HOUR",Integer.toString(hour));
        }

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.DAY_OF_WEEK,weekDay);
        int repeat = 604800*1000;

        if(isRepeating)
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                alarmPendingIntent);

        else
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),repeat,alarmPendingIntent);

    }


    public void addAlarm(Intent setAlarmIntent){
        if(setAlarmIntent.getParcelableExtra("Alarm") != null) {
            AlarmObject alarm = setAlarmIntent.getParcelableExtra("Alarm");
            for(int i = 0; i <7;i++) {
                if(alarm.getWeekDays()[i]) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR, alarm.getHour());
                    calendar.set(Calendar.MINUTE, alarm.getMinute());
                    calendar.set(Calendar.AM_PM, alarm.getAM_PM());
                    calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                    if(alarm.getRepeating()==0)
                     setAlarm(calendar,false);
                    else
                        setAlarm(calendar,true);

                }

            }

            String alarmData = formatTime(alarm.getAlarmName(),alarm.getHour(),alarm.getMinute(),alarm.getAM_PM());
            mAlarmNames.add(alarmData);


            if(currentUser.has("AlarmList")){
                Toast.makeText(AlarmActivity.this,"HAS LIST",Toast.LENGTH_LONG).show();
                ( (ArrayList<AlarmObject>)currentUser.get("AlarmList")).add(alarm);

            //    currentUser.saveInBackground();

            }
            if(currentUser.has("AlarmNames")){
                ((ArrayList<String>)currentUser.get("AlarmNames")).add(alarmData);
            //    currentUser.saveInBackground();
            }
            else{
                Log.d("ALARM ACTIVITY","new objects being created");
                currentUser.put("AlarmNames", mAlarmNames);
                ArrayList<AlarmObject> alarmList = new ArrayList<AlarmObject>();
                alarmList.add(alarm);

                currentUser.put("AlarmList", alarmList);
           //     currentUser.saveInBackground();
//
            }
            currentUser.put("test1", "test");

           currentUser.saveInBackground(new SaveCallback() {
               @Override
               public void done(com.parse.ParseException e) {
                   if(e == null)
                    Log.d("ALARM ACTIVITY","DONE SAVING");
                   else
                    e.printStackTrace();
               }
           });




        }
        else{
            Log.d("Alarm Activity","INTENT NULL");
        }

    }
    public String formatTime(String alarmName,int hour, int min, int am_pm){
        String hourStr = Integer.toString(hour)+":";
        String minStr = Integer.toString(min);
        if(min<10)
            minStr = "0"+minStr+" ";
        String am_pmStr = "AM";
        if(am_pm == 1)
            am_pmStr = "PM";
        return alarmName + hourStr+minStr+am_pmStr;
    }



    //SPEAK
    public void speak(){
        mTTS.speak("Hello ahbeeshant!", TextToSpeech.QUEUE_FLUSH,null);

    }


    @Override
    public void onInit(int initStatus){
        if(initStatus == TextToSpeech.SUCCESS){
            mTTS.setLanguage(Locale.US);
            mTTS.setPitch(1.3f);
        }
        else if(initStatus == TextToSpeech.ERROR){
            Toast.makeText(this,"Please Try Again",Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm, menu);
        mShare = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_item_share));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,"I'M SHARING STUFF!");
        shareIntent.setType("text/plain");
        mShare.setShareIntent(shareIntent);
        //mShare.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.action_addalarm){
            Intent createAlarmIntent = new Intent(this,SetAlarmActivity.class);
            startActivity(createAlarmIntent);
            return true;
        }
        if(id==R.id.menu_item_share){



            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //OnActivityResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MY_DATA_CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(this, this);
            }
            else{
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }


    }
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            speak();
        }
    };
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }




}
