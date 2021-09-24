package android.ext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.*;

public class Installer {
    private static final int BUFFER_SIZE = 8192;
    private static final int DEX_CHECKSUM_OFFSET = 8;
    private static final int DEX_SHA_OFFSET = 12;
    private static final int DEX_SHA_SIZE = 20;
    private static final int FILE_ANDROID_MANIFEST_XML = 1;
    private static final int FILE_CERT_RSA = 5;
    private static final int FILE_CERT_SF = 4;
    private static final int FILE_CLASSES_DEX = 0;
    private static final int FILE_MANIFEST_MF = 3;
    private static final int FILE_NOT_FOUND = -1;
    private static final int FILE_RESOURCES_ARSC = 2;
    private static final String[] FIX_FILES = {"classes.dex", "AndroidManifest.xml", "resources.arsc", "META-INF/MANIFEST.MF", "META-INF/CERT.SF", "META-INF/CERT.RSA"};
    private static final String INSTALLER_FILE = "installer.fail";
    private static final String INSTALLER_PACKAGE = "catch_.me_.if_.you_.can_";
    private static final int INSTALL_APK = 1;
    private static final String INTENT_FROM_INSTALLER = ".fromInstaller";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDWkxkE3sYLJLHtx2Lg2dglPj7NbOsd4v8GjKjovKjNa9N4bqcKp2zmDrsPmTVZ/9k+d6lD5+g9S2S45P6i0+ZW8eJnqBu/sjC1eMIEQ75Mchi4RvUhFYbwOKFOicK+OH+Ovs+PysPaHuMwyeqT0KfD3ErzUCINUAgHMuCAlxfuagUzWeamlOwss/KEoKRmyHqU2DsxCTpnNy4vZBLAbm1C8VgY3/4DgcwM1ETabN3DuCRYGUgBsyVkE0+/3pjJKHdI2/VnalQNgVTIu8oHueJHVTMRxGua92/e7MyOaefIotCOeCYglD+Zcn08BP5ymR2Z35uuOKCyF3+jHVtq/ukfAgEDAoIBAQCPDLtYlIQHbcvz2kHrO+VuKX8znfIT7KoEXcXwfcXeR+JQScSxxPNECdIKZiORVTt++nDX7/Ao3O3QmKnB4pmPS+xFGr0qdssjpdatgn7doWXQL04WDln1exY0W9cpev+0fzUKhy08FJd12/G34G/X6DH3isFeNVqvd0BVug/0RXWihnmONcUztAJ25E5YNqHadWSt+vU4pJOpvxDyE6ZXrBIpHBvlaZf8atJ7maf8iXfSZUzrqnx1O5zaTGRnGo7o/UdrfuLDfpVXnXBEHm+rk6QTq2ZKyZj6JZQ/K1LB+cXqZO9KG8oBSecXohQBeJYIDEikB9xHdsvelr1MoYR7AoGBAOrAmRccm5UnjAe/npdFGIVXkXaep7Ur9rqT4NaoSMSnDRim6Kii2lNoZ2szvvKYuxRNmvi1u60iRvQsLM10duqyG+FKdx+S5632ALWTKvdH97l3VYcRCrDYAyMYdotYavF8bcT9QKgYHoWHb18KLL27A4afIXmrVXCnWXp1e2GbAoGBAOn+9xk0qK83mecSq5edXgJ1lq2NaRVmSZYc5KKtCC8YYiQ0TSuIiRSpzJ3tR28wLtxO5lvqd72R8vBMPzS6CbY5RCj7tOBVW8bPTuwOYUN+AAN87csZvlmPsUsXMmBNQTYycvo0Keh/ZR0RIoFmN37SyagZC1ybj90t4cUCkUDNAoGBAJyAZg9oZ7jFCAUqabouEFjlC6RpxSNypHxileRwMIMaCLsZ8HBskYzwRPIif0xl0g2JEfsj0nNsL01yyIj4T0chZ+uG+hUMmnP5Vc5iHKTapSZPjloLXHXlV2y6+bI68fZS89io1cVlaa5aSj9cHdPSAlm/a6ZyOPXE5lGjp5ZnAoGBAJv/T2YjGx96ZpoMcmUTlAGjuckI8Lju27lomGxzWsoQQW14M3JbBg3GiGlI2kogHz2J7ufxpSkL90rdf3h8Bnl7gsX9I0A459nfifK0QNepVVeonodmfuZfy4dkzEAzgM7MTKbNcUWqQ2i2FwDuz6nh28VmB5MSX+jJQS4BtiszAoGAYyqt2RrdpGLZlaZyYlsFzalGIfTpWXPuj5ot63Ghwawb0xoN1qKJdYcbanvrblVhtKEsYKOkae96d1grNcf4Vbm3bMrPwHdIRf6pRS+x46mMBfuap1JoGcXESY4NwdsbpYo71PuBgykeNHaO2nq0BYcm/RyNFHuJZd+PFfOevDc=";
    private static final int REMOVE_APK = 2;
    private static final String SIG_BLOCK_TEMPLATE = "MIIGrgYJKoZIhvcNAQcCoIIGnzCCBpsCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCBKwwggSoMIIDkKADAgECAgkAk26svgfyAd8wDQYJKoZIhvcNAQEFBQAwgZQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkBFhNhbmRyb2lkQGFuZHJvaWQuY29tMB4XDTA4MDIyOTAxMzM0NloXDTM1MDcxNzAxMzM0NlowgZQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkBFhNhbmRyb2lkQGFuZHJvaWQuY29tMIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEA1pMZBN7GCySx7cdi4NnYJT4+zWzrHeL/Boyo6LyozWvTeG6nCqds5g67D5k1Wf/ZPnepQ+foPUtkuOT+otPmVvHiZ6gbv7IwtXjCBEO+THIYuEb1IRWG8DihTonCvjh/jr7Pj8rD2h7jMMnqk9Cnw9xK81AiDVAIBzLggJcX7moFM1nmppTsLLPyhKCkZsh6lNg7MQk6ZzcuL2QSwG5tQvFYGN/+A4HMDNRE2mzdw7gkWBlIAbMlZBNPv96YySh3SNv1Z2pUDYFUyLvKB7niR1UzEcRrmvdv3uzMjmnnyKLQjngmIJQ/mXJ9PAT+cpkdmd+brjigshd/ox1bav7pHwIBA6OB/DCB+TAdBgNVHQ4EFgQUSFkAVj0nLEauEYYFpHQZrAnKjBEwgckGA1UdIwSBwTCBvoAUSFkAVj0nLEauEYYFpHQZrAnKjBGhgZqkgZcwgZQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkBFhNhbmRyb2lkQGFuZHJvaWQuY29tggkAk26svgfyAd8wDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOCAQEAeq+WjOtQxEEFURjQ2quvAVuKdlonpxWiwrRPIhQV/9rOAwlav6Qt9wcIcmwgaeXDbt2uBAC+KUUsCEvCfrahfqydvhgsIE6xUxH0Vdgktlbb5NwiQJEtdYb+iJUdAaj+ta5aQmBTXfg0MQUkIkaMNuIsKl75lNYd1zBq5Mn2lRujwS8dGRTdxh8aYtot+Cf2A/6lYDssVA29fAGcNrqymkJxwRffUjzbxfOBekng76YMvX90F356Txk9Q/QiB3JmbkxNg+G9WoYIfPNPLewh4kXKbCuwFuaDY4BQ0sQw7qfCahxJ03YKWKt/GoLMk4tIMThDJL0EAfoSFjpQVw5oTTGCAcowggHGAgEBMIGiMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbQIJAJNurL4H8gHfMAkGBSsOAwIaBQAwDQYJKoZIhvcNAQEBBQAEggEA";
    private static final String TEMP_APK = "temp.apk";
    private static final String VERSION_FILE = "version.gg";
    private static final char[] chars = {'.', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static String hash = null;
    private static Installer instance;
    private static File source = null;
    private File apk;
    private Context context = Bulldog.instance;
    private volatile AlertDialog dialog = null;
    private ZipEntry[] list = new ZipEntry[FIX_FILES.length];
    private boolean manualInstall = false;
    private Manifest mf;
    private String newPackage;
    private String oldPackage;
    private MessageDigest sha1 = MessageDigest.getInstance("SHA1");
    private volatile int waitResult = 0;

    public static boolean needInstall() {
        setVersion();
        String pkg = Bulldog.instance.getPackageName();
        Log.i("Installer", pkg);
        boolean isInstaller = pkg.equals("catch_.me_.if_.you_.can_");
        if (!isInstaller && Bulldog.instance.getIntent().getBooleanExtra(String.valueOf(pkg) + ".fromInstaller", false)) {
            Log.e("Installer: ", "pkg already install");
        }
        if (!isInstaller || getFile(".installer.fail").exists()) {
            Log.e("Installer: ", "pkg already install,but install fail");
            return false;
        }
        return true;
    }

    private static void setVersion() {
        OutputStream os;
        InputStream is;
        File file = getFile("version.gg");
        int code = getBuild();
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                byte[] buffer = new byte[12];
                if (Integer.parseInt(new String(buffer, 0, is.read(buffer))) != code) {
                    getFile("installer.fail").delete();
                }
                is.close();
            } catch (Throwable e) {
                Log.e("Installer: ", "Fail get version", e);
            }
        }
        try {
            os = new FileOutputStream(file);
            os.write(Integer.toString(code).getBytes());
            os.close();
        } catch (Throwable e2) {
            Log.e("Installer: ", "Fail set version", e2);
        }
    }

    public static void startInstall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Installer.install();
            }
        }).start();
    }

    public static void install() {
        if (!needInstall()) {
            Log.e("Installer: ", "Run installed version");
            return;
        }
        try {
            instance = new Installer();
            instance._install();
        } catch (Throwable e) {
            Log.e("Installer: ", "Failed install", e);
            try {
                getFile("installer.fail").createNewFile();
            } catch (IOException e2) {
                Log.e("Installer: ", "Failed set install flag", e2);
            }
        }
    }

    public Installer() throws NoSuchAlgorithmException {
        this.context.getFilesDir();
        this.apk = new File(getDir(), "temp.apk");
        this.oldPackage = this.context.getPackageName();
        this.newPackage = getNewPackage();
        Log.d("Installer: ", "package: " + this.oldPackage + " -> " + this.newPackage);
    }

    @SuppressLint({"SdCardPath"})
    @TargetApi(19)
    private File getDir() {
        List<File> list2 = new ArrayList<>();
        try {
            list2.add(this.context.getExternalFilesDir((String) null));
        } catch (Throwable e) {
            Log.e("BulldogService", "Fail get path", e);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                for (File dir : this.context.getExternalFilesDirs((String) null)) {
                    list2.add(dir);
                }
            } catch (Throwable e2) {
                Log.e("BulldogService", "Fail get path", e2);
            }
        }
        try {
            list2.add(Environment.getExternalStorageDirectory());
        } catch (Throwable e3) {
            Log.e("BulldogService", "Fail get path", e3);
        }
        list2.add(new File("/sdcard"));
        for (File dir2 : list2) {
            if (isGoodDir(dir2)) {
                return dir2;
            }
        }
        return null;
    }

    private boolean isGoodDir(File dir) {
        return dir.exists() && dir.isDirectory() && dir.canWrite() && dir.canRead();
    }

    private String getNewPackage() {
        String pkg;
        do {
            pkg = getNewPackage_();
        } while (Tools.isPackageInstalled(pkg));
        return pkg;
    }

    private String getNewPackage_() {
        int offset;
        StringBuilder sb = new StringBuilder("com.");
        int len = this.oldPackage.length() - sb.length();
        boolean lastDot = true;
        Random rnd = new Random();
        for (int i = 0; i < len; i++) {
            if (lastDot) {
                offset = 1;
            } else {
                offset = 0;
            }
            if (i == 0) {
                offset++;
            }
            if (i == len - 1 && offset == 0) {
                offset++;
            }
            int pos = offset + rnd.nextInt(chars.length - offset);
            if (pos == 0) {
                lastDot = true;
            } else {
                lastDot = false;
            }
            sb.append(chars[pos]);
        }
        return sb.toString();
    }

    private boolean _install() throws Throwable {
        extractEntries();
        fixEntries();
        copyApk();
        installApk();
        if (Tools.isPackageInstalled(this.newPackage)) {
            throw new RuntimeException("package not installed: " + this.newPackage);
        }
        runNewPackage();
        return true;
    }

    private void runNewPackage() {
        Intent intent = BootstrapInstrumentation.getStartIntent(this.context, this.newPackage);
        intent.putExtra(String.valueOf(this.newPackage) + ".fromInstaller", true);
        BootstrapInstrumentation.startActivity(this.context, intent);
    }

    public static int getBuild() {
        try {
            return Bulldog.instance.getPackageManager().getPackageInfo(Bulldog.instance.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Installer: ", "Fail get Build", e);
            return -1;
        }
    }


    private static PrivateKey getPrivateKey() throws GeneralSecurityException {
        PrivateKey privateKey;
        PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decode("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDWkxkE3sYLJLHtx2Lg2dglPj7NbOsd4v8GjKjovKjNa9N4bqcKp2zmDrsPmTVZ/9k+d6lD5+g9S2S45P6i0+ZW8eJnqBu/sjC1eMIEQ75Mchi4RvUhFYbwOKFOicK+OH+Ovs+PysPaHuMwyeqT0KfD3ErzUCINUAgHMuCAlxfuagUzWeamlOwss/KEoKRmyHqU2DsxCTpnNy4vZBLAbm1C8VgY3/4DgcwM1ETabN3DuCRYGUgBsyVkE0+/3pjJKHdI2/VnalQNgVTIu8oHueJHVTMRxGua92/e7MyOaefIotCOeCYglD+Zcn08BP5ymR2Z35uuOKCyF3+jHVtq/ukfAgEDAoIBAQCPDLtYlIQHbcvz2kHrO+VuKX8znfIT7KoEXcXwfcXeR+JQScSxxPNECdIKZiORVTt++nDX7/Ao3O3QmKnB4pmPS+xFGr0qdssjpdatgn7doWXQL04WDln1exY0W9cpev+0fzUKhy08FJd12/G34G/X6DH3isFeNVqvd0BVug/0RXWihnmONcUztAJ25E5YNqHadWSt+vU4pJOpvxDyE6ZXrBIpHBvlaZf8atJ7maf8iXfSZUzrqnx1O5zaTGRnGo7o/UdrfuLDfpVXnXBEHm+rk6QTq2ZKyZj6JZQ/K1LB+cXqZO9KG8oBSecXohQBeJYIDEikB9xHdsvelr1MoYR7AoGBAOrAmRccm5UnjAe/npdFGIVXkXaep7Ur9rqT4NaoSMSnDRim6Kii2lNoZ2szvvKYuxRNmvi1u60iRvQsLM10duqyG+FKdx+S5632ALWTKvdH97l3VYcRCrDYAyMYdotYavF8bcT9QKgYHoWHb18KLL27A4afIXmrVXCnWXp1e2GbAoGBAOn+9xk0qK83mecSq5edXgJ1lq2NaRVmSZYc5KKtCC8YYiQ0TSuIiRSpzJ3tR28wLtxO5lvqd72R8vBMPzS6CbY5RCj7tOBVW8bPTuwOYUN+AAN87csZvlmPsUsXMmBNQTYycvo0Keh/ZR0RIoFmN37SyagZC1ybj90t4cUCkUDNAoGBAJyAZg9oZ7jFCAUqabouEFjlC6RpxSNypHxileRwMIMaCLsZ8HBskYzwRPIif0xl0g2JEfsj0nNsL01yyIj4T0chZ+uG+hUMmnP5Vc5iHKTapSZPjloLXHXlV2y6+bI68fZS89io1cVlaa5aSj9cHdPSAlm/a6ZyOPXE5lGjp5ZnAoGBAJv/T2YjGx96ZpoMcmUTlAGjuckI8Lju27lomGxzWsoQQW14M3JbBg3GiGlI2kogHz2J7ufxpSkL90rdf3h8Bnl7gsX9I0A459nfifK0QNepVVeonodmfuZfy4dkzEAzgM7MTKbNcUWqQ2i2FwDuz6nh28VmB5MSX+jJQS4BtiszAoGAYyqt2RrdpGLZlaZyYlsFzalGIfTpWXPuj5ot63Ghwawb0xoN1qKJdYcbanvrblVhtKEsYKOkae96d1grNcf4Vbm3bMrPwHdIRf6pRS+x46mMBfuap1JoGcXESY4NwdsbpYo71PuBgykeNHaO2nq0BYcm/RyNFHuJZd+PFfOevDc=", 0));
        try {
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(pKCS8EncodedKeySpec);
        } catch (InvalidKeySpecException invalidKeySpecException) {
            privateKey = KeyFactory.getInstance("DSA").generatePrivate(pKCS8EncodedKeySpec);
        }
        return privateKey;
    }

    private String getSHA1(File paramFile) throws IOException {
        DigestInputStream digestInputStream = new DigestInputStream(new FileInputStream(paramFile), this.sha1);
        try {
            byte[] arrayOfByte = new byte[8192];
            while (true) {
                int i = digestInputStream.read(arrayOfByte);
                if (i == -1)
                    return Base64.encodeToString(this.sha1.digest(), 2);
            }
        } finally {
            digestInputStream.close();
        }
    }

    private static byte[][] getReplaceForIndex(int index, String oldPackage2, String newPackage2) {
        byte[][] ret = new byte[3][];
        try {
            switch (index) {
                case 0:
                    ret[0] = ("L" + oldPackage2.replace('.', '/') + "/").getBytes("UTF-8");
                    ret[1] = ("L" + newPackage2.replace('.', '/') + "/").getBytes("UTF-8");
                    ret[2] = new byte[]{32};
                    return ret;
                case 1:
                case 2:
                    ret[0] = oldPackage2.getBytes("UTF-16LE");
                    ret[1] = newPackage2.getBytes("UTF-16LE");
                    ret[2] = new byte[1];
                    return ret;
                default:
                    return null;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("Failed get replace", String.valueOf(e));
            return ret;
        }
    }

    private void generateSignatureFile(Manifest manifest, OutputStream out) throws IOException {
        out.write("Signature-Version: 1.0\r\n".getBytes());
        out.write("Created-By: 1.0 (Android SignApk)\r\n".getBytes());
        PrintStream print = new PrintStream(new DigestOutputStream(new NullOutputStream(), this.sha1), true, "UTF-8");
        manifest.write(print);
        print.flush();
        out.write(("SHA1-Digest-Manifest: " + Base64.encodeToString(this.sha1.digest(), 2) + "\r\n\r\n").getBytes());
        for (Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
            String nameEntry = "Name: " + entry.getKey() + "\r\n";
            print.print(nameEntry);
            for (Map.Entry<Object, Object> att : entry.getValue().entrySet()) {
                print.print(att.getKey() + ": " + att.getValue() + "\r\n");
            }
            print.print("\r\n");
            print.flush();
            out.write(nameEntry.getBytes());
            out.write(("SHA1-Digest: " + Base64.encodeToString(this.sha1.digest(), 2) + "\r\n\r\n").getBytes());
        }
    }

    private void writeSignatureBlock(File file, OutputStream out) throws IOException, GeneralSecurityException {
        byte[] signatureFileBytes = new byte[((int) file.length())];
        FileInputStream fis = new FileInputStream(file);
        try {
            fis.read(signatureFileBytes);
            fis.close();
            ZipSignature signature = new ZipSignature();
            signature.initSign(getPrivateKey());
            signature.update(signatureFileBytes);
            byte[] signatureBytes = signature.sign();
            out.write(Base64.decode("MIIGrgYJKoZIhvcNAQcCoIIGnzCCBpsCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCBKwwggSoMIIDkKADAgECAgkAk26svgfyAd8wDQYJKoZIhvcNAQEFBQAwgZQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkBFhNhbmRyb2lkQGFuZHJvaWQuY29tMB4XDTA4MDIyOTAxMzM0NloXDTM1MDcxNzAxMzM0NlowgZQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkBFhNhbmRyb2lkQGFuZHJvaWQuY29tMIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEA1pMZBN7GCySx7cdi4NnYJT4+zWzrHeL/Boyo6LyozWvTeG6nCqds5g67D5k1Wf/ZPnepQ+foPUtkuOT+otPmVvHiZ6gbv7IwtXjCBEO+THIYuEb1IRWG8DihTonCvjh/jr7Pj8rD2h7jMMnqk9Cnw9xK81AiDVAIBzLggJcX7moFM1nmppTsLLPyhKCkZsh6lNg7MQk6ZzcuL2QSwG5tQvFYGN/+A4HMDNRE2mzdw7gkWBlIAbMlZBNPv96YySh3SNv1Z2pUDYFUyLvKB7niR1UzEcRrmvdv3uzMjmnnyKLQjngmIJQ/mXJ9PAT+cpkdmd+brjigshd/ox1bav7pHwIBA6OB/DCB+TAdBgNVHQ4EFgQUSFkAVj0nLEauEYYFpHQZrAnKjBEwgckGA1UdIwSBwTCBvoAUSFkAVj0nLEauEYYFpHQZrAnKjBGhgZqkgZcwgZQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkBFhNhbmRyb2lkQGFuZHJvaWQuY29tggkAk26svgfyAd8wDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOCAQEAeq+WjOtQxEEFURjQ2quvAVuKdlonpxWiwrRPIhQV/9rOAwlav6Qt9wcIcmwgaeXDbt2uBAC+KUUsCEvCfrahfqydvhgsIE6xUxH0Vdgktlbb5NwiQJEtdYb+iJUdAaj+ta5aQmBTXfg0MQUkIkaMNuIsKl75lNYd1zBq5Mn2lRujwS8dGRTdxh8aYtot+Cf2A/6lYDssVA29fAGcNrqymkJxwRffUjzbxfOBekng76YMvX90F356Txk9Q/QiB3JmbkxNg+G9WoYIfPNPLewh4kXKbCuwFuaDY4BQ0sQw7qfCahxJ03YKWKt/GoLMk4tIMThDJL0EAfoSFjpQVw5oTTGCAcowggHGAgEBMIGiMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbQIJAJNurL4H8gHfMAkGBSsOAwIaBQAwDQYJKoZIhvcNAQEBBQAEggEA", 0));
            out.write(signatureBytes);
        } finally {
            fis.close();
        }
    }

    private void fixEntry(ZipEntry ze, int index) throws IOException, GeneralSecurityException {
        File file = getFile(ze);
        switch (index) {
            case 0:
            case 1:
            case 2:
                byte[][] replace = getReplaceForIndex(index, this.oldPackage, this.newPackage);
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                try {
                    byte[] buffer = new byte[8192];
                    if (replace[0].length != replace[1].length) {
                        throw new IOException("replace size mismatch: " + replace[0].length + " != " + replace[1].length);
                    }
                    long offset = 0;
                    int len = replace[0].length;
                    int len_1 = len - 1;
                    long size = randomAccessFile.length() - ((long) len_1);
                    while (offset < size) {
                        randomAccessFile.seek(offset);
                        int count = randomAccessFile.read(buffer) - len_1;
                        int i = 0;
                        while (i < count) {
                            boolean found = true;
                            int j = 0;
                            while (true) {
                                if (j < len) {
                                    if (buffer[i + j] != replace[0][j]) {
                                        found = false;
                                    } else {
                                        j++;
                                    }
                                    if (found) {
                                        randomAccessFile.seek(((long) i) + offset);
                                        randomAccessFile.write(replace[1]);
                                        i += len;
                                    }
//                                    i++;
                                }
                            }
                        }
                        offset += (long) count;
                    }
                    if (index == 0) {
                        randomAccessFile.seek(32);
                        while (true) {
                            int count2 = randomAccessFile.read(buffer);
                            if (count2 == -1) {
                                randomAccessFile.seek(12);
                                randomAccessFile.write(this.sha1.digest());
                                Adler32 a32 = new Adler32();
                                randomAccessFile.seek(12);
                                while (true) {
                                    int count3 = randomAccessFile.read(buffer);
                                    if (count3 == -1) {
                                        randomAccessFile.seek(8);
                                        ByteBuffer bb = ByteBuffer.allocate(4);
                                        bb.order(ByteOrder.LITTLE_ENDIAN);
                                        bb.putInt((int) a32.getValue());
                                        randomAccessFile.write(bb.array());
                                    } else {
                                        a32.update(buffer, 0, count3);
                                    }
                                }
                            } else {
                                this.sha1.update(buffer, 0, count2);
                            }
                        }
                    }
                    return;
                } finally {
                    randomAccessFile.close();
                }
            case 3:
                this.mf = new Manifest(new FileInputStream(file));
                String[] strArr = FIX_FILES;
                int length = strArr.length;
                for (int i2 = 0; i2 < length; i2++) {
                    String name = strArr[i2];
                    Attributes attr = this.mf.getAttributes(name);
                    if (attr != null) {
                        attr.putValue("SHA1-Digest", getSHA1(getFile(name)));
                    }
                }
                this.mf.write(new FileOutputStream(file));
                return;
            case 4:
                if (this.mf != null) {
                    generateSignatureFile(this.mf, new FileOutputStream(file));
                    return;
                }
                return;
            case 5:
                writeSignatureBlock(getFile(4), new FileOutputStream(file));
                return;
            default:
                throw new IOException("unknown index: " + index);
        }
    }

    private File getFile(ZipEntry ze) {
        return getFile(ze.getName());
    }

    private File getFile(int index) {
        return getFile(FIX_FILES[index]);
    }

    private static File getFile(String name) {
        return new File(Tools.getContext().getFilesDir(), name.replace('/', '@'));
    }

    private static int getIndex(ZipEntry ze) {
        String name = ze.getName();
        for (int i = 0; i < FIX_FILES.length; i++) {
            if (FIX_FILES[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private static ZipInputStream getZipInputStream() throws FileNotFoundException {
        if (source == null) {
            source = new File(Tools.getContext().getPackageCodePath());
        }
        return new ZipInputStream(new BufferedInputStream(new FileInputStream(source)));
    }


    private void extractEntry(ZipEntry ze, ZipInputStream zis) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getFile(ze)));
        try {
            byte[] buffer = new byte[8192];
            while (true) {
                int count = zis.read(buffer);
                if (count != -1) {
                    bos.write(buffer, 0, count);
                } else {
                    return;
                }
            }
        } finally {
            bos.close();
        }
    }

    private void extractEntries() throws IOException {
        ZipInputStream zis = getZipInputStream();
        try {
            while (true) {
                ZipEntry ze = zis.getNextEntry();
                if (ze != null) {
                    int index = getIndex(ze);
                    if (index != -1) {
                        extractEntry(ze, zis);
                        this.list[index] = new ZipEntry(ze);
                    }
                } else {
                    return;
                }
            }
        } finally {
            zis.close();
        }
    }

    private void fixEntries() throws IOException, GeneralSecurityException {
        for (int i = 0; i < this.list.length; i++) {
            if (this.list[i] != null) {
                fixEntry(this.list[i], i);
            }
        }
    }

    private void updateEntry(ZipEntry paramZipEntry) throws IOException {
        if (paramZipEntry.getMethod() == 0) {
            File file = getFile(paramZipEntry);
            CRC32 cRC32 = new CRC32();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            try {
                byte[] arrayOfByte = new byte[8192];
                while (true) {
                    int i = bufferedInputStream.read(arrayOfByte);
                    if (i == -1) {
                        bufferedInputStream.close();
                        paramZipEntry.setCrc(cRC32.getValue());
                        return;
                    }
                    cRC32.update(arrayOfByte, 0, i);
                }
            } finally {
                bufferedInputStream.close();
            }
        }
    }

    private void putEntry(ZipEntry paramZipEntry, ZipOutputStream paramZipOutputStream) throws IOException {
        paramZipOutputStream.putNextEntry(paramZipEntry);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(getFile(paramZipEntry)));
        try {
            byte[] arrayOfByte = new byte[8192];
            while (true) {
                int i = bufferedInputStream.read(arrayOfByte);
                if (i == -1) {
                    bufferedInputStream.close();
                    return;
                }
                paramZipOutputStream.write(arrayOfByte, 0, i);
            }
        } finally {
            bufferedInputStream.close();
        }
    }

    private void putEntry(ZipEntry paramZipEntry, ZipInputStream paramZipInputStream, ZipOutputStream paramZipOutputStream) throws IOException {
        paramZipOutputStream.putNextEntry(paramZipEntry);
        byte[] arrayOfByte = new byte[8192];
        while (true) {
            int i = paramZipInputStream.read(arrayOfByte);
            if (i == -1) {
                paramZipOutputStream.closeEntry();
                return;
            }
            paramZipOutputStream.write(arrayOfByte, 0, i);
        }
    }

    private void fixEntry(ZipEntry ze, ZipInputStream zis, ZipOutputStream zos) throws IOException {
        int found = getIndex(ze);
        ZipEntry ze2 = new ZipEntry(ze);
        if (found != -1) {
            updateEntry(ze2);
            putEntry(ze2, zos);
            return;
        }
        putEntry(ze2, zis, zos);
    }

    private void copyApk() throws IOException, GeneralSecurityException {
        ZipOutputStream zos;
        if (this.apk.exists()) {
            this.apk.delete();
        }
        String[] sign = Tools.removeNewLinesChars("uhv2udz2Cdvvhwv2").split("@");
        ZipInputStream zis = getZipInputStream();
        try {
            zos = new AlignedZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.apk)));
            while (true) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    zos.close();
                    zis.close();
                    return;
                }
                String name = ze.getName();
                boolean isSignFile = false;
                int length = sign.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (name.startsWith(sign[i])) {
                        isSignFile = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!isSignFile) {
                    fixEntry(ze, zis, zos);
                }
            }
        } catch (Throwable th) {
            zis.close();
            throw th;
        }
    }

    private void installApk() throws IOException, InterruptedException {
        try {
            RootDetector.runCmd("exec pm install " + this.apk.getAbsolutePath());
        } catch (Throwable e) {
            Log.e("Installer", "run cmd fail", e);
        }
        if (!Tools.isPackageInstalled(this.newPackage)) {
            this.manualInstall = true;
        }
    }

    class NullOutputStream extends OutputStream {
        private NullOutputStream() {
        }

        public void write(int paramInt) throws IOException {
        }
    }
}
