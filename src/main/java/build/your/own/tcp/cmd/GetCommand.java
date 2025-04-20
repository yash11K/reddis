package build.your.own.tcp.cmd;

import build.your.own.database.DbMap;
import build.your.own.logger.Logger;

import java.util.List;

public class GetCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(GetCommand.class);

  @Override
  public String execute(List<String> args) {
    logger.debug(String.format("Executing GET command with args: %s", args));
    
    if(args.isEmpty()) {
      logger.warn("GET command called without key argument");
      return "--ERR not enough arguments specified see help";
    }
    
    String key = args.getFirst();
    String value = DbMap.getInMemoryMap().getValue(key);
    logger.debug(String.format("GET operation - Key: '%s', Value found: %s", key, value != null));
    return value;
  }
}
