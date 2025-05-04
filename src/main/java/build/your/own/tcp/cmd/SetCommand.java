package build.your.own.tcp.cmd;

import build.your.own.database.DbMap;
import build.your.own.logger.Logger;
import build.your.own.persist.SerializeProtocol;
import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;
import build.your.own.resp.error.IllegalArgumentError;
import build.your.own.resp.error.UnexpectedError;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

public class SetCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(SetCommand.class);
  private static final String PX_OPTION = "px";

  private final SerializeProtocol serializeProtocol;

  public SetCommand(SerializeProtocol serializeProtocol) {
    this.serializeProtocol = serializeProtocol;
  }

  @Override
  public RespData execute(List<String> args) {
    logger.debug(String.format("Executing SET command with args: %s", args));

    if (args.size() < 2) {
      logger.warn("Insufficient arguments for SET command");
      return new IllegalArgumentError("not enough arguments specified");
    }

    String key = args.get(0);
    String value = args.get(1);

    try {
      LocalDateTime expiry = parseExpiry(args);
      serializeProtocol.getInMemoryMap().putValue(key, value, expiry);

      // Offload serialization to a virtual thread
      Thread.startVirtualThread(() -> {
        try {
          serializeProtocol.saveToFile();
        } catch (IOException e) {
          e.printStackTrace();
          logger.error("Background serialization failed");
        }
      });

      logger.debug(String.format("Set key '%s' with%s expiry", key, expiry != null ? "" : "out"));
      return new BulkString("OK");

    } catch (IllegalArgumentError e) {
      logger.warn("SET command failed due to bad arguments: " + e.getMessage());
      return e;
    } catch (DateTimeException e) {
      logger.error("DateTime error: " + e.getMessage());
      return new IllegalArgumentError("date time exception");
    } catch (Exception e) {
      logger.error("Unexpected error in SET command: " + e.getMessage());
      return new UnexpectedError("Unexpected error");
    }
  }

  private LocalDateTime parseExpiry(List<String> args) throws IllegalArgumentError {
    int expiryIndex = args.indexOf(PX_OPTION);
    if (expiryIndex == -1) return null;

    // Ensure px is at the right index (e.g., SET key value px 1000)
    if (expiryIndex != 2) {
      throw new IllegalArgumentError("px flag must appear after key and value");
    }

    if (expiryIndex + 1 >= args.size()) {
      throw new IllegalArgumentError("Missing expiry value for px option");
    }

    String expiryStr = args.get(expiryIndex + 1);
    try {
      int expirySeconds = Integer.parseInt(expiryStr);
      return LocalDateTime.now().plusSeconds(expirySeconds);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentError("px expiry must be a valid number");
    }
  }
}
