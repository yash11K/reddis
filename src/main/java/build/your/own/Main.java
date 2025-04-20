package build.your.own;

import build.your.own.tcp.Client;
import build.your.own.logger.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Main {
  private static final Logger logger = Logger.getInstance(Main.class);
  public static final Map<String, String> config = new HashMap<>();

  public static void main(String[] args) {
    Logger.init(Logger.Level.INFO, Main.class);
    logger.info("Starting Redis Implementation");

    logger.info("Process System Args: ");
    try{
      loadSystemArgs(args);
    }catch (IllegalArgumentException stop){
      return;
    }

    int port = 6379;
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);
      logger.info("Server started successfully on port " + port);

      while(true) {
        try {
          Socket newClient = serverSocket.accept();
          logger.info("New client connected from: " + newClient.getRemoteSocketAddress());
          new Thread(new Client(newClient)).start();
        } catch (IOException e) {
          logger.error("Failed to accept client connection: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      logger.error("Server startup failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void loadSystemArgs(String[] args){
    try{
      for(int i = 0; i < args.length; i++){
        switch (args[i]){
          case "--dir":
            i++;
            config.put("dir", args[i]);
            logger.info("Setting --dir to " + config.get("dir"));
          case "--dbfile":
            i++;
            config.put("dbfile",args[++i]);
            logger.info("Setting --dbfile to " + config.get("dbfile"));
        }
      }

      if(config.get("dir") == null){
        //default directory for rdb files
        config.put("dir", "/tmp/redis/files");
        logger.warn("Setting default --dir " + config.get("dir"));
      }

      if(config.get("dbfile") == null){
        config.put("dbfile", LocalDateTime.now() + "rdb");
        logger.warn("Setting default --dbfile " + config.get("dbfile"));
      }

      config.put("dbPath", config.get("dir") + "/" + config.get("dbfile"));
      logger.info("Final rdb path -: " + config.get("dbPath"));

    }catch (IndexOutOfBoundsException illegal){
      throw new IllegalArgumentException();
    }

  }
}
