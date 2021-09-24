package android.ext;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class ZipSignature {
    byte[] afterAlgorithmIdBytes;
    byte[] algorithmIdBytes;
    byte[] beforeAlgorithmIdBytes = {48, 33};
    Cipher cipher;
    MessageDigest md;

    public ZipSignature() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] bArr = new byte[11];
        bArr[0] = 48;
        bArr[1] = 9;
        bArr[2] = 6;
        bArr[3] = 5;
        bArr[4] = 43;
        bArr[5] = 14;
        bArr[6] = 3;
        bArr[7] = 2;
        bArr[8] = 26;
        bArr[9] = 5;
        this.algorithmIdBytes = bArr;
        this.afterAlgorithmIdBytes = new byte[]{4, 20};
        this.md = MessageDigest.getInstance("SHA1");
        this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }

    public void initSign(PrivateKey privateKey) throws InvalidKeyException {
        this.cipher.init(1, privateKey);
    }

    public void update(byte[] data) {
        this.md.update(data);
    }

    public void update(byte[] data, int offset, int count) {
        this.md.update(data, offset, count);
    }

    public byte[] sign() throws BadPaddingException, IllegalBlockSizeException {
        this.cipher.update(this.beforeAlgorithmIdBytes);
        this.cipher.update(this.algorithmIdBytes);
        this.cipher.update(this.afterAlgorithmIdBytes);
        this.cipher.update(this.md.digest());
        return this.cipher.doFinal();
    }
}
