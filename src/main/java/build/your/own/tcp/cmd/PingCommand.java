package build.your.own.tcp.cmd;

import build.your.own.persist.SerializeProtocol;
import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;
import build.your.own.tcp.cmd.CommandHandler;
import build.your.own.logger.Logger;

import java.io.IOException;
import java.util.List;

public class PingCommand implements CommandHandler {
  private final Logger logger = Logger.getInstance(PingCommand.class);

  @Override
  public RespData execute(List<String> args) {
    logger.debug("Executing PING command");
    return new BulkString("PONG");
  }
}
