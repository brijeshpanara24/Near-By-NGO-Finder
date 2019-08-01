package com.example.userapp;

import com.example.userapp.DataClass.DonationDetails;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DonationListTest {

    @Test
    public void test1() {

        ArrayList<DonationDetails> list1 = new ArrayList<>();
        list1.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list1.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));
        list1.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list1.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));

        ArrayList<DonationDetails> list2 = new ArrayList<>();
        list2.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));
        list2.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list2.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list2.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));

        DonationList donationList = new DonationList();
        donationList.sortByName = true;
        donationList.sortByAmount = false;
        donationList.sortByDate = false;
        donationList.allDonations = list1;
        ArrayList<DonationDetails> recyclerList = donationList.getSortedList();

        assertEquals(true,recyclerList.equals(list2));
    }

    @Test
    public void test2() {

        ArrayList<DonationDetails> list1 = new ArrayList<>();
        list1.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list1.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));
        list1.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list1.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));

        ArrayList<DonationDetails> list2 = new ArrayList<>();
        list2.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list2.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));
        list2.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list2.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));

        DonationList donationList = new DonationList();
        donationList.sortByName = false;
        donationList.sortByAmount = true;
        donationList.sortByDate = false;
        donationList.allDonations = list1;
        ArrayList<DonationDetails> recyclerList = donationList.getSortedList();

        assertEquals(true,recyclerList.equals(list2));
    }

    @Test
    public void test3() {

        ArrayList<DonationDetails> list1 = new ArrayList<>();
        list1.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list1.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));
        list1.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list1.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));

        ArrayList<DonationDetails> list2 = new ArrayList<>();
        list2.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));
        list2.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list2.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list2.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));

        DonationList donationList = new DonationList();
        donationList.sortByName = true;
        donationList.sortByAmount = true;
        donationList.sortByDate = false;
        donationList.allDonations = list1;
        ArrayList<DonationDetails> recyclerList = donationList.getSortedList();

        assertEquals(true,recyclerList.equals(list2));
    }

    @Test
    public void test4() {

        ArrayList<DonationDetails> list1 = new ArrayList<>();
        list1.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list1.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));
        list1.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list1.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));

        ArrayList<DonationDetails> list2 = new ArrayList<>();
        list2.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list2.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));
        list2.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list2.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));

        DonationList donationList = new DonationList();
        donationList.sortByName = false;
        donationList.sortByAmount = true;
        donationList.sortByDate = false;
        donationList.allDonations = list1;
        ArrayList<DonationDetails> recyclerList = donationList.getSortedList();

        assertEquals(true,recyclerList.equals(list2));
    }

    @Test
    public void test5() {

        ArrayList<DonationDetails> list1 = new ArrayList<>();
        list1.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list1.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));
        list1.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list1.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));

        ArrayList<DonationDetails> list2 = new ArrayList<>();
        list2.add(new DonationDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC","705","16/12/2019"));
        list2.add(new DonationDetails("smit@gmail.com","smit","def@gmail.com","DEF","501","18/12/2019"));
        list2.add(new DonationDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI","1000","31/12/2018"));
        list2.add(new DonationDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM","101","14/12/2018"));

        DonationList donationList = new DonationList();
        donationList.sortByName = true;
        donationList.sortByAmount = true;
        donationList.sortByDate = true;
        donationList.allDonations = list1;
        ArrayList<DonationDetails> recyclerList = donationList.getSortedList();

        assertEquals(true,recyclerList.equals(list2));
    }


    @Test
    public void test6() {

        ArrayList<DonationDetails> list1 = new ArrayList<>();
        ArrayList<DonationDetails> list2 = new ArrayList<>();

        DonationList donationList = new DonationList();
        donationList.sortByName = false;
        donationList.sortByAmount = false;
        donationList.sortByDate = true;
        donationList.allDonations = list1;
        ArrayList<DonationDetails> recyclerList = donationList.getSortedList();

        assertEquals(true,recyclerList.equals(list2));
    }
}