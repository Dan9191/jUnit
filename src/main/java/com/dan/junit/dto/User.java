package com.dan.junit.dto;

import lombok.Value;

@Value(staticConstructor = "of")
public class User {

    Integer id;
    String name;
    String password;
}
