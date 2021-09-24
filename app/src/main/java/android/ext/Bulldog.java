package android.ext;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import catch_.me_.if_.you_.can_.R;

public class Bulldog extends Activity {
    public static Context appContext;
    public static Bulldog instance;
    public static volatile boolean waitExit = false;
    private boolean installModel;

    public Bulldog() {
        instance = this;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.installModel = Installer.needInstall();
        if(this.installModel){
            setContentView(R.layout.activity_main);
            Installer.startInstall();
        }else {
            appContext = getApplicationContext();
            setContentView(R.layout.activity_main);
        }
    }
}
