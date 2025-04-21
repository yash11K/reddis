package build.your.own.tcp.cmd;

import build.your.own.logger.Logger;
import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;

import java.util.List;

public class EchoCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(EchoCommand.class);

  @Override
  public RespData execute(List<String> args) {
    logger.debug(String.format("Executing ECHO command with args: %s", args));
    
    if(!args.isEmpty()) {
      return new BulkString(String.join(" ", args));
    }

    logger.warn("ECHO command called without arguments");
    return new BulkString(null);
  }
}
