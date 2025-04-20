package build.your.own.tcp.cmd;

import build.your.own.database.DbMap;
import build.your.own.logger.Logger;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

public class SetCommand implements CommandHandler{
  private final Logger logger = Logger.getInstance(SetCommand.class);

  @Override
  public String execute(List<String> args) {
    logger.debug(String.format("Executing SET command with args: %s", args));

    try{
      int expiryIndex = args.indexOf("px");
      if(expiryIndex > 0){
        int expiry = expiryIndex + 1;
        if(expiry < args.size()){
          int expirySeconds = Integer.parseInt(args.get(expiry));
          logger.debug(String.format("Setting key '%s' with expiry of %d seconds", args.get(0), expirySeconds));
          DbMap.getInMemoryMap().putValue(args.get(0), args.get(1), LocalDateTime.now().plusSeconds(expirySeconds));
          return "OK";
        }else{
          logger.warn("Missing expiry value for px option");
          return "--ERR illegal argument for -px expiry";
        }
      }
    }catch (NumberFormatException e){
      logger.error(String.format("Invalid expiry format: %s", e.getMessage()));
      return "-ERR illegal argument px expiry needs to be a number";
    }catch (DateTimeException dateTimeException){
      logger.error(String.format("DateTime error: %s", dateTimeException.getMessage()));
      return "--ERR date time exception";
    }

    if(args.size() < 2){
      logger.warn("Insufficient arguments for SET command");
      return "--ERR not enough arguments specified ";
    }
    try{
      logger.debug(String.format("Setting key '%s' without expiry", args.get(0)));
      DbMap.getInMemoryMap().putValue(args.get(0), args.get(1), null);
      return "OK";
    }catch (Exception e){
      logger.error(String.format("Unexpected error in SET command: %s", e.getMessage()));
      return "--ERR Unexpected error";
    }
  }
}