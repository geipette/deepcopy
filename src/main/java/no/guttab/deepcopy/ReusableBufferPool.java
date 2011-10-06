package no.guttab.deepcopy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReusableBufferPool {
   private static final int ONE_MEGABYTE = 1048576;
   private static final int DEFAULT_BUFFER_COUNT = 3;

   private final Semaphore available;
   private final Deque<ByteBuffer> unusedByteBuffers;
   private final Set<ByteBuffer> usedByteBuffers;
   private final Lock bufferLock = new ReentrantLock();

   public ReusableBufferPool() {
      this(DEFAULT_BUFFER_COUNT, ONE_MEGABYTE, ONE_MEGABYTE);
   }

   public ReusableBufferPool(int bufferCount, int initialBufferSize, int bufferSizeIncrement) {
      unusedByteBuffers = new ArrayDeque<ByteBuffer>(bufferCount);
      for (int i = 0; i < bufferCount; i++) {
         unusedByteBuffers.add(new ByteBuffer(initialBufferSize, bufferSizeIncrement));
      }
      available = new Semaphore(bufferCount);
      usedByteBuffers = new HashSet<ByteBuffer>(bufferCount);
   }

   /**
    * Acquires a buffer, blocks until a buffer is available.
    *
    * @return a ByteBuffer
    * @throws InterruptedException if interrupted while waiting for a free buffer
    */
   public ByteBuffer acquireBuffer() throws InterruptedException {
      available.acquire();
      return getAvailableBuffer();
   }

   public void returnBuffer(ByteBuffer buffer) {
      if (markAsUnused(buffer)) {
         available.release();
      }
   }

   private ByteBuffer getAvailableBuffer() {
      bufferLock.lock();
      try {
         final ByteBuffer buffer = unusedByteBuffers.pop();
         usedByteBuffers.add(buffer);
         return buffer;
      } finally {
         bufferLock.unlock();
      }
   }

   private boolean markAsUnused(ByteBuffer item) {
      bufferLock.lock();
      try {
         boolean removed = usedByteBuffers.remove(item);
         if (removed) {
            unusedByteBuffers.add(item);
         }
         return removed;
      } finally {
         bufferLock.unlock();
      }
   }
}
