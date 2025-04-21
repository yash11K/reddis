package build.your.own.resp;

import java.util.ArrayList;
import java.util.List;

import static build.your.own.resp.RESP.CRLF;

//*<number-of-elements>\r\n<element-1>...<element-n>

/**
 * An asterisk (*) as the first byte.
 * One or more decimal digits (0..9) as the number of elements in the array as an unsigned, base-10 value.
 * The CRLF terminator.
 * An additional RESP type for every element of the array.
 */
public class Arrays<K extends RespData> implements RespData{
  public List<K> data;

  public Arrays() {
    this.data = new ArrayList<>();
  }

  @Override
  public String toString() {
    StringBuilder sb  = new StringBuilder();
    sb.append("*");
    sb.append(data.size());
    sb.append(CRLF);
    for(RespData data : data){
      sb.append(data.toString());
    }
    return sb.toString();
  }

  @Override
  public byte[] serialize() {
    return toString().getBytes();
  }

  public void add(K e){
    data.add(e);
  }
}

