package org.tokenscript.eip712;

public class Eip712InternalData {
  private String description;
  private String timestamp;

  public Eip712InternalData() {}

  public Eip712InternalData(String description, String timestamp) {
    this.description = description;
    this.timestamp = timestamp;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

}
