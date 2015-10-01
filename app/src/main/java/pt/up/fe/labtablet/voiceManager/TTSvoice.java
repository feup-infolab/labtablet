package pt.up.fe.labtablet.voiceManager;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.activities.FieldModeActivity;
import pt.up.fe.labtablet.utils.Utils;

public class TTSvoice {
    private final String TAG = "TTSvoice";
    FieldModeActivity fieldMode;
    TextToSpeech voice;

    /* Utterance IDs */
    public static final String UID_HELLO = "hello";
    public static final String UID_GOODBYE = "goodbye";
    public static final String UID_OP_DISABLED = "option_disabled";
    public static final String UID_DESC_INVALID = "invalid_descriptor";
    public static final String UID_CANCELED = "canceled";
    public static final String UID_SAVED = "saved";
    public static final String UID_DESC_ADDED = "added_descriptor";
    public static final String UID_DESC_WATING = "wating_for_descriptor";
    public static final String UID_DESC_VAL_WATING = "wating_for_descriptor_value";
    public static final String UID_SENSOR_SAVED = "sensor_saved";
    public static final String UID_TEXT_WAITING = "wating_for_text_input";
    public static final String UID_NOT_SAVED = "not_saved";
    public static final String UID_LOST_CONN = "lost_conn";
    public static final String UID_GPS_DISABLED = "gps_disabled";

    public static boolean voiceSpeaking;

