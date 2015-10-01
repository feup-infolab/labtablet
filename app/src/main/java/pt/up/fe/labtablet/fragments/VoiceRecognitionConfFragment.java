package pt.up.fe.labtablet.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.utils.Utils;
import pt.up.fe.labtablet.voiceManager.VoiceOrdersFile;


public class VoiceRecognitionConfFragment extends Fragment {

    Spinner spnLang;
    VoiceOrdersFile voiceOrdersFile;

    private EditText edtTxtBat;
    private EditText edtTxtGoodbye;
    private EditText edtTxtNetwork;
    private EditText edtTxtLum;
    private EditText edtTxtMagnetic;
    private EditText edtTxtGPS;
    private EditText edtTxtNote;
    private EditText edtTxtCancelNote;
    private EditText edtTxtSaveNote;
    private EditText edtTxtPosition;
    private EditText edtTxtRecord;
    private EditText edtTxtDescriptor;
    private EditText edtTxtSaveValue;
    private EditText edtTxtCancelValue;
    private EditText edtTxtYes;
    private EditText edtTxtNo;
    private SeekBar seekBar;

    public static boolean saved = true;

    private ImageView imgSaved;
    private TextView txtSaved;
    //private Switch sw_voice, sw_sound;

    String str_saved, str_not_saved;
    private Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        this.voiceOrdersFile = (VoiceOrdersFile) this.getArguments().getSerializable("voiceOrders");

        View rootView = inflater.inflate(R.layout.fragment_voice_config, container, false);


        imgSaved = (ImageView) rootView.findViewById(R.id.imgSaved);
        txtSaved = (TextView) rootView.findViewById(R.id.txtSaved);

        str_saved = this.getResources().getText(R.string.tts_saved).toString();
        str_not_saved = this.getResources().getText(R.string.tts_not_saved).toString();

        btnSave = (Button) rootView.findViewById(R.id.btnSave);

        if(saved) markSaved();
        else markUnsaved();


        spnLang = (Spinner) rootView.findViewById(R.id.spinnerLang);

        ArrayAdapter<String> adapter;
        List<String> list;

