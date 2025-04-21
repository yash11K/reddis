package build.your.own.resp.error;

public class InvalidCommandError extends Error{
  private static final int code = 10003;
  private static final String error = "ERR INVALID COMMAND";

  public InvalidCommandError(String message) {
    super(message, error, code);
  }
}
