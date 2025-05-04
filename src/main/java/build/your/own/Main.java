package build.your.own;

import build.your.own.configurations.SystemConfig;
import build.your.own.database.DbMap;
import build.your.own.persist.SerializeProtocol;
import build.your.own.persist.Snapshot;
import build.your.own.tcp.Client;
import build.your.own.logger.Logger;
import build.your.own.tcp.cmd.CommandRegistry;
import build.your.own.utils.ArgumentsUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  private static final Logger logger = Logger.getInstance(Main.class);


  public static void main(String[] args) {
    final SystemConfig config = new SystemConfig();
    final DbMap inMemoryDb = new DbMap();
    final SerializeProtocol serializeProtocol = new SerializeProtocol(config, inMemoryDb);
    final CommandRegistry commandRegistry = new CommandRegistry(config, serializeProtocol);

    logger.info("Starting Redis Server");
    logger.info("Process System Args: ");
    try{
      ArgumentsUtils.loadSystemArgs(args, config);
      logger.info("Initialize Snapshot CRON");
      Snapshot snapshot = new Snapshot(serializeProtocol);
      //FOR DISASTER BACKUP
      snapshot.start();

      //Reload SystemArgs after restart
      try {
        serializeProtocol.writeHeadersToCache();
        serializeProtocol.loadDbMapFromCacheFile();
      }catch (IOException e){
        e.printStackTrace();
        logger.error(String.format("Failed to reload from cache %s", e.getMessage()));
      }
    }catch (IllegalArgumentException stop){
      return;
    }catch (RuntimeException e){
      throw new RuntimeException(e);
    }

    //set default port
    config.getConfig().putIfAbsent("port", "6769");

    //This will never fail as reverse integer parsing has been already tested
    int port = Integer.parseInt(config.getConfig().get("port"));

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);
      logger.info("Server started successfully on port " + port);

      while(true) {
        try {
          Socket newClient = serverSocket.accept();
          logger.info("New client connected from: " + newClient.getRemoteSocketAddress());
          new Thread(new Client(newClient, commandRegistry)).start();
        } catch (IOException e) {
          logger.error("Failed to accept client connection: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      logger.error("Server startup failed: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
