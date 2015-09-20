package com.example.DrowsyMuse;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import android.widget.TextView;
import com.interaxon.libmuse.*;
/**
 * In this simple example MainActivity implements 2 MuseHeadband listeners
 * and updates UI when data from Muse is received. Similarly you can implement
 * listers for other data or register same listener to listen for different type
 * of data.
 * For simplicity we create Listeners as inner classes of MainActivity. We pass
 * reference to MainActivity as we want listeners to update UI thread in this
 * example app.
 * You can also connect multiple muses to the same phone and register same
 * listener to listen for data from different muses. In this case you will
 * have to provide synchronization for data members you are using inside
 * your listener.
 *
 * Usage instructions:
 * 1. Enable bluetooth on your device
 * 2. Pair your device with muse
 * 3. Run this project
 * 4. Press Refresh. It should display all paired Muses in Spinner
 * 5. Make sure Muse headband is waiting for connection and press connect.
 * It may take up to 10 sec in some cases.
 * 6. You should see EEG and accelerometer data as well as connection status,
 * Version information and MuseElements (alpha, beta, theta, delta, gamma waves)
 * on the screen.
 */
public class MyActivity extends Activity  implements PromptDialogFragment.DialogListener{
    /**
     * Connection listener updates UI with new connection status and logs it.
     */

    private int LEVEL_1 = 5;
    private final int LEVEL_2 = 50;
    private boolean LEVEL_ALARM = false;
    private boolean useDrowsyData = false;
    private boolean hasPrompted = false;
    private TextToSpeech tts;
    private Uri notification;
    private Ringtone r;
    String GPSlocation;
    private LocationManager locationManager;
    // How many drowsies to we have in a row?
    private int drowsyCount;

    public static double[] data1 = {0.54,0.86,-0.95,0.82,-0.1,0.79,-0.39,0.28,0.94,
            -0.88,-0.3,-0.95,-0.38,0.79,-0.35,0.78,-0.52,0.05,0.07,0.17,
            -0.2,-0.95,-0.86,0.63,-0.29,-0.73,-0.31,0.84,0.59,-0.12,-0.4,
            -0.6,0.43,-0.65,0.2,-0.1,-0.54,0.89,-0.21,-0.49,-0.86,0.32,0.54,
            0.31,-0.67,-0.56,0.28,-0.05,0.72,-0.33,0.57,-0.58,0.54,0.69,-0.95,
            0.48,0.36,-0.64,0.01,0.17,-0.81,-0.53,-0.29,-0.88,0.47,-0.54,-0.01,
            0.26,-0.37,-0.65,0.78,-0.78,0.11,0.22, 0.25, 0.28, 0.29, 0.23, 0.34,
            0.45, 0.56, 0.23, 0.34, 0.67, 0.23, 0.22, 0.21, 0.22, 0.29, 0.28, 0.27,
            0.25, 0.23, 0.26, 0.34, 0.45, 0.23, 0.24, 0.27, 0.38, 0.56, 0.36, 0.56,
            0.36, 0.65, 0.25, 0.36, 0.45, 0.23, 0.26, 0.45, 0.23, 0.27, 0.45, 0.78,
            0.26, 0.29, 0.56, 0.35, 0.36, 0.45, 0.58, 0.32, 0.35, 0.25, 0.21, 0.27,
            0.87, 0.23, 0.24, 0.25, 0.22, 0.29, 0.45, 0.34, 0.78, 0.65, 0.45, 0.23,
            0.24, 0.56, 0.77, 0.66, 0.43, -0.86,-0.08,-0.93,0.62,0.4,-0.73,-0.45,0.7,
            -0.13,0.68,-0.59,-0.51,0.95,0.33,-0.55,0.82,0.78,-0.01,0.38,-0.27,0.8,0.62,
            0.29,-0.52,0.79,-0.47};

