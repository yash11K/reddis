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

public class SetCommand implements CommandHandler{
  private final Logger logger = Logger.getInstance(SetCommand.class);

  @Override
  public RespData execute(List<String> args) {
    logger.debug(String.format("Executing SET command with args: %s", args));

    //Check if SET command as px flag? process expiry : store with expiry as null
    try{
      int expiryIndex = args.indexOf("px");
      if(expiryIndex > 0){
        assert expiryIndex == 2;
        int expiry = expiryIndex + 1;
        if(expiry < args.size()){
          int expirySeconds = Integer.parseInt(args.get(expiry));
          logger.debug(String.format("Setting key '%s' with expiry of %d seconds", args.get(0), expirySeconds));
          DbMap.getInMemoryMap().putValue(args.get(0), args.get(1), LocalDateTime.now().plusSeconds(expirySeconds));
          try {
            SerializeProtocol.getInstance().saveEntryToFile(args.get(0), new DbMap.Data(LocalDateTime.now().plusSeconds(expirySeconds), args.get(1)));
          }catch (IOException e){
            logger.error(String.format("Failed to save entry to rdb file : %s", e.getMessage()));
            throw new RuntimeException(e);
          }
          return new BulkString("OK");
        }else{
          logger.warn("Missing expiry value for px option");
          return new IllegalArgumentError("illegal argument for -px expiry");
        }
      }
    }catch (NumberFormatException e){
      logger.error(String.format("Invalid expiry format: %s", e));
      return new IllegalArgumentError("illegal argument px expiry needs to be a number");
    }catch (DateTimeException dateTimeException){
      logger.error(String.format("DateTime error: %s", dateTimeException.getMessage()));
      return new IllegalArgumentError("date time exception");
    }

    if(args.size() < 2){
      logger.warn("Insufficient arguments for SET command");
      return new IllegalArgumentError("not enough arguments specified ");
    }

    try{
      logger.debug(String.format("Setting key '%s' without expiry", args.get(0)));
      DbMap.getInMemoryMap().putValue(args.get(0), args.get(1), null);
      return new BulkString("OK");
    }catch (Exception e){
      logger.error(String.format("Unexpected error in SET command: %s", e.getMessage()));
      return new UnexpectedError("Unexpected error");
    }
  }
}