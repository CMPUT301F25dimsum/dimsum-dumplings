package com.example.lotteryapp;

import com.example.lotteryapp.reusecomponent.UserProfile;

import org.junit.Test;

public class UserProfileUnitTest {
    @Test
    public void EqualityTest() {
        UserProfile p1 = new UserProfile();
        p1.uid = "joe";
        p1.email = "joe1@gmail.com";

        UserProfile p2 = new UserProfile();
        p2.uid = "joe";
        p2.email = "joe2@gmail.com";

        assert(p1.equals(p2));
    }
}
