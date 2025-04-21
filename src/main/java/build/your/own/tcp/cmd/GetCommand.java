package build.your.own.tcp.cmd;

import build.your.own.database.DbMap;
import build.your.own.logger.Logger;
import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;
import build.your.own.resp.error.IllegalArgumentError;

import java.util.List;

public class GetCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(GetCommand.class);

  @Override
  public RespData execute(List<String> args) {
    logger.debug(String.format("Executing GET command with args: %s", args));
    
    if(args.isEmpty()) {
      logger.warn("GET command called without key argument");
      return new IllegalArgumentError("not enough arguments specified see help");
    }
    
    String key = args.getFirst();
    String value = DbMap.getInMemoryMap().getValue(key);
    logger.debug(String.format("GET operation - Key: '%s', Value found: %s", key, value != null));
    return new BulkString(value);
  }
}
