package build.your.own.tcp.cmd;

import build.your.own.logger.Logger;
import java.util.*;

public class CommandRegistry {
  private static final CommandRegistry commandRegistry = new CommandRegistry();
  private final Logger logger = Logger.getInstance(CommandRegistry.class);
  public record CommandMatchResult(CommandHandler cmd, List<String> args){}

  private final Map<String, CommandHandler> registry = new HashMap<>();

  private CommandRegistry() {
    logger.info("Initializing CommandRegistry with default commands");
    register("PING", new PingCommand());
    register("ECHO", new EchoCommand());
    register("SET", new SetCommand());
    register("GET", new GetCommand());
    register("CONFIG", new ConfigCommand());
    register("KEYS", new KeysCommand());
    logger.info("CommandRegistry initialization complete with " + registry.size() + " commands");
  }

  public static CommandRegistry getInstance(){
    return commandRegistry;
  }

  public void register(String cmd, CommandHandler handler){
    logger.debug(String.format("Registering command: %s with handler: %s", cmd, handler.getClass().getSimpleName()));
    this.registry.put(cmd, handler);
  }

  public CommandHandler getHandler(String cmd){
    CommandHandler handler = this.registry.get(cmd);
    if (handler == null) {
      logger.debug(String.format("Handler not found for command: %s", cmd));
    }
    return handler;
  }

  public CommandMatchResult commandMatchResult(String cmd){
    String[] parts = cmd.trim().split(" ");
    logger.debug(String.format("Attempting to match command: '%s'", cmd));

    for(int i = parts.length - 1; i >= 0; i--){
      //Reverse check for command identification o(n^2) - considering no collisions
      //Trie is a better and scalable solution as the application grows in terms of users, sessions and commands - AUTO complete intuition solution to match the longest prefix
      String maybeCmd = String.join(" ", Arrays.copyOfRange(parts, 0, i+1)).toUpperCase();
      if(registry.containsKey(maybeCmd)){
        List<String> args = new ArrayList<>(Arrays.asList(parts).subList(i+1, parts.length));
        logger.info(String.format("Command matched: '%s' with args: %s", maybeCmd, args));
        return new CommandMatchResult(this.registry.get(maybeCmd), args);
      }
    }

    logger.warn(String.format("No matching command found for input: '%s'", cmd));
    return null;
  }
}
