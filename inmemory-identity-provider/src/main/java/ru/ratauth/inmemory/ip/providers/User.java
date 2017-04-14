package ru.ratauth.inmemory.ip.providers;

import lombok.Data;

@Data
public class User {

  private String USERNAME;
  private String USER_ID;
  private String PASSWORD;
  private String CODE;

}
