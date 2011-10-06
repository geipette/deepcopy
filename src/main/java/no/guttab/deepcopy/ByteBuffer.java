package no.guttab.deepcopy;

import java.util.Arrays;

class ByteBuffer {
   private byte[] buf;
   private int resizeIncrement;

   public ByteBuffer(int initialSize, int resizeIncrement) {
      this.resizeIncrement = resizeIncrement;
      this.buf = new byte[initialSize];
   }

   public byte[] buffer() {
      return buf;
   }

   public void enlarge() {
      buf = Arrays.copyOf(buf, buf.length + resizeIncrement);
   }
}
