package build.your.own.tcp.cmd;

import build.your.own.resp.RespData;

import java.util.List;

/**
 * Supporting Command Dispatch Architecture.
 */
public interface CommandHandler {
  /**
   * This method is implemented by each command class
   *
   * @param args arguments for the command
   * @return what needs to be printed to the outputStream
   */
  public RespData execute(List<String> args);
}
