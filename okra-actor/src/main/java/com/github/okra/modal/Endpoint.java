package com.github.okra.modal;

import java.io.Serializable;

public class Endpoint implements Serializable {
  private String host;
  private Integer port;

  private Endpoint() {}

  public static Endpoint of(String host, Integer port) {
    Endpoint endpoint = new Endpoint();
    endpoint.setHost(host);
    endpoint.setPort(port);
    return endpoint;
  }

  public String getHost() {
    return host;
  }

  private void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  private void setPort(Integer port) {
    this.port = port;
  }
}
