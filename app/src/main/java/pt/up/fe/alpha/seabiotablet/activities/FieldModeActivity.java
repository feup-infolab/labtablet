package pt.up.fe.alpha.seabiotablet.activities;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.seabiotablet.api.ChangelogManager;
import pt.up.fe.alpha.seabiotablet.api.LTLocationListener;
import pt.up.fe.alpha.seabiotablet.async.AsyncTaskHandler;
import pt.up.fe.alpha.seabiotablet.async.AsyncWeatherFetcher;
import pt.up.fe.alpha.seabiotablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.seabiotablet.db_handlers.FormMgr;
import pt.up.fe.alpha.seabiotablet.models.ChangelogItem;
import pt.up.fe.alpha.seabiotablet.models.Descriptor;
import pt.up.fe.alpha.seabiotablet.models.FavoriteItem;
import pt.up.fe.alpha.seabiotablet.models.Form;
import pt.up.fe.alpha.seabiotablet.models.FormInstance;
import pt.up.fe.alpha.seabiotablet.utils.FileMgr;
import pt.up.fe.alpha.seabiotablet.utils.Utils;



/**
 * Exposes many of the device's sensors to gather their values
 * It also adds options to import from other resources such as camera, screen and gps
 */
public class FieldModeActivity extends AppCompatActivity implements SensorEventListener {


    private Button bt_photo;
    private Button bt_sketch;
    private Button bt_audio;

    private Button bt_location;
    private Button bt_note;

    private Button bt_temperature_sample;
    private Button bt_network_temperature_sample;
    private Button bt_luminosity_sample;
    private Button bt_magnetic_sample;
    private Button bt_launch_form;

    private Switch sw_gps;

    private SensorManager sensorManager;
    private LTLocationListener locationListener;
    private MediaRecorder recorder;
    private BroadcastReceiver mBatInfoReceiver;
    private SensorsOnClickListener sensorClickListener;

    private String favorite_name;
    private String temperature_value;
    private String network_temperature_value;

    private String real_magnetic_value;
    private String audio_filename;

    private boolean recording;
    private Uri capturedImageUri;
    private String path;
    private boolean isCollecting;
    private long lastUpdateNetworkTemperature;
    private long lastUpdateSensorTemperature;
    private long lastUpdateSensorMagnetic;
    private long lastUpdateSensorLuminosity;

    private FavoriteItem currentFavoriteItem;
    private ProgressBar pb_update;
    private ProgressBar pb_location;

    private boolean formCollected;

