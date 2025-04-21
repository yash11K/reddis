package build.your.own.tcp;

import build.your.own.resp.RESP;
import build.your.own.resp.RespData;
import build.your.own.resp.error.InvalidCommandError;
import build.your.own.resp.error.UnexpectedError;
import build.your.own.tcp.cmd.CommandRegistry;
import build.your.own.logger.Logger;

import java.io.*;
import java.util.UUID;

public class Process implements Runnable, AutoCloseable {
  private final UUID processId = UUID.randomUUID();
  private final BufferedReader input;
  private final OutputStream outputStream;
  private final Client client;
  private final CommandRegistry cmdRegistry = CommandRegistry.getInstance();
  private final Logger logger = Logger.getInstance(Process.class);

  public Process(Client client) throws IOException {
    this.input = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
    this.outputStream = new BufferedOutputStream(client.getSocket().getOutputStream());
    this.client = client;
    logger.debug(String.format("Process initialized - Process ID: %s, Client ID: %s", processId, client.getClientId()));
  }

  public UUID getProcessId() {
    return this.processId;
  }

  public BufferedReader getInput() {
    return this.input;
  }

  public OutputStream getOutput() {
    return this.outputStream;
  }

  public Client getClient() {
    return this.client;
  }

  @Override
  public void run() {
    String cmd;
    logger.info(String.format("Process started - Process ID: %s, Client ID: %s", processId, client.getClientId()));
    try {
      while ((cmd = this.getInput().readLine()) != null) {
        try {
          if(cmd.isEmpty()) {
            logger.debug("Empty command received, skipping");
            continue;
          }
          
          logger.debug(String.format("Processing command: '%s'", cmd));
          CommandRegistry.CommandMatchResult cmdMatch = cmdRegistry.commandMatchResult(cmd);
          RespData exec = null;

          if(cmdMatch != null) {
            logger.debug(String.format("Executing command: %s with args: %s", 
                cmdMatch.cmd().getClass().getSimpleName(), 
                String.join(", ", cmdMatch.args())));
            exec = cmdMatch.cmd().execute(cmdMatch.args());
          } else {
            logger.warn(String.format("Unknown command received: '%s'", cmd));
            exec = new InvalidCommandError("command not found, everyone needs help at some point");
          }

          outputStream.write(exec.serialize());
          outputStream.flush();
          logger.debug("Command execution completed successfully");

        } catch (Exception e) {
          logger.error(String.format("Error processing command '%s': %s", cmd, e.getMessage()));
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      logger.error(String.format("Connection interrupted - Client ID: %s, Process ID: %s, Error: %s", 
          getClient().getClientId(), getProcessId(), e.getMessage()));
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {
    logger.info(String.format("Closing process - Process ID: %s, Client ID: %s", processId, client.getClientId()));
    getInput().close();
    getOutput().close();
  }
}
