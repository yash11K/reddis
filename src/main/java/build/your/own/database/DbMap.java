package build.your.own.database;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
  /** Singleton instance of the in-memory map. */
  private static final DbMap inMemoryMapInstance = new DbMap(new HashMap<>());

  private final Map<String, Data> inMemoryMap;

  /**
   * Private constructor for singleton pattern.
   *
   * @param inMemoryMap the backing map used to store key-entry pairs
   */
  private DbMap(HashMap<String, Data> inMemoryMap) {
    this.inMemoryMap = inMemoryMap;
  }

  /**
   * Returns the singleton instance of {@code DbMap}.
   *
   * @return the shared in-memory map instance
   */
  public synchronized static DbMap getInMemoryMap() {
    return inMemoryMapInstance;
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
    Data data = inMemoryMap.get(key);
    if (data == null) return null;
    if(data.expiry !=null && data.expiry.isBefore(LocalDateTime.now())){
      inMemoryMap.remove(key);
    }
    if(inMemoryMap.get(key) != null){
      return inMemoryMap.get(key).data;
    }
    return null;
  }


  /**
   * Stores a key-value pair into the map with an optional expiration time.
   *
   * @param key the key to store
   * @param data {@link Data}
   */
  public void putValue(String key, Data data){
    this.inMemoryMap.put(key, data);
  }

  /**
   * Stores a key-value pair into the map with an optional expiration time.
   *
   * @param key the key to store
   * @param val the value to store
   * @param exp the expiration time (can be {@code null} if no expiry is desired)
   */
  public void putValue(String key, String val, LocalDateTime exp){
    this.inMemoryMap.put(key, new Data(exp, val));
  }


  public Set<Map.Entry<String, Data>> getEntrySet(){
    return this.inMemoryMap.entrySet();
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
