package build.your.own.resp.error;

public class UnexpectedError extends Error{
  private static final int code = 10001;
  private static final String error = "ERR UNEXPECTED ERROR";

  public UnexpectedError(String message) {
    super(message, error, code);
  }
}
