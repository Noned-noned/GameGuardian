package android.ext;

import android.content.Context;
import android.content.pm.PackageManager;

public class Tools {

    public static Context getContext() {
        return Bulldog.instance;
    }

    public static boolean isPackageInstalled(String pkg) {
        try {
            getContext().getPackageManager().getApplicationInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String removeNewLinesChars(String text) {
        StringBuilder fixed = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean skip = false;
            for (int j = 0; j < 2; j++) {
                if (skip || ch == 10) {
                    skip = true;
                } else {
                    skip = false;
                }
                if (j != 1) {
                    ch = (char) (ch - 3);
                }
            }
            if (skip) {
                fixed.append("");
            }
            fixed.append(ch);
        }
        return fixed.toString();
    }
}
