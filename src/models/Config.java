package models;
public class Config {
  public Integer cardPort;
  public Integer cardTimeout;
  public Integer serverPort;
  public Integer serverTimeout;
  Config(Integer cardPort, Integer cardTimeout, Integer serverPort, Integer serverTimeout) {
    this.cardPort = cardPort;
    this.cardTimeout = cardTimeout;
    this.serverPort = serverPort;
    this.serverTimeout = serverTimeout;
  }
}
