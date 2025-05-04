package build.your.own.persist;

import build.your.own.configurations.SystemConfig;
import build.your.own.database.DbMap;
import build.your.own.logger.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

//TODO : ADD SUPPORT FOR MULTIPLE DATA TYPES

/**
 * The {@code SerializeProtocol} class is responsible for serializing
 * a Redis-like in-memory database into a custom `.rdb`-like binary file format.
 *
 * <p>This format is custom-designed and is identifiable by a magic header ("BYDRDB").
 * It supports serializing key-value entries of type {@code Map<String, DbMap.Entry>},
 * where {@code Entry} consists of:
 * <ul>
 *   <li>{@code String data} — the actual value</li>
 *   <li>{@code LocalDateTime expiry} — optional expiration timestamp (can be null)</li>
 * </ul>
 *
 * <p>All strings are encoded in UTF-8. Currently, only string values are supported.
 * Future support for more data types (e.g., integers, lists) can be added via
 * type tagging.
 *
 * <h2>Serialization Format</h2>
 * <pre>
 * [ MAGIC_HEADER: UTF String ("BYDRDB") ]
 * [ TOTAL_ENTRIES: 4 bytes (int) ]
 *
 * For each entry:
 *   [ KEY_LENGTH: 4 bytes (int) ]
 *   [ KEY_BYTES: UTF-8 encoded bytes ]
 *   [ VALUE_BYTES: UTF-8 encoded bytes (currently: raw string, no type tag yet) ]
 *   [ EXPIRY_FLAG: 1 byte (0 = no expiry, 1 = has expiry) ]
 *   [ EXPIRY_TIMESTAMP: 8 bytes (long, epoch seconds, only if EXPIRY_FLAG == 1) ]
 * </pre>
 *
 * Example entry with key "foo", value "bar", expiry: 2025-01-01T00:00:00Z:
 * <pre>
 *   00 00 00 03   // key length = 3
 *   66 6f 6f      // key bytes = 'f', 'o', 'o'
 *   62 61 72      // value bytes = 'b', 'a', 'r'
 *   01            // expiry flag = 1
 *   00 00 01 89 6D 50 80 00  // epoch seconds
 * </pre>
 */
public class SerializeProtocol {
  private final Logger logger = Logger.getInstance(SerializeProtocol.class);
  private static final String MAGIC_HEADER = "BYDRDB";
  private static final Charset charsets = StandardCharsets.UTF_8;

  private final SystemConfig systemConfig;
  private final DbMap inMemoryMap;


  public SerializeProtocol(SystemConfig systemConfig, DbMap map) {
    this.systemConfig = systemConfig;
    this.inMemoryMap = map;
  }

