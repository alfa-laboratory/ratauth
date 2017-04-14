package ru.ratauth.inmemory.ip.providers;

import ru.ratauth.exception.AuthorizationException;
import ru.ratauth.exception.RegistrationException;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.providers.auth.dto.AuthResult;
import ru.ratauth.providers.auth.dto.BaseAuthFields;
import ru.ratauth.providers.registrations.RegistrationProvider;
import ru.ratauth.providers.registrations.dto.RegInput;
import ru.ratauth.providers.registrations.dto.RegResult;
import rx.Observable;

import java.util.HashMap;

public class InMemoryAuthProvider implements AuthProvider, RegistrationProvider {


  public static final String REG_CREDENTIAL = "credential";
  public static final String REG_CODE = "123";

  @Override
  public Observable<AuthResult> authenticate(AuthInput input) {
    if (input.getData().get(BaseAuthFields.USERNAME.val()).equals("login") && input.getData().get(BaseAuthFields.PASSWORD.val()).equals("password")) {
      HashMap<String, String> map = new HashMap<>();
      map.put(BaseAuthFields.USER_ID.val(), "user_id");
      return Observable.just(AuthResult.builder().data(map).status(AuthResult.Status.SUCCESS).build());
    } else {
      return Observable.error(new AuthorizationException(AuthorizationException.ID.CREDENTIALS_WRONG));
    }
  }

  @Override
  public boolean isAuthCodeSupported() {
    return false;
  }

  @Override
  public Observable<Boolean> checkUserStatus(AuthInput input) {
    return Observable.just(true);
  }

  @Override
  public Observable<RegResult> register(RegInput input) {
    String login = input.getData().get(BaseAuthFields.USERNAME.val());
    String password = input.getData().get(BaseAuthFields.PASSWORD.val());
    if (!input.getData().containsKey(BaseAuthFields.CODE.val())) { //first step of registration
      //one step registration
      if (login != null && login.equals("login") && password != null & password.equals("password")) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(BaseAuthFields.USER_ID.val(), "user_id");
        return Observable.just(RegResult.builder()
                .data(map)
                .status(RegResult.Status.SUCCESS)
                .build());
      } else if (input.getData().get(REG_CREDENTIAL).equals("credential")) {
        //two step registration
        HashMap<String, Object> map = new HashMap<>();
        map.put(BaseAuthFields.USERNAME.val(), "login");
        map.put(BaseAuthFields.CODE.val(), "code");
        return Observable.just(RegResult.builder()
                .data(map)
                .status(RegResult.Status.NEED_APPROVAL)
                .build());
      } else {
        return Observable.error(new RegistrationException("Registration failed"));
      }
    } else {//second step of registration
      if (input.getData().get(BaseAuthFields.CODE.val()).equals(REG_CODE) && login.equals("login")) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(BaseAuthFields.USER_ID.val(), "user_id");
        return Observable.just(RegResult.builder()
                .redirectUrl("http://relying.party/gateway")
                .data(map)
                .status(RegResult.Status.SUCCESS)
                .build());
      } else {
        return Observable.error(new RegistrationException("Registration failed"));
      }
    }
  }

  @Override
  public boolean isRegCodeSupported() {
    return true;
  }

}
