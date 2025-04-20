package build.your.own.tcp.cmd;

import build.your.own.tcp.cmd.CommandHandler;
import build.your.own.logger.Logger;

import java.util.List;

public class PingCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(PingCommand.class);

  @Override
  public String execute(List<String> args) {
    logger.debug("Executing PING command");
    return "PONG";
  }
}
