package build.your.own.resp;

public class Text implements RespData{
  public Text(String data) {
    this.data = data;
  }

  public String data;

  @Override
  public byte[] serialize() {
    return data.getBytes();
  }
}
