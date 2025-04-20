package build.your.own.tcp.cmd;

import build.your.own.logger.Logger;
import java.util.List;

public class EchoCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(EchoCommand.class);

  @Override
  public String execute(List<String> args) {
    logger.debug(String.format("Executing ECHO command with args: %s", args));
    
    if(!args.isEmpty()) {
      String response = String.join(" ", args);
      logger.debug(String.format("ECHO response: '%s'", response));
      return response;
    }
    
    logger.warn("ECHO command called without arguments");
    return null;
  }
}
