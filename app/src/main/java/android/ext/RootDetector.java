package android.ext;

import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class RootDetector {
    public static String debug = "";

    static {

    }

    public RootDetector(){}

    private static String getData(String name, InputStream is) {
        StringBuilder data = new StringBuilder(name);
        data.append(": ");
        try {
            int av = is.available();
            if (av > 0) {
                byte[] buf = new byte[av];
                data.append(new String(Arrays.copyOf(buf, is.read(buf))));
            }
        } catch (IOException e) {
            data.append(e.toString());
        }
        data.append("\n");
        return data.toString();
    }

    public static String getInfo(Process process) {
        StringBuilder dbg = new StringBuilder();
        if (process != null) {
            dbg.append(getData("stdout", process.getInputStream()));
            dbg.append(getData("stderr", process.getErrorStream()));
            dbg.append("exit value: ");
            try {
                dbg.append(process.exitValue());
            } catch (IllegalThreadStateException e) {
                dbg.append("already run");
            }
            dbg.append("\n");
        } else {
            dbg.append("process is null\n");
        }
        return dbg.toString();
    }

    private static String addPath() {
        return TextUtils.join(":", new String[]{"/vendor/bin", "/vendor/xbin", "/vendor/sbin", "/system/bin", "/system/xbin", "/system/sbin", "/sbin", "/bin", "/xbin", "/data/local/sbin", "/data/local/xbin", "/data/local/bin", "/system/sd/xbin", "/system/sd/sbin", "/system/sd/bin", "/system/bin/failsafe", "/system/xbin/failsafe", "/system/sbin/failsafe", "/system/xbin/bstk", "/system/sbin/bstk", "/system/bin/bstk", "/data/local"});
    }

    private static String[] getEnv() {
        Map<String, String> env = System.getenv();
        int size = env.size();
        int i = 0;
        String path = env.get("PATH");
        if (path == null) {
            size++;
        }
        String[] envp = new String[size];
        for (Map.Entry<String, String> e : env.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if ("PATH".equals(key)) {
                value = String.valueOf(value) + ":" + addPath();
            }
            envp[i] = String.valueOf(key) + "=" + value;
            i++;
        }
        if (path == null) {
            envp[i] = "PATH=" + addPath();
        }
        return envp;
    }

    public static Process tryRoot(String cmd) throws IOException {
        Process process = null;
        StringBuilder dbg = new StringBuilder();
        Log.d("BulldogService", "try: " + cmd);
        dbg.append("try: ");
        dbg.append(cmd);
        dbg.append("\n");
        String[] binary = {"asaua".replace("a", "")};
        int i = 0;
        while (i < 2) {
            switch (i) {
                case 0:
                    process = Runtime.getRuntime().exec(binary);
                    break;
                case 1:
                    process = Runtime.getRuntime().exec(binary, getEnv());
                    break;
            }
            try {
                DataOutputStream in = new DataOutputStream(process.getOutputStream());
                in.writeBytes(String.valueOf(cmd) + "\n");
                in.flush();
                dbg.append("process started\n");
            } catch (Throwable t) {
                for (Throwable ex = t; ex != null; ex = ex.getCause()) {
                    dbg.append(ex.toString());
                    dbg.append("\n");
                }
                dbg.append("\n");
                dbg.append(getInfo(process));
            }
            if (process == null) {
                i++;
            } else {
                debug = dbg.toString();
                return process;
            }
        }
        debug = dbg.toString();
        return process;
    }

    public static boolean runCmd(String paramString) throws IOException, InterruptedException {
        Process process = tryRoot(paramString);
        if (process == null) {
            Log.e("RootDetector","cmd fail: " + debug);
            return false;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            while (true) {
                String str = bufferedReader.readLine();
                if (str == null) {
                    bufferedReader.close();
                    return true;
                }
                StringBuilder stringBuilder = new StringBuilder();
                Log.d("RootDetector","cmd out: " + stringBuilder.append(str).toString());
            }
        } finally {
            bufferedReader.close();
        }
    }
}
