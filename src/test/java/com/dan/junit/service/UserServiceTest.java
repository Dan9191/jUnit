package com.dan.junit.service;

import com.dan.junit.dto.User;
import com.dan.junit.extension.ConditionalExtension;
import com.dan.junit.extension.GlobalExtension;
import com.dan.junit.extension.PostProcessingExtension;
import com.dan.junit.extension.ThrowableExtension;
import com.dan.junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestMethodOrder(MethodOrderer.MethodName.class)
//@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        ThrowableExtension.class
        // GlobalExtension.class             // we included custom extension, but now we extend TestBase
})
public class UserServiceTest extends TestBase {

    private static final User PETR = User.of(2, "Petr", "111");
    private static final User IVAN = User.of(1, "Ivan", "123");
    private  UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init(){
        System.out.println("Before all:");
    }

    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before each:" + this);
        this.userService = userService;
    }

    @Test
    @Order(1)
    void usersEmptyIfNoUserAdded() throws IOException {
        if (true) {
            throw new RuntimeException("Custom exception for check throwable extension");
        }
        System.out.println("test1:" + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty());
    }

    @Tag("test1")
    @Test
    @Disabled  // used to disable the test
    void userSizeIfUserAdded() {
        System.out.println("test2:" + this);

        userService.add(IVAN);
        userService.add(PETR);
        List<User> users = userService.getAll();
        assertThat(users).hasSize(2);
    }



    //@Test
    // RepeatedTest used to check flaky tests
    @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME) // number of repetitions for the test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> userMap = userService.getAllConvertedById();

        assertAll(
                () -> assertThat(userMap).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(userMap).containsValues(IVAN, PETR)
        );
    }

    // assertTimeout used to check test execution time
    @Test
    // @Timeout(value = 200, unit = TimeUnit.MILLISECONDS) can be used to check test execution time
    void checkLoginFunctionalityPerformance() {
        var result = assertTimeout(Duration.ofMillis(200L), () -> {
            Thread.sleep(150L);
            return userService.login("dummy", IVAN.getPassword());
        });
    }

    // assertTimeoutPreemptively used to check test in separate thread
    @Test
    void checkLoginFunctionalityPerformanceWithSeparateThread() {
        System.out.println(Thread.currentThread().getName());
        var result = assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
            System.out.println(Thread.currentThread().getName());
            Thread.sleep(150L);
            return userService.login("dummy", IVAN.getPassword());
        });
    }

    @AfterEach
    void deleteDateFromDatabase() {
        System.out.println("After each:" + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("AfterAll: ");
    }

    @Nested
    @DisplayName("user test login functionality")
    class LoginTest {
        @Test
   //     @Order(2)
        // default DisplayName - method name
        @DisplayName("login success")
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
    //    @Order(3)
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

        @ParameterizedTest(name = "{arguments} test")
        //@ArgumentsSource()
        //@NullSource
        //@EmptySource
        //@NullAndEmptySource
        //@ValueSource(strings = {"Ivan", "Petr"}) - применимо, если используется один параметр в методе
        @MethodSource("com.dan.junit.service.UserServiceTest#getArgumentsForLoginTest")
        //@CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1) - применимо, если используется простые типы
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);

            Optional<User> maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(maybeUser);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
