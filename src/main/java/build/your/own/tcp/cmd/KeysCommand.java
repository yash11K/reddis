package build.your.own.tcp.cmd;

import build.your.own.database.DbMap;
import build.your.own.logger.Logger;
import build.your.own.persist.SerializeProtocol;
import build.your.own.resp.Arrays;
import build.your.own.resp.BulkString;
import build.your.own.resp.RespData;

import java.util.List;
import java.util.Map;

public class KeysCommand implements CommandHandler{
  private final Logger logger = Logger.getInstance(KeysCommand.class);

  public KeysCommand(SerializeProtocol serializeProtocol) {
    this.serializeProtocol = serializeProtocol;
  }

  private final SerializeProtocol serializeProtocol;

  @Override
  public RespData execute(List<String> args) {
    logger.debug(String.format("Executing KEYS command with args: %s", args));
    if(args.getFirst().equals("*")){
      logger.debug("Fetching all keys from: inMemoryDB");
      Arrays<BulkString> arrays = new Arrays<>();

      serializeProtocol.getInMemoryMap().getEntrySet().forEach(
              (Map.Entry<String, DbMap.Data> entry) -> {
                arrays.add(new BulkString(entry.getKey()));
              }
      );
      return arrays;
    }
    else{
      return new BulkString("null");
    }
  }
}
