package com.dan.junit.service;

import com.dan.junit.dto.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    private static final User PETR = User.of(2, "Petr", "111");
    private static final User IVAN = User.of(1, "Ivan", "123");
    private  UserService userService;

    @BeforeAll
    static void init(){
        System.out.println("Before all:");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each:" + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded() {
        System.out.println("test1:" + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void userSizeIfUserAdded() {
        System.out.println("test2:" + this);

        userService.add(IVAN);
        userService.add(PETR);
        List<User> users = userService.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getName(), IVAN.getPassword());

        assertThat(maybeUser).isPresent();
       // assertTrue(maybeUser.isPresent());
       // maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
    }

    @Test
    void throwExceptionIfUsernameOrPasswordIsNull() {
//        try {
//            userService.login(null, "dummy");
//            fail("login should throw exception on null username");
//        } catch (IllegalArgumentException ex) {
//            assertTrue(true);
//        }
        assertAll(
                () -> {
                    Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                    assertThat(ex.getMessage()).isEqualTo("username or password is null");
                },
                () -> assertThrows( IllegalArgumentException.class, () -> userService.login("dummy", null))

        );

    }

    @Test
    void loginFailIfPasswordNotCorrect() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getName(), "dadad");

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login("dummy", IVAN.getPassword());

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> userMap = userService.getAllConvertedById();

        assertAll(
                () -> assertThat(userMap).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(userMap).containsValues(IVAN, PETR)
        );
    }

    @AfterEach
    void deleteDateFromDatabase() {
        System.out.println("After each:" + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("AfterAll: ");
    }
}
