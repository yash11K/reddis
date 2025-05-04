package build.your.own.database;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The {@code DbMap} class acts as an in-memory key-value store similar to a simplified Redis.
 *
 * <p>This class stores key-value pairs in memory, where each value is wrapped in an {@link Data}
 * object that can optionally have an expiration timestamp.
 *
 * <p>It follows the Singleton pattern to ensure a single shared instance across the application.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Supports setting and retrieving values with optional expiry.</li>
 *   <li>Automatically removes expired keys during retrieval.</li>
 *   <li>Can be persisted using {@code SerializeProtocol} to a file.</li>
 * </ul>
 */
public class DbMap {
  private final ConcurrentMap<String, Data> inMemoryMap;
  /* ReentrantReadWriteLock Notes
    Allows concurrent readers to access shared data
    Allows only one writer at a time
    Writer blocks all readers and writers
    Readers block writers but don't block other readers

    Through these points we can intuitively understand the chronology of placing read and write locks
    This gives better performance than synchronized because it allows multiple threads to read the data concurrently and not locks the resource completely and unfairly
    This is useful when we have a lot of reads and few writes
  */
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  /**
   * Private constructor for singleton pattern.
   *
   */
  public DbMap() {
    this.inMemoryMap = new ConcurrentHashMap<>();
  }

  /**
   * Retrieves the value associated with the given key.
   * Checks for expiry and lazily removes K,V
   *
   * <p>If the key has expired, it is removed from the map and {@code null} is returned.
   * If the key doesn't exist, {@code null} is returned.
   *
   * @param key the key to look up
   * @return the value if present and not expired, otherwise {@code null}
   */
  public String getValue(String key){
    readLock.lock();

    try{
      Data data = inMemoryMap.get(key);
      if (data == null) return null;

      if(data.expiry !=null && data.expiry.isBefore(LocalDateTime.now())){
        //Upgrade to write lock to remove expired key
        //This upgrade is okay, vice versa is not true (from write to read)
        readLock.unlock();
        writeLock.lock();
        try{
          Data doubleCheck = inMemoryMap.get(key);
          if(doubleCheck != null && doubleCheck.expiry != null && doubleCheck.expiry.isBefore(LocalDateTime.now())){
            //Remove expired key
            inMemoryMap.remove(key);
          }
        }finally {
          //downgrade again
          writeLock.unlock();
          readLock.lock();
        }
        return null;
      }

      return data.data;
    }
    finally {
      //release read lock
      readLock.unlock();
    }
  }


  /**
   * Stores a key-value pair into the map with an optional expiration time.
   *
   * @param key the key to store
   */
  public void putValue(String key, String value){
    putValue(key, value, null);
  }

  /**
   * Stores a key-value pair into the map with an optional expiration time.
   *
   * @param key the key to store
   * @param val the value to store
   * @param exp the expiration time (can be {@code null} if no expiry is desired)
   */
  public void putValue(String key, String val, LocalDateTime exp){
    writeLock.lock();
    try{
      inMemoryMap.put(key, new Data(exp, val));
    }finally {
      //release write lock
      writeLock.unlock();
    }

  }


  public Set<Map.Entry<String, Data>> getEntrySet(){
    return this.inMemoryMap.entrySet();
  }

  public ConcurrentMap<String, Data> getInMemoryMap() {
    return inMemoryMap;
  }

  /**
   * TODO: IMPLEMENT SUPPORT FOR MULTIPLY DATA TYPES \n
   * Represents a single entry in the {@code DbMap}, holding the value and optional expiry timestamp.
   *
   * @param expiry {@link LocalDateTime} the expiration time (can be null)
   * @param data   the actual string value
   */
  public record Data(
          LocalDateTime expiry,
          String data
  ){}
}
