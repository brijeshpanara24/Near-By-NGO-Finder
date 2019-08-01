package com.example.userapp;

import com.example.userapp.DataClass.NgoDetails;
import com.example.userapp.DataClass.UserDetails;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseTest {

    @Test
    public void test1() {
        UserDetails user = new UserDetails("Brijesh Panara","Rajkot, Gujarat","9033403367","brijeshpanara24@gmaiil.com");
        Database.getInstance().setUser(user);

        assertEquals(user,Database.getInstance().getUser());
    }

    @Test
    public void test2() {
        NgoDetails ngo = new NgoDetails("Navjeevan Trust","Rajkot, Gujarat","9913047920","navjeevan@gmail.com","1.234","12.34");
        Database.getInstance().addNgo(ngo);

        assertEquals(ngo,Database.getInstance().getNgoDetails("navjeevan@gmail.com"));
    }

    @Test
    public void test3() {
        NgoDetails ngo = new NgoDetails("Navjeevan Trust","Rajkot, Gujarat","9913047920","navjeevan@gmail.com","1.234","12.34");
        Database.getInstance().addNgo(ngo);

        assertEquals(null,Database.getInstance().getNgoDetails("navjeevan123@gmail.com"));
    }

    @Test
    public void test4() {
    }
}