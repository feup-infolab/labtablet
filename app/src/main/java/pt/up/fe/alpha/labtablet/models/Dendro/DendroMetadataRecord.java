package pt.up.fe.alpha.labtablet.models.Dendro;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class DendroMetadataRecord {
    private final String uri;
    private final Object value;
    //private final List<String> value;

    public DendroMetadataRecord(String uri, Object value) {
        this.uri = uri;
        this.value = value;
    };

    /*
    public DendroMetadataRecord(String uri, String value) {
        this.uri = uri;
        this.value = new ArrayList<String>();
        this.value.add(value);
    };*/

}

/*
public class DendroMetadataRecord{

    @SerializedName ("uri");
    String mUri;

    //don't assign any serialized name, this field will be parsed manually
    List<OptionValue> mValue;

    //setter
    public void setOptionValues(List<OptionValue> value){
        mValue = value;
    }

    // get set stuff here
    public class OptionValue
    {
        String uri;
        String value;
        // get set stuff here
    }

    public static class DendroMetadataRecordDeserilizer implements JsonDeserializer<DendroMetadataRecord> {

        @Override
        public DendroMetadataRecord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DendroMetadataRecord dendroMetadataRecord = new Gson().fromJson(json, DendroMetadataRecord.class);
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("value")) {
                JsonElement elem = jsonObject.get("value");
                if (elem != null && !elem.isJsonNull()) {
                    String valuesString = elem.getAsString();
                    if (!TextUtils.isEmpty(valuesString)){
                        List<OptionValue> values = new Gson().fromJson(valuesString, new TypeToken<ArrayList<OptionValue>>() {}.getType());
                        dendroMetadataRecord.setOptionValues(values);
                    }
                }
            }
            return dendroMetadataRecord ;
        }
    }
}*/