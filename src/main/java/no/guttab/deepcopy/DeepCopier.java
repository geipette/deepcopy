package no.guttab.deepcopy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DeepCopier {
   private ReusableBufferPool bufferPool;

   public DeepCopier() {
      this(new ReusableBufferPool());
   }

   public DeepCopier(ReusableBufferPool bufferPool) {
      this.bufferPool = bufferPool;
   }

   public <T extends Serializable> T copyOf(T src) throws InterruptedException, IOException {
      ObjectOutputStream outStream = null;
      try {
         final ReusableBufferPoolOutputStream byteOut = new ReusableBufferPoolOutputStream(bufferPool);
         outStream = new ObjectOutputStream(byteOut);
         outStream.writeObject(src);
         outStream.flush();

         return readObject(byteOut);
      } finally {
         if (outStream != null) {
            try {
               outStream.close();
            } catch (IOException ignored) {}
         }
      }
   }

   @SuppressWarnings({"unchecked"})
   private <T extends Serializable> T readObject(ReusableBufferPoolOutputStream byteOut) throws IOException {
      ObjectInputStream inStream = null;
      try {
         final InputStream byteIn = byteOut.getInputStream();
         inStream = new ObjectInputStream(byteIn);
         try {
            return (T) inStream.readObject();
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // This should never happen.
         }
      } finally {
         if (inStream != null) {
            try {
               inStream.close();
            } catch (IOException ignored) {}
         }
      }
   }

}
