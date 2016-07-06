package pt.up.fe.alpha.labtablet.models.SeaBioData;

import java.util.ArrayList;

/**
 * Class for the entity lookup when configuring the local storage
 * for the SeaBioData project
 */
public class EntityResponse {
    private String status;
    private ArrayList<Data> data;

    public ArrayList<Data> getData() {
        return data;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