    public static double[] data2 = {0.22,-0.63,0.24,-0.67,-0.87,-0.53,0.05,0.92,0.4,
            -0.72,-0.91,-0.48,-0.85,-0.67,0.92,0.07,0.64,0.16,0.91,-0.21,0.26,0.38,
            0.56,0.12,0.37,0.41,-0.78,0.9,-0.87,0.61,0.49,0,-0.05,0.12,0.01,0.21,0.77,
            -0.71,-0.53,-0.85,-0.17,-0.71,-0.51,0.7,-0.31,-0.72,-0.47,0.22,-0.57,-0.84,
            -0.12,-0.81,0.25,0.07,-0.86,0.49,-0.03,0.06,-0.12,-0.81,-0.12,-0.06,-0.95,
            -0.81,0.94,-0.95,0.89,0.94,-0.57,0.28,-0.07,0.26,-0.27,-0.5,-0.12,-0.79,0.52,
            0.07,0.3,-0.45,-0.2,-0.62,0.95, 0.11,0.22, 0.25, 0.28, 0.29, 0.23, 0.34, 0.45,
            0.56, 0.23, 0.34, 0.67, 0.23, 0.22, 0.21, 0.22, 0.29, 0.28, 0.27, 0.25, 0.23,
            0.26, 0.34, 0.45, 0.23, 0.24, 0.27, 0.38, 0.56, 0.36, 0.56, 0.36, 0.65, 0.25,
            0.36, 0.45, 0.23, 0.26, 0.45, 0.23, 0.27, 0.45, 0.78, 0.26, 0.29, 0.56, 0.35,
            0.36, 0.45, 0.58, 0.32, 0.35, 0.25, 0.21, 0.27, 0.87, 0.23, 0.24, 0.25, 0.22,
            0.29,  0.27, 0.87, 0.23, 0.24, 0.25, 0.22, 0.29, 0.45, 0.34, 0.78, 0.65, 0.45,
            0.23, 0.24, 0.56, 0.77, 0.66, 0.43, 0.07,-0.75,0.01,0.04,0.98,0.38,-0.11,-0.26,
            -0.66,0.45,-0.64,-0.83,-0.65,0.01,0.46,0.85,-0.07};

    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;
            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;
        private MuseFileWriter fileWriter;
        private ArrayList<String> stateList;
        private double lastTheta;
        private double lastAlpha;
        private double lastConcen;

