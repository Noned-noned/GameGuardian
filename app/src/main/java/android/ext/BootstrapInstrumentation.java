package android.ext;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class BootstrapInstrumentation extends Instrumentation {
    protected static BootstrapInstrumentation mInstance;
    protected static boolean mIsBootstraped = false;
    protected static boolean mServiceStarted = false;

    public BootstrapInstrumentation() {
    }

    static {
    }

    protected static boolean isBootstraped() {
        return mIsBootstraped;
    }

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        Log.d("BulldogService", "Instrumentation onCreate");
        mInstance = this;
        mIsBootstraped = true;
        start();
    }

    public static Intent getStartIntent(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            try {
                intent = new Intent("android.intent.action.MAIN").setClass(context, Class.forName(String.valueOf(packageName) + ".Main"));
            } catch (ClassNotFoundException e) {
                Log.e("BulldogService", "load Activity fail", e);
                throw new RuntimeException("Failed start Activity Main", e);
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static void startActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Looper.prepare()")) {
                Looper.prepare();
                context.startActivity(intent);
                return;
            }
            throw e;
        }
    }

    public static void startActivity(Context context, String packageName) {
        startActivity(context, getStartIntent(context, packageName));
    }

    public void onStart() {
        super.onStart();
        Log.d("BulldogService", "Instrumentation onStart");
        Log.d("BulldogService", "startActivity ACTION_MAIN");
        Context context = getTargetContext();
        startActivity(context, context.getPackageName());
    }

    public void onDestroy() {
        Log.d("BulldogService", "Instrumentation onDestroy");
        mServiceStarted = false;
    }
}