    private ArrayList<Descriptor> gatheredMetadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_mode);
        capturedImageUri = null;
        isCollecting = false;
        Intent intent = getIntent();
        favorite_name = intent.getStringExtra("favorite_name");
        sensorClickListener = new SensorsOnClickListener();
        gatheredMetadata = new ArrayList<>();

        path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + getResources().getString(R.string.app_name)
                + File.separator + favorite_name
                + File.separator + "meta";

        //Make meta directory
        FileMgr.makeMetaDir(getApplication(), path);
        attachButtons();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(favorite_name);
        getSupportActionBar().setSubtitle(getString(R.string.title_activity_field_mode));

        currentFavoriteItem = FavoriteMgr.getFavorite(this, favorite_name);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mBatInfoReceiver != null) {
            registerBatInforReceiver();
        }

        LTLocationListener.kmlCreatedInterface interfaceKml = new LTLocationListener.kmlCreatedInterface() {
            @Override
            public void kmlCreated(Descriptor kmlDescriptor) {
                gatheredMetadata.add(kmlDescriptor);
            }
        };

        locationListener = new LTLocationListener(FieldModeActivity.this, path, interfaceKml);


        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //it is locked
            Log.i("Screen", "locked");
        } else {

            //it is not locked
            Log.i("Screen", "UNlocked");
        }



        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationListener == null) {
                    return;
                }

                if (!isCollecting) {
                    isCollecting = true;
                    if (sw_gps.isChecked()) {
                        if (!locationListener.notifyCollectStarted()) {
                            isCollecting = false;
                            sw_gps.setChecked(false);
                        }
                    }
                } else {
                    isCollecting = false;
                    if (!sw_gps.isChecked()) {
                        locationListener.notifyCollectStopped();
                    }
                }
            }
        });

        bt_sketch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FieldModeActivity.this, FingerPaintActivity.class);
                intent.putExtra("folderName", favorite_name);
                startActivityForResult(intent, Utils.SKETCH_INTENT_REQUEST);
            }
        });


        bt_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(FieldModeActivity.this);
                builder.setTitle(getResources().getString(R.string.new_text_record));

                // Set up the input
                final EditText input = new EditText(FieldModeActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);


                builder.setView(input);


                // Set up the buttons
                builder.setPositiveButton(getResources().getString(R.string.form_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (input.getText().toString().equals("")) {
                            return;
                        }

                        Descriptor desc = new Descriptor();
                        desc.setValue(input.getText().toString());

                        desc.setTag(Utils.TEXT_TAGS);
                        gatheredMetadata.add(desc);
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

            }
        });

        bt_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recording) {
                    try {

                        pb_update.setVisibility(View.INVISIBLE);
                        bt_audio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_black_24dp, 0, 0, 0);
                        recording = false;
                        bt_audio.setText(getResources().getString(R.string.record));
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                        Descriptor mDesc = new Descriptor();
                        mDesc.setTag(Utils.AUDIO_TAGS);
                        mDesc.setValue(Uri.parse(audio_filename).getLastPathSegment());
                        mDesc.setFilePath(audio_filename);
                        gatheredMetadata.add(mDesc);
                    } catch (Exception e) {
                        ChangelogItem item = new ChangelogItem();
                        item.setMessage("FieldMode audio recorder: " + e.toString() + "When stopping the recorder. Device stopped working.");
                        item.setTitle(getResources().getString(R.string.developer_error));
                        item.setDate(Utils.getDate());
                        ChangelogManager.addLog(item, FieldModeActivity.this);
                    }
                } else {

                    //if (isVoiceRecRunning) sw_handsFree.performClick(); //shutdown voice recognition
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    recording = true;
                    pb_update.setVisibility(View.VISIBLE);
                    pb_update.setIndeterminate(true);
                    bt_audio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_voice_busy, 0, 0, 0);
                    bt_audio.setText(getResources().getString(R.string.stop));

                    try {
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        audio_filename = path + "/recording_" + new Date().getTime() + ".mp3";
                        recorder.setOutputFile(audio_filename);
                        recorder.prepare();
                        recorder.start();
                    } catch (Exception e) {
                        ChangelogItem item = new ChangelogItem();
                        item.setMessage("FieldMode audio recorder: " + e.toString() + "After preparing the recorder. Devise is being used by another application.");
                        item.setTitle(getResources().getString(R.string.developer_error));
                        item.setDate(Utils.getDate());
                        ChangelogManager.addLog(item, FieldModeActivity.this);

                        Toast.makeText(FieldModeActivity.this, "Device is busy. Close other applications in the background.",
                                Toast.LENGTH_SHORT).show();

                        pb_update.setVisibility(View.INVISIBLE);
                        bt_audio.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_black_24dp, 0, 0, 0);
                        recording = false;
                        bt_audio.setText(getResources().getString(R.string.record));
                        recorder = null;
                    }

                }
            }
        });


        bt_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!startService()) {
                    Log.e("LOCATIONListener", "error staring service");
                }
            }
        });

        bt_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CharSequence options[] = new CharSequence[]{getString(R.string.photo), "Video"};

                AlertDialog.Builder builder = new AlertDialog.Builder(FieldModeActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        Calendar cal = Calendar.getInstance();
                        String mediaFileName = "" + cal.getTimeInMillis();

                        if (which == 0) {
                            mediaFileName += ".jpg";
                            dispatchCaptureMediaIntent(Utils.CAMERA_INTENT_REQUEST, mediaFileName);
                        } else if (which == 1) {
                            mediaFileName += ".mp4";
                            dispatchCaptureMediaIntent(Utils.VIDEO_CAPTURE_REQUEST, mediaFileName);
                        }


                    }
                });
                builder.show();
            }
        });

        bt_launch_form.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                formCollected = true;
                final ArrayList<Form> forms = FormMgr.getCurrentBaseForms(FieldModeActivity.this);

                final CharSequence values[] = new CharSequence[forms.size()];
                for (int i = 0; i < forms.size(); ++i) {
                    values[i] = forms.get(i).getFormName();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(FieldModeActivity.this);
                builder.setTitle(getResources().getString(R.string.select_form_solve));
                builder.setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent formIntent = new Intent(FieldModeActivity.this, FormSolverActivity.class);
                        formIntent.putExtra("form",
                                new Gson().toJson(new FormInstance(forms.get(which))));
                        startActivityForResult(formIntent, Utils.SOLVE_FORM);
                    }
                });
                builder.show();
            }
        });
    }


    private void registerBatInforReceiver() {
        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                if ((System.currentTimeMillis() - lastUpdateSensorTemperature) < Utils.SAMPLE_MILLIS) {
                    return;
                }
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
                temperature_value = "" + temperature;
                bt_temperature_sample.setEnabled(true);
                bt_temperature_sample.setText(temperature_value + getResources().getString(R.string.battery_temp));
                lastUpdateSensorTemperature = System.currentTimeMillis();
            }
        };
        registerReceiver(mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void attachButtons() {

        bt_audio = (Button) findViewById(R.id.bt_audio);
        bt_sketch = (Button) findViewById(R.id.bt_sketch);
        bt_photo = (Button) findViewById(R.id.bt_camera);
        bt_location = (Button) findViewById(R.id.bt_one_time_position);
        bt_note = (Button) findViewById(R.id.bt_text);

        bt_temperature_sample = (Button) findViewById(R.id.bt_temperature_sample);
        bt_luminosity_sample = (Button) findViewById(R.id.bt_luminosity);
        bt_magnetic_sample = (Button) findViewById(R.id.bt_magnetic);
        bt_network_temperature_sample = (Button) findViewById(R.id.bt_network_temperature_sample);
        bt_launch_form = (Button) findViewById(R.id.bt_form);

        bt_network_temperature_sample.setOnClickListener(sensorClickListener);

        sw_gps = (Switch) findViewById(R.id.sw_gps);
        pb_update = (ProgressBar) findViewById(R.id.pb_recording);
        pb_location = (ProgressBar) findViewById(R.id.pb_location);


        if ((System.currentTimeMillis() - lastUpdateNetworkTemperature) > Utils.SAMPLE_MILLIS) {
            bt_network_temperature_sample.setText(getResources().getString(R.string.loading));
            new AsyncWeatherFetcher(new AsyncTaskHandler<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    bt_network_temperature_sample.setEnabled(true);
                    network_temperature_value = result + getResources().getString(R.string.network_temp);
                    bt_network_temperature_sample.setText(network_temperature_value);
                    lastUpdateNetworkTemperature = System.currentTimeMillis();
                }

                @Override
                public void onFailure(Exception error) {
                    bt_network_temperature_sample.setEnabled(false);
                    bt_network_temperature_sample.setText(getResources().getString(R.string.not_available));
                }

                @Override
                public void onProgressUpdate(int value) {

                }
            }).execute(getApplication());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.field_mode, menu);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        String luminosity_value;
        String magnetic_value;

        switch (type) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                if ((System.currentTimeMillis() - lastUpdateSensorMagnetic) < Utils.SAMPLE_MILLIS) {
                    break;
                }
                magnetic_value = Math.round(event.values[0]) + ", " + Math.round(event.values[1]) + ", " + Math.round(event.values[2]);
                real_magnetic_value = event.values[0] + ", " + event.values[1] + ", " + event.values[2];
                bt_magnetic_sample.setText(magnetic_value + " (uT)");
                bt_magnetic_sample.setEnabled(true);
                lastUpdateSensorMagnetic = System.currentTimeMillis();
                break;

            case Sensor.TYPE_LIGHT:
                if ((System.currentTimeMillis() - lastUpdateSensorLuminosity) < Utils.SAMPLE_MILLIS) {
                    break;
                }
                luminosity_value = event.values[0] + "lx";
                bt_luminosity_sample.setText(luminosity_value);
                bt_luminosity_sample.setEnabled(true);
                lastUpdateSensorLuminosity = System.currentTimeMillis();
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("fieldMode", "resume");

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

        registerBatInforReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("fieldMode", "pause");

        if (mBatInfoReceiver != null) {
            unregisterReceiver(mBatInfoReceiver);
            mBatInfoReceiver = null;
        }
        sensorManager.unregisterListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(FieldModeActivity.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
            return;
        }

        //get the picture filename and update records

        switch (requestCode) {
            case Utils.SKETCH_INTENT_REQUEST:
                if (resultCode != RESULT_OK) {
                    return;
                }
                String filePath = data.getStringExtra("result");
                Descriptor desc = new Descriptor();
                desc.setValue(Uri.parse(filePath).getLastPathSegment());
                desc.setFilePath(filePath);
                desc.setTag(Utils.PICTURE_TAGS);
                gatheredMetadata.add(desc);
                break;

            case Utils.CAMERA_INTENT_REQUEST:
                Descriptor desc2 = new Descriptor();
                desc2.setFilePath(capturedImageUri.getPath());
                desc2.setValue(capturedImageUri.getLastPathSegment());
                desc2.setTag(Utils.PICTURE_TAGS);
                gatheredMetadata.add(desc2);

                break;

            case Utils.METADATA_VALIDATION:
                if (data == null) {
                    return;
                }
                if (!data.getExtras().containsKey("favorite")) {
                    Toast.makeText(this, "No descriptors received", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, getResources().getString(R.string.metadata_added_success), Toast.LENGTH_SHORT).show();
                finish();
                break;

            case Utils.SOLVE_FORM:
                if (data == null) {
                    return;
                }

                if (!data.getExtras().containsKey("form")) {
                    ChangelogItem item = new ChangelogItem();
                    item.setMessage("No form was received after selection.");
                    item.setTitle(getResources().getString(R.string.developer_error));
                    item.setDate(Utils.getDate());
                    ChangelogManager.addLog(item, FieldModeActivity.this);
                    return;
                }

                //Add form item to the favorite record
                FormInstance form = new Gson().fromJson(data.getStringExtra("form"), FormInstance.class);
                currentFavoriteItem.addFormInstance(form);
                FavoriteMgr.updateFavoriteEntry(
                        currentFavoriteItem.getTitle(), currentFavoriteItem, this);


                break;
            case Utils.VIDEO_CAPTURE_REQUEST:
                if (data == null || data.getData() == null) {
                    Toast.makeText(FieldModeActivity.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
                    return;
                }

                Descriptor desc3 = new Descriptor();
                desc3.setFilePath(data.getDataString());
                desc3.setValue(data.getData().getLastPathSegment());
                desc3.setTag(Utils.PICTURE_TAGS);
                gatheredMetadata.add(desc3);
                Log.e("uri", data.getDataString());

                break;


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() != R.id.action_field_mode_end) {
            return super.onOptionsItemSelected(item);
        }

        if (isCollecting) {
            Toast.makeText(this, getResources().getString(R.string.cant_end_is_collecting), Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
        }
        if (recording) {
            Toast.makeText(this, getResources().getString(R.string.cant_end_is_recording), Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
        }

        if (gatheredMetadata.size() == 0
                && !formCollected) {
            Toast.makeText(this, getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
            finish();
            return super.onOptionsItemSelected(item);
        }

        FavoriteMgr.updateFavoriteEntry(favorite_name, currentFavoriteItem, this);

        //Proceed to validate collected metadata
        Intent intent = new Intent(FieldModeActivity.this, ValidateMetadataActivity.class);
        intent.putExtra("favorite", new Gson().toJson(currentFavoriteItem));
        intent.putExtra("unvalidated_metadata", new Gson().toJson(gatheredMetadata));
        startActivityForResult(intent, Utils.METADATA_VALIDATION);

        return super.onOptionsItemSelected(item);
    }

    private boolean startService() {
        try {
            new FetchCoordinates().execute();
            return true;
        } catch (Exception error) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("Location Listener: " +
                    error.toString() +
                    "When starting the service. Device may not have any GPS devices or the resources may be in use by another application.");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, FieldModeActivity.this);
            return false;
        }
    }


    private class SensorsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Descriptor desc;
            int id = view.getId();

            switch (id) {
                case R.id.bt_network_temperature_sample:
                    desc = new Descriptor();
                    desc.setValue(bt_network_temperature_sample.getText().toString());
                    desc.setTag(Utils.TEMP_TAGS);
                    gatheredMetadata.add(desc);

                    Toast.makeText(getApplication(),
                            getResources().getString(R.string.net_temp_saved),
                            Toast.LENGTH_SHORT).show();

                    break;

                case R.id.bt_temperature_sample:
                    desc = new Descriptor();
                    desc.setValue(bt_temperature_sample.getText().toString());
                    desc.setTag(Utils.TEMP_TAGS);
                    gatheredMetadata.add(desc);

                    Toast.makeText(getApplication(),
                            getResources().getString(R.string.temp_saved),
                            Toast.LENGTH_SHORT).show();

                    break;

                case R.id.bt_magnetic:
                    desc = new Descriptor();
                    desc.setValue(real_magnetic_value);
                    desc.setTag(Utils.MAGNETIC_TAGS);
                    gatheredMetadata.add(desc);


                    Toast.makeText(getApplication(),
                            getResources().getString(R.string.mag_saved),
                            Toast.LENGTH_SHORT).show();

                    break;

                case R.id.bt_luminosity:
                    desc = new Descriptor();
                    desc.setValue(bt_luminosity_sample.getText().toString());
                    desc.setTag(Utils.TEXT_TAGS);
                    gatheredMetadata.add(desc);

                    Toast.makeText(getApplication(),
                            getResources().getString(R.string.lum_saved),
                            Toast.LENGTH_SHORT).show();

                    break;


            }
        }
    }

    /**
     * Waits until valid coordinates are available
     */
    public class FetchCoordinates extends AsyncTask<String, Integer, Void> {
        public double latitude = 0.0;
        public double longitude = 0.0;


        public LocationManager mLocationManager;
        public mLocationListener mLocationListener;

        @Override
        protected void onPreExecute() {
            mLocationListener = new mLocationListener();
            mLocationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0,
                    mLocationListener);




            if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                Toast.makeText(FieldModeActivity.this, getResources().getString(R.string.location_disabled), Toast.LENGTH_SHORT).show();
                this.cancel(true);
                return;
            }





            pb_location.setIndeterminate(true);
            pb_location.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {
            pb_location.setIndeterminate(false);
            mLocationManager.removeUpdates(mLocationListener);
        }

        @Override
        protected void onPostExecute(Void result) {
            pb_location.setIndeterminate(false);

            Descriptor desc = new Descriptor();
            desc.setValue(latitude + "," + longitude);
            desc.setTag(Utils.GEO_TAGS);
            gatheredMetadata.add(desc);

            String text = "LAT:" + latitude + " LNG:" + longitude;

            Toast.makeText(FieldModeActivity.this,
                    text,
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Void doInBackground(String... params) {


            while (this.latitude == 0.0) {
                //empty block on purpose
            }
            return null;
        }

        public class mLocationListener implements LocationListener {

            @Override
            public void onLocationChanged(Location location) {

                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } catch (Exception e) {
                    pb_location.setIndeterminate(false);

                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.loc_unavailable)
                            , Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("OnProviderDisabled", "OnProviderDisabled");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i("onProviderEnabled", "onProviderEnabled");
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                Log.i("onStatusChanged", "onStatusChanged");

            }
        }
    }

    //Launch intent to record video
    private void dispatchCaptureMediaIntent(int which, String mediaPath) {

        Intent captureMediaIntent;
        File file = new File(path, mediaPath);

        if (file.exists() && !file.delete()) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("FieldMode" + "Failed to delete file " + file.getAbsolutePath());
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, FieldModeActivity.this);
            Toast.makeText(FieldModeActivity.this, "Check logs", Toast.LENGTH_SHORT).show();
            return;
        }

        //File either doesn't exist or was deleted - create a new one
        try {
            if (!file.createNewFile()) {
                ChangelogItem item = new ChangelogItem();
                item.setMessage("FieldMode" + "Couldn't create file " + file.getAbsolutePath());
                item.setTitle(getResources().getString(R.string.developer_error));
                item.setDate(Utils.getDate());
                ChangelogManager.addLog(item, FieldModeActivity.this);
                Toast.makeText(FieldModeActivity.this, "Check logs", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            Log.e("FIELD_MODE", e.toString());
            return;
        }

        if (which == Utils.CAMERA_INTENT_REQUEST) {
            capturedImageUri = Uri.fromFile(file);
            captureMediaIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (captureMediaIntent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(FieldModeActivity.this,
                        "There are no applications to handle this action, please install one from the Play Store",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            captureMediaIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(captureMediaIntent, Utils.CAMERA_INTENT_REQUEST);

        } else if (which == Utils.VIDEO_CAPTURE_REQUEST) {
            captureMediaIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (captureMediaIntent.resolveActivity(getPackageManager()) == null) {
                Toast.makeText(FieldModeActivity.this,
                        "There are no applications to handle this action, please install one from the Play Store",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            captureMediaIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(captureMediaIntent, Utils.VIDEO_CAPTURE_REQUEST);
        }
    }

/*
    public OfflineVoiceRecognition getOfflineRecognizer() {
        return offlineRecognizer;
    }


    public GoogleVoiceRecognition getGoogleRecognizer() {
        return googleRecognizer;
    }


    public TTSvoice getTTSvoice() {
        return ttsVoice;
    }

    public void shutdownRecognizer() {
        if (offlineRecognizer != null) {
            offlineRecognizer.shutdown();
            offlineRecognizer = null;
        }
        if (googleRecognizer != null) {
            googleRecognizer.shutdown();
            googleRecognizer = null;
            locationListener.setGoogleRecognizer(null);
        }
    }

    public void turnOnGoogleRec() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (googleRecognizer.getRecognizer() != null) {
                    googleRecognizer.getRecognizer().startListening
                            (googleRecognizer.getRecognizerIntent());
                    Log.d("recognizer", "listening with google");
                }
            }
        });
    }

    public void setGoogleRecognizer(GoogleVoiceRecognition gvr) {
        this.googleRecognizer = gvr;
    }

    public void setOfflineRecognizer(OfflineVoiceRecognition ofr) {
        this.offlineRecognizer = ofr;
    }

    public void turnOnSphinxRec() {
        final Context context = this;
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {

                if (offlineRecognizer.getRecognizer() == null) {
                    Assets assets = null;
                    try {
                        assets = new Assets(context);
                        File assetDir = assets.syncAssets();
                        offlineRecognizer.setupRecognizer(assetDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.e("", "Failed to init recognizer " + result);
                } else {
                    if (offlineRecognizer != null) {
                        offlineRecognizer.startListen();
                        Log.d("recognizer", "listening with sphinx");
                    }

                }
            }
        }.execute();
    }
*/

    public FavoriteItem getCurrentFavoriteItem() {
        return currentFavoriteItem;
    }


}