    public TTSvoice(final FieldModeActivity fieldMode) {

        this.fieldMode = fieldMode;
        voiceSpeaking = false;

        final TTSvoice ttSvoice = this;
        this.voice = new TextToSpeech(fieldMode.getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    voice.setLanguage(new Locale(Locale.getDefault().getLanguage()));
                    Log.d(TAG, "on init");
                    voice.setSpeechRate(VoiceOrdersFile.current_voice_speed);
                    Log.i(TAG + "speed",VoiceOrdersFile.current_voice_speed+"");

                    sayHello();

                    if (Build.VERSION.SDK_INT >= 15) {

                        voice.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onDone(String utteranceId) {
                                Log.i(TAG, utteranceId);


                                switch (utteranceId) {
                                    case UID_HELLO:

                                        if (Utils.isOnline(fieldMode)) {//try with google recognition if possible
                                            fieldMode.turnOnGoogleRec();
                                            
                                        } else { //try with offline recognition
                                            fieldMode.turnOnSphinxRec();
                                        }

                                        break;
                                    case UID_GOODBYE:
                                        Log.i(TAG, "shutting down");
                                        ttSvoice.shutdown();
                                        fieldMode.shutdownRecognizer();


                                        break;
                                    case UID_DESC_WATING:
                                        Log.i(TAG, UID_DESC_WATING);
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_DESC_NAME;
                                        fieldMode.getGoogleRecognizer().restart();

                                        break;
                                    case UID_DESC_INVALID:
                                        Log.i(TAG, UID_DESC_INVALID);

                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_DESC_NAME;
                                        fieldMode.getGoogleRecognizer().restart();

                                        break;
                                    case UID_DESC_ADDED:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_ORDER;

                                        break;
                                    case UID_TEXT_WAITING:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_TAKING_NOTE;
                                        fieldMode.getGoogleRecognizer().restart();
                                        break;
                                    case UID_CANCELED:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_ORDER;
                                        fieldMode.getGoogleRecognizer().resetAndRestart();

                                        break;
                                    case UID_SAVED:
                                    case UID_NOT_SAVED:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_ORDER;
                                        fieldMode.getGoogleRecognizer().resetAndRestart();
                                        break;
                                    case UID_DESC_VAL_WATING:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_DESC_VALUE;
                                        fieldMode.getGoogleRecognizer().restart();
                                        break;
                                    case UID_OP_DISABLED:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_ORDER;
                                        fieldMode.getGoogleRecognizer().resetAndRestart();
                                        break;
                                    case UID_LOST_CONN:
                                        fieldMode.getSw_handsFree().performClick();

                                        break;
                                    case UID_SENSOR_SAVED:
                                        if (fieldMode.getOfflineRecognizer() != null)
                                            fieldMode.getOfflineRecognizer().startListen();
                                        break;
                                    case UID_GPS_DISABLED:
                                        fieldMode.getGoogleRecognizer().currentAction = GoogleVoiceRecognition.AC_GPS;
                                        break;
                                }

                              /*  if(!VoiceOrdersFile.warningsOn)
                                    fieldMode.getGoogleRecognizer().audioMute();*/


                                voiceSpeaking = false;

                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.i("Utterance", "onError");

                                //restart stuff TODO


                            }

                            @Override
                            public void onStart(String utteranceId) {
                                Log.i("Utterance", "onStart");
                            }
                        });
                    } else {
                        Log.d(TAG, "set utternace completed listener");
                        voice.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String utteranceId) {
                                Log.i(TAG, utteranceId);
                            }
                        });
                    }
                } else {
                    voice = null;
                    Log.e(TAG, "voice failed!");

                }
            }
        });

    }

    public void speakText(String text, int queueMode, String id) {

        if (voice != null) {
            if (text != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    voice.speak(text, queueMode, null, id);
                    //voice.setSpeechRate()
                } else {
                    HashMap<String, String> hashTts = new HashMap<>();
                    hashTts.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
                    voice.speak(text, queueMode, hashTts);
                }
            }

        }


        Toast.makeText(fieldMode.getApplicationContext(), text,Toast.LENGTH_SHORT).show();
    }

    public void shutdown() {
        Log.i(TAG, "TTSvoice shutdown");
        if (voice != null) {
            voice.stop();
            voice.shutdown();
        }
    }


    /********************************/
    /** ADD HERE SPEAKING ORDERS ***/
    /********************************/

    public void sayHello() {
        String text = fieldMode.getApplicationContext().getString(R.string.tts_hello);

        Log.i(TAG, text);
        speakText(text, TextToSpeech.QUEUE_FLUSH, UID_HELLO);


    }

    public void sayGoodbye() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_goodbye), TextToSpeech.QUEUE_FLUSH, UID_GOODBYE);

    }


    public void sayBtnDisabled() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_option_disabled), TextToSpeech.QUEUE_FLUSH, UID_OP_DISABLED);
    }

    public void askForDescriptor() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_ask_for_descriptor), TextToSpeech.QUEUE_FLUSH, UID_DESC_WATING);
    }


    public void informInvalidDescName(String heardDescriptor) {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        String text = fieldMode.getApplicationContext().getString(R.string.tts_heard) + " " + heardDescriptor + ". "
                +fieldMode.getApplicationContext().getString(R.string.tts_invalid_descriptor);
        speakText(text, TextToSpeech.QUEUE_FLUSH, UID_DESC_INVALID);
    }

    public void informCanceled() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_canceled), TextToSpeech.QUEUE_FLUSH, UID_CANCELED);
    }

    public void informSaved() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_saved), TextToSpeech.QUEUE_FLUSH, UID_NOT_SAVED);
    }

    public void informNotSaved() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_not_saved), TextToSpeech.QUEUE_FLUSH, UID_SAVED);
    }

    public void informDescriptorAdded() {
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getApplicationContext().getString(R.string.tts_desc_added), TextToSpeech.QUEUE_FLUSH, UID_DESC_ADDED);
    }

    public void informWatingTextRecord(){
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getResources().getString(R.string.tts_new_text_record), TextToSpeech.QUEUE_FLUSH, TTSvoice.UID_TEXT_WAITING);
    }

    public void askForDescriptorValue(){
        if( fieldMode.getGoogleRecognizer() != null) fieldMode.getGoogleRecognizer().audioUnmute();
        speakText(fieldMode.getResources().getString(R.string.tts_ask_for_descriptor_value), TextToSpeech.QUEUE_FLUSH,
                TTSvoice.UID_DESC_VAL_WATING);
    }



}
