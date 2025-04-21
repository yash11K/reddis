package build.your.own.resp.error;

public class IllegalArgumentError extends Error{
  private static final int code = 10001;
  private static final String error = "ERR ILLEGAL ARGUMENT";
  public IllegalArgumentError(String message) {
    super(message, error, code);
  }
}
