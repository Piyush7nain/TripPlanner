package com.example.tripplanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TripPlannerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void test() {
        System.out.println("Hello World");
        Assertions.assertEquals(1,1);
    }

}
