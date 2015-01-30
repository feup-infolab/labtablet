package pt.up.fe.labtablet.application;

import android.app.Application;
import android.content.Context;

//import org.acra.*;
//import org.acra.annotation.*;
/*
@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        formUri = "http://paginas.fe.up.pt/~ei08103/ACRA/dump.php"
)*/
public class LabTablet extends Application {

    private static Context mContext;
    //private static LabTablet mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //mInstance = this;
        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
    }

    public static Context getContext(){
        return mContext;
    }
}
