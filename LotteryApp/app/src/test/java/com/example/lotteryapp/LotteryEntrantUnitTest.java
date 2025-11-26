package com.example.lotteryapp;

import com.example.lotteryapp.reusecomponent.LotteryEntrant;

import org.junit.Test;

public class LotteryEntrantUnitTest {
    @Test
    public void EqualityTest() {
        LotteryEntrant l1 = new LotteryEntrant("joe");
        LotteryEntrant l2 = new LotteryEntrant("joe", false, LotteryEntrant.Status.Waitlisted);

        assert(l1.equals(l2));
    }
}
