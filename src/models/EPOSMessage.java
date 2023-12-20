package models;

public class EPOSMessage {
  public Integer id;
  public String type;
  public String currency;
  public String value;

  public EPOSMessage(Integer id, String type, String currency, String value) {
    this.id = id;
    this.type = type;
    this.currency = currency;
    this.value = value;
  }
}
