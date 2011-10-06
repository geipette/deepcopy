package no.guttab.deepcopy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


class ReusableBufferPoolOutputStream extends OutputStream {
   private int count;
   private boolean writeLocked = false;
   private ByteBuffer buffer;
   private ReusableBufferPool bufferPool;

   public ReusableBufferPoolOutputStream(ReusableBufferPool bufferPool)
         throws InterruptedException {
      this.bufferPool = bufferPool;
      this.buffer = bufferPool.acquireBuffer();
   }

   /**
    * Writes the specified byte to this byte array output stream.
    *
    * @param b the byte to be written.
    */
   public synchronized void write(int b) {
      if (writeLocked) {
         throw new IllegalStateException("This outputstream has been locked from writing. getInputStream() or close() has been called");
      }
      int newcount = count + 1;
      if (newcount > buffer.buffer().length) {
         buffer.enlarge();
      }
      buffer.buffer()[count] = (byte) b;
      count = newcount;
   }

   /**
    * Writes <code>len</code> bytes from the specified byte array
    * starting at offset <code>off</code> to this byte array output stream.
    *
    * @param b   the data.
    * @param off the start offset in the data.
    * @param len the number of bytes to write.
    */
   public synchronized void write(byte b[], int off, int len) {
      if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
         throw new IndexOutOfBoundsException();
      } else if (writeLocked) {
         throw new IllegalStateException("This outputstream has been locked from writing. getInputStream() or close() has been called");
      } else if (len == 0) {
         return;
      }
      int newcount = count + len;
      if (newcount > buffer.buffer().length) {
         buffer.enlarge();
      }
      System.arraycopy(b, off, buffer.buffer(), count, len);
      count = newcount;
   }

   public synchronized InputStream getInputStream() {
      writeLocked = true;
      return new ByteArrayInputStream(buffer.buffer(), 0, count);
   }

   @Override
   public synchronized void close() throws IOException {
      writeLocked = true;
      bufferPool.returnBuffer(buffer);
      buffer = null;
   }

}
