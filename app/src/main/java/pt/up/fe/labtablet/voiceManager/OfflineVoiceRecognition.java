package pt.up.fe.labtablet.voiceManager;

import android.os.CountDownTimer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import pt.up.fe.labtablet.activities.FieldModeActivity;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class OfflineVoiceRecognition implements RecognitionListener {
    final String TAG = "OfflineVoiceRecognition";

    private SpeechRecognizer recognizer;
    private FieldModeActivity fieldModeActivity;

    private TTSvoice voice;

    protected CountDownTimer mTimer;


    public boolean isAwake;

    //Named searches
    private static final String ORDERS = "orders";


    public static final String OFF_BATTERY = "battery", OFF_INTERNET = "internet",
            OFF_LUMINOSITY = "light", OFF_MAGNETIC = "magnetic", OFF_GOODBYE = "shutdown",
            OFF_POSITION = "position", OFF_RECORD = "record";


    public OfflineVoiceRecognition(FieldModeActivity fieldModeActivity) {
        super();
        this.fieldModeActivity = fieldModeActivity;
        voice = fieldModeActivity.getTTSvoice();
        recognizer = null;
        isAwake = true;

    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        recognizer.stop();

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        //Log.i(TAG, "partial result");

        if (hypothesis == null)
            return;

    }

    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();


            if (text.equalsIgnoreCase(OFF_GOODBYE) &&
                    fieldModeActivity.getSw_handsFree().isChecked())
                fieldModeActivity.getSw_handsFree().performClick();


            if (text.equalsIgnoreCase(OFF_BATTERY)) {
                if (!fieldModeActivity.getBt_temperature_sample().isEnabled()) {
                    fieldModeActivity.getTTSvoice().sayBtnDisabled();
                } else
                    fieldModeActivity.getBt_temperature_sample().performClick();
            } else if (text.equalsIgnoreCase(OFF_POSITION)) {
                if (!fieldModeActivity.getBt_location().isEnabled()) {
                    fieldModeActivity.getTTSvoice().sayBtnDisabled();
                } else
                    fieldModeActivity.getBt_location().performClick();
            }else if (text.equalsIgnoreCase(OFF_RECORD)) {
                if (!fieldModeActivity.getBt_audio().isEnabled()) {
                    fieldModeActivity.getTTSvoice().sayBtnDisabled();
                } else
                    fieldModeActivity.getBt_audio().performClick();
            } else if (text.equalsIgnoreCase(OFF_LUMINOSITY)) {
                if (!fieldModeActivity.getBt_luminosity_sample().isEnabled()) {
                    fieldModeActivity.getTTSvoice().sayBtnDisabled();
                } else
                    fieldModeActivity.getBt_luminosity_sample().performClick();
            } else if (text.equalsIgnoreCase(OFF_INTERNET)) {
                if (!fieldModeActivity.getBt_network_temperature_sample().isEnabled()) {
                    fieldModeActivity.getTTSvoice().sayBtnDisabled();
                } else
                    fieldModeActivity.getBt_network_temperature_sample().performClick();
            } else if (text.equalsIgnoreCase(OFF_MAGNETIC)) {
                if (!fieldModeActivity.getBt_magnetic_sample().isEnabled()) {
                    fieldModeActivity.getTTSvoice().sayBtnDisabled();
                } else
                    fieldModeActivity.getBt_magnetic_sample().performClick();
            }


            recognizer.stop();

        } else {
            recognizer.stop();
            recognizer.startListening(ORDERS);

        }

    }

    @Override
    public void onError(Exception e) {
        Log.e("on error", e.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.e("on timeout", "timeout");
        recognizer.stop();
        recognizer.startListening(ORDERS);
    }


    public void setupRecognizer(File assetsDir) throws IOException {

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir)
                .setKeywordThreshold(1e-45f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();

        recognizer.addListener(this);

        File ordersKeywords = new File(assetsDir, "ordersKeywords.gram");
        recognizer.addKeywordSearch(ORDERS, ordersKeywords);

    }

    public void startListen() {
        if(recognizer != null) recognizer.startListening(ORDERS);
    }



    public SpeechRecognizer getRecognizer() {
        return recognizer;
    }

    public void shutdown() {
        if(recognizer == null) return;
        recognizer.cancel();
        recognizer.shutdown();
    }




}
