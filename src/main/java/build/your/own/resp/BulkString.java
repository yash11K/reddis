package build.your.own.resp;

import static build.your.own.resp.RESP.CRLF;

public class BulkString implements RespData{
  public String data;

  public BulkString(String data) {
    this.data = data;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    //$<length>\r\n<data>\r\n
    sb.append("$");
    if(data != null){
      sb.append(data.length());
      sb.append(CRLF);
      sb.append(data);
      sb.append(CRLF);
    } else {
      sb.append(-1);
      sb.append(CRLF);
    }
    return sb.toString();
  }

  @Override
  public byte[] serialize() {
    return toString().getBytes();

  }
}
