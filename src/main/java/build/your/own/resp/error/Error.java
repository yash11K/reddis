package build.your.own.resp.error;

import build.your.own.resp.RespData;

import java.time.LocalDateTime;

import static build.your.own.resp.RESP.CRLF;

public abstract class Error implements RespData {
  private final String message;
  private final String error;
  private final int code;
  private final LocalDateTime timestamp = LocalDateTime.now();

  public Error(String message, String error, int code) {
    this.message = message;
    this.error = error;
    this.code = code;
  }

  @Override
  public byte[] serialize() {
    String text = "-" + error + (message != null ? " " + message : " ") + CRLF;
    return text.getBytes();
  }

  @Override
  public String toString() {
    return "-" + error + (message != null ? " " + message : "");
  }
}