  public void writeHeadersToCache() throws IOException{
    try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(systemConfig.getConfig().get("dbPath")))) {
      //Only write headers if the specified file is empty
      if(dataInputStream.readAllBytes().length == 0){
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(systemConfig.getConfig().get("dbPath")))) {
          dataOutputStream.writeUTF(MAGIC_HEADER);
        }
      }
    }
  }

  /**
   *
   * @throws IOException
   */
  public void saveToFile() throws IOException {
    logger.info(String.format("Starting database serialization to file: %s", systemConfig.getConfig().get("dbPath")));
    logger.debug(String.format("Total entries to serialize: %d", this.inMemoryMap.getInMemoryMap().size()));

    try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(systemConfig.getConfig().get("dbPath")))) {
      dataOutputStream.writeUTF(MAGIC_HEADER);
      dataOutputStream.writeInt(this.inMemoryMap.getInMemoryMap().size());
      int processedEntries = 0;
      int skippedEntries = 0;

      for (Map.Entry<String, DbMap.Data> entry : this.inMemoryMap.getEntrySet()) {
        if (entry.getValue() != null && entry.getValue().data() != null) {
          if (entry.getValue().expiry() == null) {
            saveEntryToFile(entry.getKey(), entry.getValue(), dataOutputStream);
          } else if (entry.getValue().expiry().isAfter(LocalDateTime.now())) {
            saveEntryToFile(entry.getKey(), entry.getValue(), dataOutputStream);
          } else {
            skippedEntries++;
            logger.debug(String.format("Skipping expired entry for key: %s", entry.getKey()));
          }
          processedEntries++;
        } else {
          skippedEntries++;
          logger.debug(String.format("Skipping null entry for key: %s", entry.getKey()));
        }
      }
        logger.info(String.format("Serialization complete - Processed: %d, Skipped: %d",
                processedEntries, skippedEntries));
      } catch(IOException e){
        logger.error(String.format("Failed to serialize database to file %s: %s", systemConfig.getConfig().get("dbPath"), e.getMessage()));
        throw e;
      }
    }

  private void saveEntryToFile(String key, DbMap.Data data, DataOutputStream dos) throws IOException {
      if(key != null && data.data() != null) {
        byte[] keyBytes = key.getBytes(charsets);
        dos.writeInt(keyBytes.length);
        dos.write(keyBytes);
        writeValue(data, dos);
      } else {
        logger.debug(String.format("Skipping null entry for key: %s", key));
      }
  }

  private void writeValue(DbMap.Data entry, DataOutputStream dos) throws IOException {
    assert entry != null;
    int length = entry.data().length();
    dos.writeInt(length);
    byte[] data = entry.data().getBytes(charsets);
    dos.write(data);
    writeExpiryForValue(entry.expiry(), dos);
    logger.debug(String.format("Wrote value with length %d bytes%s",
        data.length, 
        entry.expiry() != null ? String.format(", expiry: %s", entry.expiry()) : ""));
  }

  private void writeExpiryForValue(LocalDateTime expiry, DataOutputStream dos) throws IOException {
    if(expiry != null) {
      dos.writeByte(1);
      long epochSeconds = expiry.toEpochSecond(ZoneOffset.UTC);
      dos.writeLong(epochSeconds);
      logger.debug(String.format("Wrote expiry timestamp: %d", epochSeconds));
    } else {
      dos.writeByte(0);
      logger.debug("Wrote no expiry flag");
    }
  }

  public void loadDbMapFromCacheFile() throws IOException, IllegalAccessError{
    logger.info(String.format("Trying to search for existing cache file : %s", systemConfig.getConfig().get("dbPath")));

    //FileInputStream Just reads raw bytes, DataInputStream reads data in a  structured way from these raw bytes
    try(DataInputStream dataInputStream = new DataInputStream(new FileInputStream(systemConfig.getConfig().get("dbPath")))) {
      String magicHeader = dataInputStream.readUTF();
      if(!magicHeader.equals(MAGIC_HEADER)){
        logger.error(String.format("Failed to recognize header %s", magicHeader));
        throw new IllegalAccessError("Invalid File for preload caching");
      }
      else {
        /*
         *  * <pre> Serialization protocol
         *  *   00 00 00 03   // key length = 3
         *  *   66 6f 6f      // key bytes = 'f', 'o', 'o'
         *  *   62 61 72      // value bytes = 'b', 'a', 'r'
         *  *   01            // expiry flag = 1
         *  *   00 00 01 89 6D 50 80 00  // epoch seconds
         *  * </pre>
         */

        int totalEntries = dataInputStream.readInt();
        logger.info(String.format("Total Entries to process -: %s", totalEntries));
        while(totalEntries > 0){
          int keyLength = dataInputStream.readInt();

          byte[] key = dataInputStream.readNBytes(keyLength);

          int valueLength = dataInputStream.readInt();
          byte[] value = dataInputStream.readNBytes(valueLength);

          boolean isExpiryValid = dataInputStream.readBoolean();
          LocalDateTime expiry = null;
          if(isExpiryValid){
            long epoch = dataInputStream.readLong();
            expiry = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(epoch),
                    ZoneId.systemDefault()
            );
          }

          inMemoryMap.putValue(new String(key), new String(value), expiry);
          totalEntries--;
        }
      }
    }catch (EOFException endoffile){
      logger.error("End Of file");
    }
  }

  public DbMap getInMemoryMap() {
    return inMemoryMap;
  }
}
