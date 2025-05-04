package build.your.own.persist;

import build.your.own.Main;
import build.your.own.database.DbMap;
import build.your.own.logger.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Backup Snapshots</h1>
 * Acts as a backup directory by default saved under the path {@link build.your.own.Main} Config DIR
 */
public class Snapshot {
  private static final Logger logger = Logger.getInstance(Snapshot.class);

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final SerializeProtocol serializeProtocol;

  public Snapshot(SerializeProtocol serializeProtocol) {
    this.serializeProtocol = serializeProtocol;
  }

  public void start(){
    scheduler.scheduleAtFixedRate(this::periodicSnapshots, 5, 10, TimeUnit.SECONDS);
  }

  private void periodicSnapshots() {
    try{
      logger.info("Initiate Periodic Snapshot of Cache at : " + LocalDateTime.now());
      serializeProtocol.saveToFile();
      logger.info("Next Snapshot scheduled at : " + LocalDateTime.now().plusHours(24));
    }catch (IOException e){
      logger.error("Error occurred while snapshot " + e);

    }
  }
}
