package pt.up.fe.alpha.labtablet.voiceManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.activities.FieldModeActivity;
import pt.up.fe.alpha.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Created by Susana on 17-May-15.
 *
 */

public class GoogleVoiceRecognition implements
        RecognitionListener {
    static final String TAG = "GoogleVoiceRecognizer";

    private FieldModeActivity fieldModeActivity;



    private boolean paused;
    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected CountDownTimer mTimer;

    public static final String LANG_PT_PT = "pt";
    public static final String LANG_EN = "en";

    /**
     * ******
     */
    private EditText textRecord;
    private AlertDialog dialogTextInput, dialogGPS;
    private MultiAutoCompleteTextView autoCompleteTextView;


    public String currentAction;

    /* possible current actions. Used to decide what to do when user says something*/
    public static final String AC_TAKING_NOTE = "taking_note";
    public static final String AC_DESC_VALUE = "descriptor_value";
    public static final String AC_ORDER = "order";
    public static final String AC_DESC_NAME = "descriptor_name";
    public static final String AC_GPS = "gps";


    public void setTextRecord(EditText textRecord) {
        this.textRecord = textRecord;
    }

    public void setDialogTextInput(AlertDialog dialog) {
        this.dialogTextInput = dialog;
    }

    public void setDialogGPS(AlertDialog dialog) {
        this.dialogGPS = dialog;
    }



    public GoogleVoiceRecognition(FieldModeActivity fieldModeActivity) {
        super();
        this.fieldModeActivity = fieldModeActivity;

        mSpeechRecognizer = null;

        mAudioManager = (AudioManager) fieldModeActivity.getSystemService(Context.AUDIO_SERVICE);

        textRecord = null;

        paused = false;

        currentAction = AC_ORDER;

    }



    public void setupGoogleRecognizer() {
        final GoogleVoiceRecognition gvr = this;
        fieldModeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(fieldModeActivity);
                mSpeechRecognizer.setRecognitionListener(gvr);
                mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        Locale.getDefault().getLanguage());
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, VoiceOrdersFile.currentLang);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        fieldModeActivity.getPackageName());
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            }


        });
    }

    public void shutdown() {
        Log.d(TAG, "recognizer shutdown");

        paused = true;
        fieldModeActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mSpeechRecognizer != null) {
                    mSpeechRecognizer.cancel();
                    mSpeechRecognizer.destroy();
                    mSpeechRecognizer = null;
                    audioUnmute(); //reset all sounds
                }

            }
        });

        audioUnmute(); //reset all sounds
    }

    public SpeechRecognizer getRecognizer() {
        return mSpeechRecognizer;
    }

    public Intent getRecognizerIntent() {
        return mSpeechRecognizerIntent;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginingOfSpeech");

       /* if (mTimer != null) {
            mTimer.cancel();
        }*/

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");

    }

    @Override
    public void onError(int error) {
        Log.d("error", getErrorText(error));
        Log.e("paused", paused + "");
        if (!paused) {

            if (mTimer != null) {
                mTimer.cancel();
            }

            Log.d(TAG, "error = " + getErrorText(error));

            resetAndRestart();
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d("current action", currentAction);

       /* if (mTimer != null) {
            mTimer.cancel();
        }
*/
    }

    public void resetAndRestart(){
        shutdown();
        setupGoogleRecognizer();
        restart();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");

        //if(!VoiceOrdersFile.warningsOn && !TTSvoice.voiceSpeaking)
          //  audioMute();


        if (mTimer != null) {
            mTimer.cancel();
        }

    }

    @Override
    public void onResults(Bundle results) {
        Log.d("Speech", "onResults");

        //If the timer is available, cancel it so it doesn't interrupt the result processing
        if (mTimer != null) {
            mTimer.cancel();
        }

        //Start processing data
        ArrayList<String> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String result = strlist.get(0);

        Log.d("Speech", "YOU SAID: " + result);
        //Log.d("current Order", currentAction);

        Map<String, String> kwords = null;
        switch (VoiceOrdersFile.currentLang) {
            case "en":
                kwords = (Map<String, String>) VoiceOrdersFile.savedKeywords_eng.getAll();
                break;
            case "pt":
                kwords = (Map<String, String>) VoiceOrdersFile.savedKeywords_pt.getAll();
                break;
            default:
                Log.e("GVR", "kwords null - ERROR");
                break;
        }

        switch (currentAction) {
            case AC_GPS:
                if (dialogGPS != null) {
                    if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.YES))) {
                        dialogGPS.getButton(Dialog.BUTTON_POSITIVE).performClick();
                    } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.NO))) {
                        dialogGPS.getButton(Dialog.BUTTON_NEGATIVE).performClick();
                    }
                } else Log.e("googleRec", "gps dialog null");
                break;
            case AC_TAKING_NOTE:  //some note

                if (textRecord != null) {

                    if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.CANCEL_NOTE))
                            || result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.CANCEL_NOTE) + "s")) {
                        dialogTextInput.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
                    } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.SAVE_NOTE))
                            || (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.SAVE_NOTE) + "s"))) {
                        dialogTextInput.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    } else {
                        if (textRecord.getText().toString().length() == 0)
                            textRecord.append(result);
                        else textRecord.append(" " + result);
                    }
                }
                break;
            case AC_DESC_NAME:  //descriptor name

                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANG_EN);

                if (result.equalsIgnoreCase(fieldModeActivity.getResources().getString(R.string.cancel))) {

                    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, VoiceOrdersFile.currentLang);
                    audioUnmute();
                    fieldModeActivity.getTTSvoice().informCanceled();
                } else {

                    ArrayList<Descriptor> descriptors = FavoriteMgr.getBaseDescriptors(fieldModeActivity);
                    boolean validDescriptor = false;
                    Descriptor selectedDescriptor = null;
                    for (Descriptor d : descriptors) {
                        if (d.getName().equalsIgnoreCase(result)) {
                            {
                                validDescriptor = true;
                                selectedDescriptor = d;
                            }
                            break;
                        }
                    }

                    //Log.d("validDescriptor", validDescriptor + "");

                    if (validDescriptor) {
                        pause();
                        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, VoiceOrdersFile.currentLang);
                        fieldModeActivity.getTTSvoice().askForDescriptorValue();

                        Log.i("Current lang", VoiceOrdersFile.currentLang);

                        selectedDescriptor.setState(Utils.DESCRIPTOR_STATE_VALIDATED);
                        AlertDialog.Builder builder = new AlertDialog.Builder(fieldModeActivity);
                        builder.setTitle(selectedDescriptor.getName());

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(fieldModeActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                selectedDescriptor.getAllowed_values()
                                        .toArray(new String[selectedDescriptor.getAllowed_values().size()]));

                        autoCompleteTextView = new MultiAutoCompleteTextView(fieldModeActivity);
                        autoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                        autoCompleteTextView.setAdapter(adapter);
                        autoCompleteTextView.setThreshold(1);

                        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                autoCompleteTextView.showDropDown();
                                return false;
                            }
                        });


                        // Set up the input
                        builder.setView(autoCompleteTextView);

                        // Set up the buttons

                        final Descriptor newDescriptor = selectedDescriptor;
                        builder.setPositiveButton(fieldModeActivity.getResources().getString(R.string.form_ok), new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (autoCompleteTextView.getText().toString().equals("")) {
                                            fieldModeActivity.getTTSvoice().informNotSaved();
                                            autoCompleteTextView.setError("The value shall not be empty.");
                                        } else {
                                            newDescriptor.setValue(autoCompleteTextView.getText().toString());
                                            fieldModeActivity.getCurrentFavoriteItem().addMetadataItem(newDescriptor);
                                            FavoriteMgr.updateFavoriteEntry(fieldModeActivity.getCurrentFavoriteItem().getTitle(),
                                                    fieldModeActivity.getCurrentFavoriteItem(), fieldModeActivity);

                                            dialog.dismiss();

                                            fieldModeActivity.getTTSvoice().informDescriptorAdded();

                                        }
                                    }
                                });

                        builder.setNegativeButton(fieldModeActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener
                                () {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Toast.makeText(fieldModeActivity.getApplication(), fieldModeActivity.getResources().getString(R.string.cancelled),
                                        Toast.LENGTH_SHORT).show();

                                audioUnmute();
                                fieldModeActivity.getTTSvoice().informCanceled();
                            }
                        });

                        final AlertDialog alertDialog = builder.show();

                        dialogTextInput = alertDialog;


                    } else {
                        if (VoiceOrdersFile.warningsOn) audioUnmute();
                        pause();

                        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANG_EN);
                        fieldModeActivity.getTTSvoice().informInvalidDescName(result);


                    }
                }


                break;
            case AC_DESC_VALUE:
                if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.CANCEL_DESC_VALUE))) {
                    dialogTextInput.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.SAVE_DESC_VALUE))) {
                    dialogTextInput.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                } else {
                    if (autoCompleteTextView.getText().toString().length() == 0)
                        autoCompleteTextView.append(result);
                    else autoCompleteTextView.append(" " + result);
                }

                break;
            case AC_ORDER:  //some order

                if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.NOTE)) || result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.NOTE) + "s")) {
                    pause();

                    fieldModeActivity.getBt_note().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.BATTERY))) {
                    audioUnmute(); //ttsVoice
                    if (!fieldModeActivity.getBt_temperature_sample().isEnabled()) {
                        fieldModeActivity.getTTSvoice().sayBtnDisabled();
                    } else
                        fieldModeActivity.getBt_temperature_sample().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.POSITION))) {
                    audioUnmute(); //ttsVoice
                    if (!fieldModeActivity.getBt_location().isEnabled()) {
                        fieldModeActivity.getTTSvoice().sayBtnDisabled();
                    } else
                        fieldModeActivity.getBt_location().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.RECORD))) {
                    audioUnmute(); //ttsVoice
                    if (!fieldModeActivity.getBt_audio().isEnabled()) {
                        fieldModeActivity.getTTSvoice().sayBtnDisabled();
                    } else
                        fieldModeActivity.getBt_audio().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.LUMINOSITY))) {
                    audioUnmute(); //ttsVoice
                    if (!fieldModeActivity.getBt_luminosity_sample().isEnabled()) {
                        fieldModeActivity.getTTSvoice().sayBtnDisabled();
                    } else
                        fieldModeActivity.getBt_luminosity_sample().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.INTERNET))) {
                    audioUnmute(); //ttsVoice
                    if (!fieldModeActivity.getBt_network_temperature_sample().isEnabled()) {
                        fieldModeActivity.getTTSvoice().sayBtnDisabled();
                    } else
                        fieldModeActivity.getBt_network_temperature_sample().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.MAGNETIC))) {
                    audioUnmute(); //ttsVoice
                    if (!fieldModeActivity.getBt_magnetic_sample().isEnabled()) {
                        fieldModeActivity.getTTSvoice().sayBtnDisabled();
                    } else
                        fieldModeActivity.getBt_magnetic_sample().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.GOODBYE)) &&
                        fieldModeActivity.getSw_handsFree().isChecked()) {
                    audioUnmute();
                    fieldModeActivity.getSw_handsFree().performClick();
                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.GPS))) {
                    audioUnmute();
                    fieldModeActivity.getSw_gps().performClick();


                } else if (result.equalsIgnoreCase(kwords.get(VoiceOrdersFile.DESCRIPTOR))) {
                    pause();
                    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANG_EN);
                    fieldModeActivity.getTTSvoice().askForDescriptor();

                }


                break;
        }

        if(!paused)
            restart();

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(TAG, "onRmsChanged");

    }


    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public void audioMute() {
Log.e("audio", "mute");
        mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    public void audioUnmute() {
        Log.e("audio", "unmute");
        mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }


    /*void informButtonDisabled(){
        fieldModeActivity.getTTSvoice().sayBtnDisabled();
        Toast.makeText(fieldModeActivity,
                fieldModeActivity.getResources().getString(R.string.tts_option_disabled),
                Toast.LENGTH_SHORT).show();
        }
*/
    public void pause() {
        if (mTimer != null) {
            mTimer.cancel();
        }

        if (mSpeechRecognizer != null) mSpeechRecognizer.cancel();
        paused = true;
        audioUnmute();
    }


    public void restart() {

        paused = false;
        fieldModeActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //Start listening again
                Log.d("Speech", "Start Listening");
                if (mSpeechRecognizerIntent == null) Log.e("timer", "mSpeechRecognizerIntent null");
                mSpeechRecognizer.cancel();
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);


                //Start a timer in case OnReadyForSpeech is never called back (Android Bug?)

                if (mTimer == null) {
                    Log.d("Speech", "Start a timer");
                    mTimer = new CountDownTimer(2000, 500) {
                        @Override
                        public void onTick(long l) {
                        }

                        @Override
                        public void onFinish() {
                            Log.d("Speech", "Timer.onFinish: Timer Finished, Restart recognizer");
                            Log.e(" timer paused", paused + "");
                            if (mSpeechRecognizer != null) {
                                mSpeechRecognizer.cancel();
                                if (mSpeechRecognizerIntent == null)
                                    Log.e("timer", "mSpeechRecognizerIntent null");
                                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                            } else Log.e("Times", "recognizer null");

                        }
                    };
                    mTimer.start();
                }


            }
        });

       /* if(!TTSvoice.voiceSpeaking)
            audioMute();*/


    }


}