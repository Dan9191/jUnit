package com.dan.junit.service;

import com.dan.junit.extension.GlobalExtension;
import org.junit.jupiter.api.extension.ExtendWith;

// this extension classes inherit all classes, who implement this class
@ExtendWith({
        GlobalExtension.class
})
public class TestBase  {
}
