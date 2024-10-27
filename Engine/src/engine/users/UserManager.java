package engine.users;

import java.util.*;

public class UserManager {

    private final List<String> usersSet;

    public UserManager() {
        usersSet = new ArrayList<>();
    }

    public synchronized void addUser(String username) {
        if (isUserExists(username)) {
            throw new IllegalArgumentException("Username " + username + " is already in use");
        }

        usersSet.add(username);
    }

    public synchronized void removeUser(String username) {
        usersSet.remove(username);
    }

    public synchronized List<String> getUsers() {
        return Collections.unmodifiableList(usersSet);
    }

    public boolean isUserExists(String username) {
        return usersSet.contains(username);
    }
}
