package ru.ratauth.inmemory.ip.providers.repository;

import ru.ratauth.inmemory.ip.providers.domain.User;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class UserRepository {

    private final Map<String, User> users;

    public UserRepository(List<User> users) {
        this.users = users.stream().collect(toMap(User::getUserName, v -> v));
    }

    public User save(User user) {
        return users.put(user.getUserName(), user);
    }

    public User getByUserName(String userName) {
        return users.get(userName);
    }

}