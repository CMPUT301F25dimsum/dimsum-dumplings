package com.example.lotteryapp;

import com.example.lotteryapp.reusecomponent.Lottery;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class LotteryUnitTest {
    @Test
    public void addEntrantTest() {
        Lottery l = new Lottery();

        l.addEntrant("joe");
        assert(l.containsEntrant("joe"));
        assert(l.entrantStatus.contains(LotteryEntrant.Status.Registered));
        l.addEntrant("joe");
        assert(l.getEntrants().size() == 1);
        assert(l.entrantStatus.size() == 1);
        l.setMaxEntrants(1);
        l.addEntrant("bob");
        assert(!l.containsEntrant("bob"));
        assert(l.getEntrants().size() == 1);
    }

    @Test
    public void isOpenTest() {
        Lottery l = new Lottery();
        l.registrationStart = new Date(0);
        LocalDate localDate = LocalDate.now().plusDays(7);
        l.registrationEnd = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        assert(l.isOpen());
        l.registrationEnd = new Date(1);
        assert(!l.isOpen());
    }

    @Test
    public void removeTest() {
        Lottery l = new Lottery();
        l.addEntrant("joe");

        l.removeEntrant("joe");
        assert(l.getEntrants().isEmpty());
        assert(l.entrantStatus.isEmpty());
    }
}
