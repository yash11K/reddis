package build.your.own.tcp;

import build.your.own.logger.Logger;
import build.your.own.tcp.cmd.CommandRegistry;

import java.net.Socket;
import java.util.UUID;

public class Client implements Runnable {
  private final UUID clientId = UUID.randomUUID(); // Fixed typo: clintId -> clientId
  private final Socket socket;
  private final Logger logger = Logger.getInstance(Client.class);
  private final CommandRegistry commandRegistry;

  public Client(Socket socket, CommandRegistry commandRegistry) {
    this.commandRegistry = commandRegistry;
    this.socket = socket;
  }

  public UUID getClientId() { // Fixed typo: getClintId -> getClientId
    return clientId;
  }

  public Socket getSocket() {
    return socket;
  }

  @Override
  public void run() {
    logger.info(String.format("Client thread started - Client ID: %s", this.clientId));
    try {
      new Thread(new Process(this, commandRegistry)).start();
      logger.debug(String.format("Process thread started for client: %s", this.clientId));
    } catch (Exception e) {
      logger.error(String.format("Failed to start process for client %s: %s", this.clientId, e.getMessage()));
      e.printStackTrace();
    }
  }
}