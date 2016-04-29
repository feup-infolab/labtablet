package pt.up.fe.alpha.labtablet.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ricardo on 29-04-2016.
 */
public class Dictionary {
    private HashMap<String, ArrayList<String>> items;

    public HashMap<String, ArrayList<String>> getItems() {
        return items;
    }

    public void setItems(HashMap<String, ArrayList<String>> items) {
        this.items = items;
    }
}