        private int dataIndex;
        private double[] data;

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
            stateList = new ArrayList();
            lastAlpha = lastTheta = lastConcen = 0d;
            dataIndex = 0;
            data = data1;
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
                case ALPHA_ABSOLUTE:
                    lastAlpha = updateAlphaAbsolute(p.getValues());
                    break;
                case CONCENTRATION:
                    lastConcen = updateConcentration(p.getValues());
                    break;
                case THETA_ABSOLUTE:
                    lastTheta = updateThetaAbsolute(p.getValues());
                    break;
                case BATTERY:
                    fileWriter.addDataPacket(1, p);
                    // It's library client responsibility to flush the buffer,
                    // otherwise you may get memory overflow.
                    if (fileWriter.getBufferedMessagesSize() > 8096)
                        fileWriter.flush();
                    break;
                default:
                    break;
            }

            // We have a leaky working memory
            if (stateList.size() > 20) {
                stateList.remove(0);
            }
            double w1, w2;
            w1 = 0.7d;
            w2 = 0.3d;

            // The concentration has not been set up yet, we only use theta-alpha to decide
            if (lastConcen <= 0d) {
                double avg = 0.0d;
                if (useDrowsyData) {
                    if (dataIndex >= data.length) {
                        dataIndex = 0;
                    }
                    avg = data[dataIndex];
                    dataIndex++;
                }
                else {
                    avg = lastTheta - lastAlpha;
                }
                Log.w("AVG",String.valueOf(avg));
                if (avg >= 0.20) {
                    Log.w("D", "D");
                    stateList.add("D");
                    drowsyCount++;
                } else {
                    Log.w("A", "A");
                    stateList.add("A");
                    drowsyCount = 0;
                }
            }
            // We have some data!!
            // v1,v2 are our output "neurons"

            else {
                // We can use machine learning to learn w1, w2, ... , wn
                double v1 = 0.0d;
                double v2 = 0.0d;
                double avg = 0.0d;

                if (useDrowsyData) {
                    if (dataIndex >= data.length) {
                        dataIndex = 0;
                    }
                    avg = data[dataIndex];
                    dataIndex++;
                }
                else {
                    avg = lastTheta - lastAlpha;
                }
                Log.w("AVG",String.valueOf(avg));
                if (avg >= 0.30) {
                    v1 = 1d*w1;
                }
                else if (avg >= 0.20) {
                    v1 = 0.5d*w1;
                }


                double drowsy = 1 - lastConcen;
                v2 = drowsy * w2;

                double threshold = 0.5d;
                if (v1 + v2 >= threshold) {
                    Log.w("D", "D");
                    stateList.add("D");
                    drowsyCount++;
                }
                else {
                    Log.w("A", "A");
                    stateList.add("A");
                    drowsyCount = 0;
                }
            }

            if (drowsyCount >= LEVEL_2 && !hasPrompted) {
                Activity activity = activityRef.get();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPromptDialog();
                        }
                    });
                }

                hasPrompted = true;
            }
            // Change the colors of the bar
            Activity activity = activityRef.get();
            if (activity != null) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textViewCoffee = (TextView) findViewById(R.id.textview_coffee);
                        TextView textViewDosing = (TextView) findViewById(R.id.textview_dosing);
                        TextView textViewSleepy = (TextView) findViewById(R.id.textview_sleepy);

                        if ((drowsyCount < LEVEL_1) && !LEVEL_ALARM && (textViewCoffee != null)
                                && (textViewDosing != null)
                                && (textViewSleepy != null)) {
                            textViewCoffee.setBackgroundColor(Color.rgb(189,189,189));
                            textViewDosing.setBackgroundColor(Color.rgb(189, 189,189));
                            textViewSleepy.setBackgroundColor(Color.rgb(189, 189,189));
                        }
                        if ((drowsyCount >= LEVEL_1 && drowsyCount < LEVEL_2)
                                && !LEVEL_ALARM && (textViewCoffee != null) &&
                                (textViewSleepy != null)  && (textViewDosing != null)) {
                            textViewCoffee.setBackgroundColor(Color.YELLOW);
                            textViewDosing.setBackgroundColor(Color.rgb(189, 189,189));
                            textViewSleepy.setBackgroundColor(Color.rgb(189, 189,189));
                        }
                        if ((drowsyCount >= LEVEL_2) && (textViewSleepy != null)  &&
                                (textViewDosing != null)
                                && (textViewCoffee != null) && !LEVEL_ALARM) {
                            textViewCoffee.setBackgroundColor(Color.YELLOW);
                            textViewDosing.setBackgroundColor(Color.rgb(255, 165, 0));
                            textViewSleepy.setBackgroundColor(Color.rgb(189, 189,189));
                        }
                        if (LEVEL_ALARM && (textViewSleepy != null)  && (textViewCoffee != null)
                                && (textViewDosing != null)) {
                            textViewCoffee.setBackgroundColor(Color.YELLOW);
                            textViewDosing.setBackgroundColor(Color.rgb(255, 165, 0));
                            textViewSleepy.setBackgroundColor(Color.RED);
                        }
                    }
                });

            }

        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

        private double updateConcentration(final ArrayList<Double> data) {
            double concen = data.get(0);
            return concen;
        }

        private double updateThetaAbsolute(final ArrayList<Double> data) {
            double tp9 = data.get(Eeg.TP9.ordinal());
            double fp1 = data.get(Eeg.FP1.ordinal());
            double fp2 = data.get(Eeg.FP2.ordinal());
            double tp10 = data.get(Eeg.TP10.ordinal());
            return (fp1 + fp2 + tp9 + tp10) / 4d;
        }

        private double updateAlphaAbsolute(final ArrayList<Double> data) {
            double tp9 = data.get(Eeg.TP9.ordinal());
            double fp1 = data.get(Eeg.FP1.ordinal());
            double fp2 = data.get(Eeg.FP2.ordinal());
            double tp10 = data.get(Eeg.TP10.ordinal());
            return (fp1 + fp2 + tp9 + tp10) / 4d;
        }

        public void setFileWriter(MuseFileWriter fileWriter) {
            this.fileWriter  = fileWriter;
        }
    }

    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;
    private MuseFileWriter fileWriter = null;

    public MyActivity() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity =
                new WeakReference<Activity>(this);

        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        fileWriter = MuseFileFactory.getMuseFileWriter(
                new File(dir, "new_muse_file.muse"));
        Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
        fileWriter.addAnnotationString(1, "MainActivity onCreate");
        dataListener.setFileWriter(fileWriter);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                int result = tts.setLanguage(Locale.US);
            }
        });

        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);

        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
    }

    private void configureLibrary() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.CONCENTRATION);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.THETA_ABSOLUTE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ALPHA_ABSOLUTE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ARTIFACTS);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.BATTERY);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
        muse.setNotchFrequency(NotchFrequency.NOTCH_60HZ);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_spinner);

        int id = item.getItemId();
        if (id == R.id.action_drowsy_data) {
            useDrowsyData = !useDrowsyData;
            return true;
        }
        if (id == R.id.action_connect) {
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            if (pairedMuses.size() < 1 ||
                    musesSpinner.getAdapter().getCount() < 1) {
                Log.w("Muse Headband", "There is nothing to connect to");
            }
            else {
                muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
                ConnectionState state = muse.getConnectionState();
                if (state == ConnectionState.CONNECTED ||
                        state == ConnectionState.CONNECTING) {
                    Log.w("Muse Headband",
                            "doesn't make sense to connect second time to the same muse");
                }
                else {
                    configureLibrary();
                    fileWriter.open();
                    fileWriter.addAnnotationString(1, "Connect clicked");
                    /**
                     * In most cases libmuse native library takes care about
                     * exceptions and recovery mechanism, but native code still
                     * may throw in some unexpected situations (like bad bluetooth
                     * connection). Print all exceptions here.
                     */
                    try {
                        muse.runAsynchronously();
                    } catch (Exception e) {
                        Log.e("Muse Headband", e.toString());
                    }
                }
            }
            return true;
        }
        if (id == R.id.action_disconnect) {
            if (muse != null) {
                /**
                 * true flag will force libmuse to unregister all listeners,
                 * BUT AFTER disconnecting and sending disconnection event.
                 * If you don't want to receive disconnection event (for ex.
                 * you call disconnect when application is closed), then
                 * unregister listeners first and then call disconnect:
                 * muse.unregisterAllListeners();
                 * muse.disconnect(false);
                 */
                muse.disconnect(true);
                fileWriter.addAnnotationString(1, "Disconnect clicked");
                fileWriter.flush();
                fileWriter.close();
            }
            return true;
        }
        if (id == R.id.action_refresh) {
            MuseManager.refreshPairedMuses();
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            List<String> spinnerItems = new ArrayList<String>();
            for (Muse m: pairedMuses) {
                String dev_id = m.getName() + "-" + m.getMacAddress();
                Log.i("Muse Headband", dev_id);
                spinnerItems.add(dev_id);
            }
            ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
                    this, android.R.layout.simple_spinner_item, spinnerItems);
            musesSpinner.setAdapter(adapterArray);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        LEVEL_ALARM = false;
        drowsyCount = 0;
        r.stop();
        tts = null;
    }

    public void onPromptDialog() {
        onSpeak("Are you falling asleep?");
        DialogFragment newFragment = new PromptDialogFragment();
        newFragment.show(getFragmentManager(), "Drowsy?");

        new CountDownTimer(25000, 1000) {

            public void onTick(long msUntilFinished) {

                if ((msUntilFinished / 1000) == 15) {
                    onSpeak("Please wake up");
                }
                if ((msUntilFinished / 1000) == 12) {
                    onAlarm();
                }
            }

            public void onFinish() {
                r.stop();
                onSpeak("Messaging Emergency Contacts");

                GPSlocation = "(" + locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()
                        + ", " + locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude() + ")";
                Log.w("LOCATION", GPSlocation);
                String message = "Hi, I" +
                        " might have fallen asleep while driving and may require medical " +
                        "attention! Here is my location: " + GPSlocation;

                ArrayList<String> list = obtainFavorites();
                for (int i = 0; i < list.size(); i++) {
                    String number = list.get(i);
                    SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
                }

            }
        }.start();
    }

    public void onSpeak(String voice) {
        if (tts != null) {
            Long tsLong = System.currentTimeMillis() / 1000;
            //String identifier = "speech" + tsLong.toString();
            tts.speak(voice, TextToSpeech.QUEUE_ADD, null);
        }
    }
    public void onAlarm() {
        LEVEL_ALARM = true;
        r.play();
    }

    public void onSendToDialog(long ms) {
        TextView txtView = (TextView) findViewById(R.id.txtCountdown);
        txtView.setText("" + ms / 1000);
        //PromptDialogFragment dialog = (PromptDialogFragment) getFragmentManager().findFragmentByTag("drowsyPrompt");
        //dialog.onCountUpdated(ms);
    }

    @Override
    protected void onDestroy() {
        tts.stop();
        tts.shutdown();
        super.onDestroy();
    }

    public ArrayList<String> obtainFavorites()
    {

        //  Find contact based on name.
        ArrayList<String> phoneNumbers = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                "starred=?", new String[]{"1"}, null);
        if (cursor.moveToFirst()) {
            String contactId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //
            //  Get all phone numbers.
            //

            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        // do nothing
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        phoneNumbers.add(number);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        // do nothing
                        break;
                }
            }
            phones.close();
        }
        cursor.close();

        return phoneNumbers;
    }
}