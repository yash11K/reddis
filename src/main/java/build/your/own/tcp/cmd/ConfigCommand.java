package build.your.own.tcp.cmd;

import build.your.own.Main;

import java.util.List;

public class ConfigCommand implements CommandHandler{
  @Override
  public String execute(List<String> args) {
    if(args.size() != 2){
      return "--ERR Illegal Argument - args not satisfied";
    }

    switch (args.getFirst()){
      case "GET":
        //get specific config
        return getConfig(args.get(1));

      default:
        return "--ERR Illegal Argument - not a valid command";
    }
  }

  private String getConfig(String key){
    String c = Main.config.get(key);
    if(c == null){
      return "-ERR Illegal argument - no such config";
    }
    return c;
  }
}
