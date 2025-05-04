package build.your.own.replication;

public class ServerState {

  private ServerRole role;

  public ServerState(ServerRole role) {
    this.role = role;
  }

  public ServerRole getRole() {
    return role;
  }

  public void setRole(ServerRole role) {
    this.role = role;
  }
}
