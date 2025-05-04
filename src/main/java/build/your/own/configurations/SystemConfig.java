package build.your.own.configurations;

import java.util.HashMap;
import java.util.Map;

public class SystemConfig {
  private final Map<String, String> config = new HashMap<>();
  private final Map<String, String> replicaInfo  = new HashMap<>();

  public Map<String, String> getConfig() {
    return config;
  }

  public Map<String, String> getReplicaInfo() {
    return replicaInfo;
  }

  public void setConfig(String key, String value) {
    config.put(key, value);
  }

  public void setReplicaInfo(String key, String value) {
    replicaInfo.put(key, value);
  }

  public SystemConfig() {
  }
}
