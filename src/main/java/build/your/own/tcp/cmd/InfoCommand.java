package build.your.own.tcp.cmd;

import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;
import build.your.own.resp.Text;

import java.util.List;

public class InfoCommand implements CommandHandler{
  private enum InfoArgs{
    REPLICATION
  }

  @Override
  public RespData execute(List<String> args) {
    if(args.isEmpty()){
      throw new IllegalArgumentException("missing argument for INFO");
    }

    String whichInfo = args.getFirst();

    switch (whichInfo){
      case "replication":
        return new BulkString(getInfo(InfoArgs.REPLICATION));
      default:
        throw new IllegalArgumentException("Invalid argument for INFO");
    }
  }

  private String getInfo(InfoArgs infoArgs) {
    switch (infoArgs){
      case REPLICATION -> {
        return getReplicationInfo();
      }
    }
    //unreachable block as will be blocked by caller function
    return null;
  }

  private String getReplicationInfo() {
    return "role:master";
  }
}
