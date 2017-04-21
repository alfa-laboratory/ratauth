package ru.ratauth.inmemory.ip.providers

import groovy.transform.CompileStatic
import ru.ratauth.exception.AuthorizationException
import ru.ratauth.exception.RegistrationException
import ru.ratauth.providers.auth.AuthProvider
import ru.ratauth.providers.auth.dto.AuthInput
import ru.ratauth.providers.auth.dto.AuthResult
import ru.ratauth.providers.registrations.RegistrationProvider
import ru.ratauth.providers.registrations.dto.RegInput
import ru.ratauth.providers.registrations.dto.RegResult
import ru.ratauth.providers.registrations.dto.RegResult.Status
import rx.Observable

import static java.lang.String.format
import static ru.ratauth.exception.AuthorizationException.ID.CREDENTIALS_WRONG
import static ru.ratauth.providers.auth.dto.AuthResult.Status.SUCCESS
import static ru.ratauth.providers.auth.dto.BaseAuthFields.*
import static ru.ratauth.providers.registrations.dto.RegResult.Status.NEED_APPROVAL

@CompileStatic
class InMemoryAuthProvider implements AuthProvider, RegistrationProvider {

  private static final String REG_CREDENTIAL = "credential"
  private static final String REG_CODE = "123"
  private final Map<String, User> users

  InMemoryAuthProvider(List<User> users) {
    this.users = users.collectEntries { [it.userName, it] }
  }

  @Override
  Observable<AuthResult> authenticate(AuthInput input) {

    String userName = input.data[USERNAME.val()]
    String password = input.data[PASSWORD.val()]

    User user = users.get(userName)

    if (user == null) {
      throw new IllegalArgumentException(format("User with such username: %s doesn't exist", userName))
    }

    if ((userName == user.userName) && (password == user.password)) {
      return Observable.just(AuthResult.builder()
          .data([(USER_ID.val()): user.userId] as Map)
          .status(SUCCESS)
          .build())
    } else {
      return Observable.error(new AuthorizationException(CREDENTIALS_WRONG))
    }
  }

  @Override
  boolean isAuthCodeSupported() {
    return false
  }

  @Override
  Observable<Boolean> checkUserStatus(AuthInput input) {
    return Observable.just(true)
  }

  @Override
  Observable<RegResult> register(RegInput regInput) {

    String userName = regInput.data[USERNAME.val()]
    String password = regInput.data[PASSWORD.val()]

    User user = new User()
    user.userName = userName
    user.userId = userName
    user.password = password

    if (isFirstStepOfRegistration(regInput)) {
      if (userName != null) {
        user.code = UUID.randomUUID().toString()
        users.put(user.userName, user)
        return Observable.just(oneStepOfRegistration(user))
      }
    } else if (isSecondStepOfRegistration(regInput, user)) {
      return Observable.just(secondStepOfRegistration(user))
    }
    return Observable.error(new RegistrationException("Registration failed"))
  }

  private boolean isFirstStepOfRegistration(RegInput regInput) {
    return !regInput.data.containsKey(CODE.val())
  }

  private RegResult oneStepOfRegistration(User user) {
    return RegResult.builder()
        .data([(USER_ID.val()): user.userId] as Map)
        .status(Status.SUCCESS)
        .build()

  }

  private RegResult twoStepRegistration(User user) {
    return RegResult.builder()
        .data(
        [
            (USERNAME.val()): user.userName,
            (CODE.val())    : user.code
        ] as Map)
        .status(NEED_APPROVAL)
        .build()
  }

  private boolean isSecondStepOfRegistration(RegInput regInput, User user) {
    String userName = regInput.data[USERNAME.val()]
    String code = regInput.data[CODE.val()]
    return (code == REG_CODE) && (userName == user.userName)
  }

  private RegResult secondStepOfRegistration(User user) {
    return RegResult.builder()
        .redirectUrl("http://relying.party/gateway")
        .data([(USER_ID.val()): user.userId] as Map)
        .status(Status.SUCCESS)
        .build()
  }

  @Override
  boolean isRegCodeSupported() {
    return true
  }

}
