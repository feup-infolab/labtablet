package pt.up.fe.labtablet.voiceManager;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import pt.up.fe.labtablet.activities.FieldModeActivity;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Susana on 04-May-15.
 */
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
        Log.i(TAG, "beginning of speech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "end of speech");
        recognizer.stop();

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        //Log.i(TAG, "partial result");

        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();

        Log.i("partial result:", text);


    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.i(TAG, "final result");


        if (hypothesis != null) {


            String text = hypothesis.getHypstr();
            Log.i("final result", text);


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
            Log.i(TAG, "hypothesis null");

            recognizer.stop();
            //Toast.makeText(fieldModeActivity.getApplicationContext(), "I did not understand. Please repeat it.",Toast.LENGTH_SHORT).show();
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

        // Create grammar-based search for digit recognition
        //File ordersGrammar = new File(assetsDir, "orders.gram");
        File ordersKeywords = new File(assetsDir, "ordersKeywords.gram");
        //File wakeupKeyword = new File(assetsDir, "wakeup.gram");
        File englishLm = new File(assetsDir, "cmusphinx-5.0-en-us.lm.dmp");

        //recognizer.addNgramSearch(ORDERS, englishLm);


        //recognizer.addKeywordSearch(WAKEUP_ORDER, wakeupKeyword);

        recognizer.addKeywordSearch(ORDERS, ordersKeywords);

    }

    public void startListen() {
        Log.d(TAG, "I'm listening");
        if(recognizer != null) recognizer.startListening(ORDERS);
    }



    public SpeechRecognizer getRecognizer() {
        return recognizer;
    }

    public void shutdown() {
        Log.d(TAG, "recognizer shutdown");
        if(recognizer == null) return;
        recognizer.cancel();
        recognizer.shutdown();
    }




}
