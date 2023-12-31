package com.dan.junit.dao;

import java.util.HashMap;
import java.util.Map;

public class UserDaoSpy extends UserDao {


    private final UserDao userDao;

    private Map<Integer, Boolean> answers = new HashMap<>();
    //   private Answer1<Integer, Boolean> answer1;


    public UserDaoSpy(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean delete(Integer userId) {
        // реальный вызов userDao.delete(userId) должен быть "ленивым", и не вызываться при наличии userId в answers
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
