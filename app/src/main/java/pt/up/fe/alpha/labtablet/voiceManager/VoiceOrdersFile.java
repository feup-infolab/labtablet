package pt.up.fe.alpha.labtablet.voiceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import pt.up.fe.alpha.R;

public class VoiceOrdersFile implements Serializable{


    public static String TAG = "VoiceOrdersFile";
    transient Context context;

    //map keys
    public static String LANGUAGE = "language", GOODBYE = "goodbye", BATTERY = "battery",
            INTERNET = "internet", LUMINOSITY = "luminosity", MAGNETIC = "magnetic",
            GPS = "gps", NOTE = "note", RECORD = "record", DESCRIPTOR = "descriptor",
            POSITION = "position", CANCEL_NOTE = "cancel_note", SAVE_NOTE = "save_note",
            CANCEL_DESC_VALUE = "cancel_descriptor_value", SAVE_DESC_VALUE = "save_descriptor_value",
            VOICE = "voice", WARN_SOUNDS = "warnings", YES = "yes", NO = "no", VOICE_SPEED = "voice_speed";

    //shared preferences
    public static SharedPreferences savedKeywords_pt, savedKeywords_eng, pref_lang, voice_speed;
    public static SharedPreferences pref_sound;

    //flags for current application
    public static String currentLang;
    public static boolean voiceOn = true, warningsOn = true;
    public static float current_voice_speed;


    public VoiceOrdersFile(Context context){
        this.context = context;

       /*context.getSharedPreferences(
                "voiceRec_keywords_pt",
                Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(
                "voiceRec_keywords_eng",
                Context.MODE_PRIVATE).edit().clear().commit();
        */


        pref_lang = context.getSharedPreferences("voiceRec_pref_lang", Context.MODE_PRIVATE);
        currentLang = pref_lang.getString(VoiceOrdersFile.LANGUAGE, Locale.getDefault().getLanguage().toLowerCase());

        Log.i("currentLang", currentLang);

        voice_speed = context.getSharedPreferences("voiceSpeed_pref", Context.MODE_PRIVATE);
        current_voice_speed = voice_speed.getFloat(VoiceOrdersFile.VOICE_SPEED, 1.0f);

        savedKeywords_pt = context.getSharedPreferences(
               "voiceRec_keywords_pt",
                Context.MODE_PRIVATE);

        savedKeywords_eng = context.getSharedPreferences(
                "voiceRec_keywords_eng",
                Context.MODE_PRIVATE);



        if(savedKeywords_pt.getAll().isEmpty())
        {
            Resources standardResources = context.getResources();
            AssetManager assets = standardResources.getAssets();
            DisplayMetrics metrics = standardResources.getDisplayMetrics();
            Configuration config = new Configuration(standardResources.getConfiguration());
            config.locale = new Locale("pt");
            new Resources(assets, metrics, config);
            loadDefaultKeywords(savedKeywords_pt);


            Iterator it = savedKeywords_pt.getAll().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                Log.i(pair.getKey().toString(),pair.getValue().toString());
                it.remove();
            }

        }

        if(savedKeywords_eng.getAll().isEmpty())
        {
            Resources standardResources = context.getResources();
            AssetManager assets = standardResources.getAssets();
            DisplayMetrics metrics = standardResources.getDisplayMetrics();
            Configuration config = new Configuration(standardResources.getConfiguration());
            config.locale = new Locale("en");
            new Resources(assets, metrics, config);
            loadDefaultKeywords(savedKeywords_eng);

            Iterator it = savedKeywords_eng.getAll().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                Log.i(pair.getKey().toString(),pair.getValue().toString());
                it.remove();
            }

        }

        pref_sound = context.getSharedPreferences(
                "voiceRec_sound",
                Context.MODE_PRIVATE);

        voiceOn = pref_sound.getBoolean(VOICE, true);
        warningsOn = pref_sound.getBoolean(WARN_SOUNDS, true);

    }

    public void resetKeywords() {

        if(currentLang.equals("en"))
        {
            savedKeywords_eng.edit().clear().apply();
            loadDefaultKeywords(savedKeywords_eng);
        }
        else if(currentLang.equals("pt"))
        {
            savedKeywords_pt.edit().clear().apply();
            loadDefaultKeywords(savedKeywords_pt);
        }


    }

    public void resetVoiceSpeed(){
        voice_speed.edit().clear().apply();
        loadDefaultVoiceSpeed();
    }

    public void loadDefaultKeywords(SharedPreferences sp){

        try {
            //reset keywords
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(GOODBYE, context.getResources().getString(R.string.kw_goodbye));
            editor.putString(BATTERY, context.getResources().getString(R.string.battery_temp_vr));
            editor.putString(INTERNET, context.getResources().getString(R.string.network_temp_vr));
            editor.putString(LUMINOSITY, context.getResources().getString(R.string.luminosity));
            editor.putString(MAGNETIC, context.getResources().getString(R.string.magnetic));
            editor.putString(GPS, context.getResources().getString(R.string.continuous_gps));
            editor.putString(NOTE, context.getResources().getString(R.string.note));
            editor.putString(DESCRIPTOR, context.getResources().getString(R.string.collect_descriptor));
            editor.putString(POSITION, context.getResources().getString(R.string.position));
            editor.putString(RECORD,  context.getResources().getString(R.string.record));
            editor.putString(CANCEL_NOTE, context.getResources().getString(R.string.cancel_note));
            editor.putString(SAVE_NOTE, context.getResources().getString(R.string.save_note));
            editor.putString(CANCEL_DESC_VALUE, context.getResources().getString(R.string.cancel_value));
            editor.putString(SAVE_DESC_VALUE, context.getResources().getString(R.string.save_value));
            editor.putString(NO,  context.getResources().getString(R.string.no));
            editor.putString(YES,  context.getResources().getString(R.string.yes));

            editor.apply();

            Resources standardResources = context.getResources();
            AssetManager assets = standardResources.getAssets();
            DisplayMetrics metrics = standardResources.getDisplayMetrics();
            Configuration config = new Configuration(standardResources.getConfiguration());
            config.locale = Locale.getDefault();
            new Resources(assets, metrics, config);




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadDefaultVoiceSpeed(){
        VoiceOrdersFile.current_voice_speed = 1.0f;
        SharedPreferences.Editor editor = voice_speed.edit();
        editor.putFloat(VoiceOrdersFile.VOICE_SPEED, VoiceOrdersFile.current_voice_speed);
        editor.apply();

    }

}
