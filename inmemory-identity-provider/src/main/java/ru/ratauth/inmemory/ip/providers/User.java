package ru.ratauth.inmemory.ip.providers;

import lombok.Data;

@Data
public class User {

  private String userName;
  private String userId;
  private String password;
  private String code;

}