        list = new ArrayList<>();
        list.add("PT");
        list.add("EN");
        adapter = new ArrayAdapter<>(container.getContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLang.setAdapter(adapter);


        spnLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, final View selectedItemView, int position, long id) {

                if (!saved) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(parentView.getContext());

                    alert.setTitle(R.string.save_changes_current_lang_title);
                    alert.setMessage(R.string.save_changes_current_lang);

                    alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            btnSave.performClick();
                            changeLangKeywords();

                        }
                    });

                    alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            changeLangKeywords();

                        }
                    });

                    alert.show();
                } else changeLangKeywords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setProgress(Utils.getIntFromFloat(VoiceOrdersFile.current_voice_speed));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Toast.makeText(seekBar.getContext(), "Value: " + getFloatFromInt(progress), Toast.LENGTH_SHORT).show();

                changeVoiceSpeed(Utils.getFloatFromInt(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        edtTxtGoodbye = (EditText) rootView.findViewById(R.id.goodbye);
        edtTxtBat = (EditText) rootView.findViewById(R.id.battery);
        edtTxtNetwork = (EditText) rootView.findViewById(R.id.network);
        edtTxtLum = (EditText) rootView.findViewById(R.id.luminosity);
        edtTxtMagnetic = (EditText) rootView.findViewById(R.id.magnetic);
        edtTxtGPS = (EditText) rootView.findViewById(R.id.gps);

        edtTxtNote = (EditText) rootView.findViewById(R.id.note);

        edtTxtCancelNote = (EditText) rootView.findViewById(R.id.cancel_note);

        edtTxtSaveNote = (EditText) rootView.findViewById(R.id.save_note);

        edtTxtPosition = (EditText) rootView.findViewById(R.id.position);

        edtTxtRecord= (EditText) rootView.findViewById(R.id.record);

        edtTxtDescriptor = (EditText) rootView.findViewById(R.id.descriptor);

        edtTxtSaveValue = (EditText) rootView.findViewById(R.id.save_descriptor);

        edtTxtCancelValue = (EditText) rootView.findViewById(R.id.cancel_descriptor);

        edtTxtYes = (EditText) rootView.findViewById(R.id.yes);
        edtTxtNo = (EditText) rootView.findViewById(R.id.no);



        loadKeywordsToEdtTxt();

        LinearLayout savedInfo = (LinearLayout) rootView.findViewById(R.id.savedInfo);
        savedInfo.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View view) {
                btnSave.performClick();
                return false;
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = null;
                if(VoiceOrdersFile.currentLang.equalsIgnoreCase("en"))
                    editor  = VoiceOrdersFile.savedKeywords_eng.edit();
                else if(VoiceOrdersFile.currentLang.equalsIgnoreCase("pt"))
                    editor  = VoiceOrdersFile.savedKeywords_pt.edit();


                editor.putString(VoiceOrdersFile.GOODBYE, edtTxtGoodbye.getText().toString());
                editor.putString(VoiceOrdersFile.BATTERY, edtTxtBat.getText().toString());
                editor.putString(VoiceOrdersFile.INTERNET, edtTxtNetwork.getText().toString());
                editor.putString(VoiceOrdersFile.LUMINOSITY, edtTxtLum.getText().toString());
                editor.putString(VoiceOrdersFile.MAGNETIC, edtTxtMagnetic.getText().toString());
                editor.putString(VoiceOrdersFile.GPS, edtTxtGPS.getText().toString());
                editor.putString(VoiceOrdersFile.NOTE, edtTxtNote.getText().toString());
                editor.putString(VoiceOrdersFile.DESCRIPTOR, edtTxtDescriptor.getText().toString());
                editor.putString(VoiceOrdersFile.POSITION, edtTxtPosition.getText().toString());
                editor.putString(VoiceOrdersFile.RECORD, edtTxtRecord.getText().toString());
                editor.putString(VoiceOrdersFile.CANCEL_NOTE, edtTxtCancelNote.getText().toString());
                editor.putString(VoiceOrdersFile.SAVE_NOTE, edtTxtSaveNote.getText().toString());
                editor.putString(VoiceOrdersFile.CANCEL_DESC_VALUE, edtTxtCancelValue.getText().toString());
                editor.putString(VoiceOrdersFile.SAVE_DESC_VALUE, edtTxtSaveValue.getText().toString());
                editor.putString(VoiceOrdersFile.YES, edtTxtYes.getText().toString());
                editor.putString(VoiceOrdersFile.NO, edtTxtNo.getText().toString());

                editor.apply();


                savePrefLang();

                markSaved();

            }
        });

        final Context context = this.getActivity();

        Button btnReset = (Button) rootView.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                alert.setTitle(R.string.confirm_default_conf_voice_rec_title);
                alert.setMessage(R.string.confirm_default_conf_voice_rec);

                alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        voiceOrdersFile.resetKeywords();
                        loadKeywordsToEdtTxt();

                        voiceOrdersFile.resetVoiceSpeed();
                        seekBar.setProgress(Utils.getIntFromFloat(VoiceOrdersFile.current_voice_speed));
                        markUnsaved();
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                alert.show();

            }
        });


        edtTxtGoodbye.addTextChangedListener(checkSaved);
        edtTxtBat.addTextChangedListener(checkSaved);
        edtTxtNetwork.addTextChangedListener(checkSaved);
        edtTxtLum.addTextChangedListener(checkSaved);
        edtTxtMagnetic.addTextChangedListener(checkSaved);
        edtTxtGPS.addTextChangedListener(checkSaved);
        edtTxtNote.addTextChangedListener(checkSaved);
        edtTxtCancelNote.addTextChangedListener(checkSaved);
        edtTxtSaveNote.addTextChangedListener(checkSaved);
        edtTxtPosition.addTextChangedListener(checkSaved);
        edtTxtRecord.addTextChangedListener(checkSaved);
        edtTxtDescriptor.addTextChangedListener(checkSaved);
        edtTxtSaveValue.addTextChangedListener(checkSaved);
        edtTxtCancelValue.addTextChangedListener(checkSaved);
        edtTxtYes.addTextChangedListener(checkSaved);
        edtTxtNo.addTextChangedListener(checkSaved);




        /*sw_sound = (Switch) rootView.findViewById(R.id.sw_sound);
        sw_voice = (Switch) rootView.findViewById(R.id.sw_voice);

        if(VoiceOrdersFile.voiceOn) sw_voice.setChecked(true);
        if(VoiceOrdersFile.warningsOn) sw_sound.setChecked(true);


        sw_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePrefSound();
                VoiceOrdersFile.voiceOn = sw_voice.isChecked();
            }
        });

        sw_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePrefSound();
                VoiceOrdersFile.warningsOn = sw_sound.isChecked();
            }
        });
*/

        return rootView;
    }

    private void savePrefLang() {
        SharedPreferences.Editor editor  = VoiceOrdersFile.pref_lang.edit();
        editor.putString(VoiceOrdersFile.LANGUAGE, VoiceOrdersFile.currentLang);
        editor.apply();

    }

    private void saveVoiceSpeed() {
        SharedPreferences.Editor editor  = VoiceOrdersFile.voice_speed.edit();
        editor.putFloat(VoiceOrdersFile.VOICE_SPEED, VoiceOrdersFile.current_voice_speed);
        editor.apply();

    }

    /*private void savePrefSound() {
        SharedPreferences.Editor editor  = VoiceOrdersFile.pref_sound.edit();
        editor.putBoolean(VoiceOrdersFile.VOICE, sw_voice.isChecked());
        editor.putBoolean(VoiceOrdersFile.WARN_SOUNDS, sw_sound.isChecked());
        editor.apply();

    }*/


    TextWatcher checkSaved = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            markUnsaved();
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    void loadKeywordsToEdtTxt(){

        Map<String, String> keywordsMap;


        if(VoiceOrdersFile.currentLang.equals("pt"))
        {
            spnLang.setSelection(0);
            keywordsMap = (Map<String, String>) VoiceOrdersFile.savedKeywords_pt.getAll();
        }
        else {
            spnLang.setSelection(1);
            keywordsMap = (Map<String, String>) VoiceOrdersFile.savedKeywords_eng.getAll();
        }

        edtTxtGoodbye.setText(keywordsMap.get(VoiceOrdersFile.GOODBYE));
        edtTxtBat.setText(keywordsMap.get(VoiceOrdersFile.BATTERY));
        edtTxtNetwork.setText(keywordsMap.get(VoiceOrdersFile.INTERNET));
        edtTxtLum.setText(keywordsMap.get(VoiceOrdersFile.LUMINOSITY));
        edtTxtMagnetic.setText(keywordsMap.get(VoiceOrdersFile.MAGNETIC));
        edtTxtGPS.setText(keywordsMap.get(VoiceOrdersFile.GPS));
        edtTxtNote.setText(keywordsMap.get(VoiceOrdersFile.NOTE));
        edtTxtCancelNote.setText(keywordsMap.get(VoiceOrdersFile.CANCEL_NOTE));
        edtTxtSaveNote.setText(keywordsMap.get(VoiceOrdersFile.SAVE_NOTE));
        edtTxtPosition.setText(keywordsMap.get(VoiceOrdersFile.POSITION));
        edtTxtRecord.setText(keywordsMap.get(VoiceOrdersFile.RECORD));
        edtTxtDescriptor.setText(keywordsMap.get(VoiceOrdersFile.DESCRIPTOR));
        edtTxtSaveValue.setText(keywordsMap.get(VoiceOrdersFile.SAVE_DESC_VALUE));
        edtTxtCancelValue.setText(keywordsMap.get(VoiceOrdersFile.CANCEL_DESC_VALUE));
        edtTxtYes.setText(keywordsMap.get(VoiceOrdersFile.YES));
        edtTxtNo.setText(keywordsMap.get(VoiceOrdersFile.NO));



    }



    void markSaved(){
        saved = true;
        imgSaved.setImageResource(R.drawable.ic_check);
        txtSaved.setText(str_saved);
        btnSave.setText(str_saved);
        btnSave.setEnabled(false);
    }

    void markUnsaved(){
        saved = false;
        imgSaved.setImageResource(R.drawable.ic_warning);
        txtSaved.setText(str_not_saved);
        btnSave.setText(R.string.save);
        btnSave.setEnabled(true);
    }


    void changeLangKeywords(){
        VoiceOrdersFile.currentLang = spnLang.getSelectedItem().toString().toLowerCase();

        savePrefLang();

        loadKeywordsToEdtTxt();

        markSaved();
    }



    void changeVoiceSpeed(float newspeed){
        VoiceOrdersFile.current_voice_speed = newspeed;

        saveVoiceSpeed();
    }

    @Override
    public void onPause() {
        super.onPause();



    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("voice rec fragment", "stop");
    }




}
