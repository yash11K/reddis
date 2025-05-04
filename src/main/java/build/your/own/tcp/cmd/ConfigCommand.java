package build.your.own.tcp.cmd;

import build.your.own.Main;
import build.your.own.configurations.SystemConfig;
import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;
import build.your.own.resp.error.IllegalArgumentError;
import build.your.own.resp.error.InvalidCommandError;

import java.util.List;

public class ConfigCommand implements CommandHandler{
  private final SystemConfig config;

  public ConfigCommand(SystemConfig config) {
    this.config = config;
  }

  @Override
  public RespData execute(List<String> args) {
    if(args.size() != 2){
      return new IllegalArgumentError("argument is not satisfied");
    }

    switch (args.getFirst()){
      case "GET":
        //get specific config
        return new BulkString(getConfig(args.get(1)));

      default:
        return new InvalidCommandError("Illegal Argument - not a valid command");
    }
  }

  private String getConfig(String key){
    return config.getConfig().get(key);
  }
}
