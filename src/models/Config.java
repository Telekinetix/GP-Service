package models;
public class Config {
  public Integer cardPort;
  public Integer cardTimeout;
  public Integer serverPort;
  public Integer serverTimeout;
  public Integer tid;
  Config(Integer cardPort, Integer cardTimeout, Integer serverPort, Integer serverTimeout, Integer tid) {
    this.cardPort = cardPort;
    this.cardTimeout = cardTimeout;
    this.serverPort = serverPort;
    this.serverTimeout = serverTimeout;
    this.tid = tid;
  }
}
