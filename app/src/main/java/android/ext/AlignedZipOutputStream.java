package android.ext;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AlignedZipOutputStream extends ZipOutputStream {
    private static final int ALIGN_BASE = 16;
    private CountedOutputStream cos = CountedOutputStream.lastInstance;
    private boolean entryOpen = false;

    public AlignedZipOutputStream(OutputStream os) {
        super(new CountedOutputStream(os));
    }

    public void putNextEntry(ZipEntry ze) throws IOException {
        if (this.entryOpen) {
            closeEntry();
        }
        if (ze.getMethod() == 0) {
            flush();
            ze.setExtra(new byte[((16 - (((this.cos.length + 30) + ze.getName().getBytes("UTF-8").length) % 16)) % 16)]);
        }
        super.putNextEntry(ze);
        this.entryOpen = true;
    }

    public void closeEntry() throws IOException {
        this.entryOpen = false;
        super.closeEntry();
    }

    static class CountedOutputStream extends OutputStream{

        static CountedOutputStream lastInstance;

        int length = 0;

        private OutputStream os;

        public CountedOutputStream(OutputStream paramOutputStream) {
            this.os = paramOutputStream;
            lastInstance = this;
        }

        public void close() throws IOException {
            this.os.close();
        }

        public boolean equals(Object paramObject) {
            return this.os.equals(paramObject);
        }

        public void flush() throws IOException {
            this.os.flush();
        }

        public int hashCode() {
            return this.os.hashCode();
        }

        public String toString() {
            return this.os.toString();
        }

        public void write(int paramInt) throws IOException {
            this.os.write(paramInt);
            this.length++;
        }

        public void write(byte[] paramArrayOfbyte) throws IOException {
            this.os.write(paramArrayOfbyte);
            this.length += paramArrayOfbyte.length;
        }

        public void write(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException {
            this.os.write(paramArrayOfbyte, paramInt1, paramInt2);
            this.length += paramInt2;
        }
    }
}
