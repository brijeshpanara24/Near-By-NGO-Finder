package com.example.userapp;

import com.example.userapp.DataClass.SubscriptionDetails;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SubscribedNgoTest {

    @Test
    public void test1() {
        ArrayList<SubscriptionDetails> list1 = new ArrayList<>();
        list1.add(new SubscriptionDetails("dsc1@gmail.com","DSC","darshan@gmail.com","darshan"));
        list1.add(new SubscriptionDetails("dsc2@gmail.com","DSC","smit@gmail.com","smit"));
        list1.add(new SubscriptionDetails("dsc3@gmail.com","DSC","brijesh@gmail.com","brijesh"));
        list1.add(new SubscriptionDetails("dsc4@gmail.com","DSC","rahul@gmail.com","rahul"));

        ArrayList<SubscriptionDetails> list2 = new ArrayList<>();
        list2.add(new SubscriptionDetails("dsc1@gmail.com","DSC","darshan@gmail.com","darshan"));
        list2.add(new SubscriptionDetails("dsc2@gmail.com","DSC","smit@gmail.com","smit"));
        list2.add(new SubscriptionDetails("dsc3@gmail.com","DSC","brijesh@gmail.com","brijesh"));
        list2.add(new SubscriptionDetails("dsc4@gmail.com","DSC","rahul@gmail.com","rahul"));

        SubscribedNgo subscribedNgo = new SubscribedNgo();
        ArrayList<SubscriptionDetails> recyclerList = subscribedNgo.getSortedList(list1);

        assertEquals(true,recyclerList.equals(list2));
    }

    @Test
    public void test2() {
        ArrayList<SubscriptionDetails> list1 = new ArrayList<>();
        list1.add(new SubscriptionDetails("darshan@gmail.com","darshan","dsc@gmail.com","DSC"));
        list1.add(new SubscriptionDetails("smit@gmail.com","smit","dsc@gmail.com","DSC"));
        list1.add(new SubscriptionDetails("brijesh@gmail.com","brijesh","dsc@gmail.com","DSC"));
        list1.add(new SubscriptionDetails("rahul@gmail.com","rahul","dsc@gmail.com","DSC"));

        ArrayList<SubscriptionDetails> list2 = new ArrayList<>();
        list2.add(new SubscriptionDetails("brijesh@gmail.com","brijesh","dsc@gmail.com","DSC"));
        list2.add(new SubscriptionDetails("darshan@gmail.com","darshan","dsc@gmail.com","DSC"));
        list2.add(new SubscriptionDetails("rahul@gmail.com","rahul","dsc@gmail.com","DSC"));
        list2.add(new SubscriptionDetails("smit@gmail.com","smit","dsc@gmail.com","DSC"));

        SubscribedNgo subscribedNgo = new SubscribedNgo();
        ArrayList<SubscriptionDetails> recyclerList = subscribedNgo.getSortedList(list1);

        assertEquals(true,recyclerList.equals(list2));
    }

    @Test
    public void test3() {
        ArrayList<SubscriptionDetails> list1 = new ArrayList<>();
        list1.add(new SubscriptionDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI"));
        list1.add(new SubscriptionDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM"));
        list1.add(new SubscriptionDetails("smit@gmail.com","smit","def@gmail.com","DEF"));
        list1.add(new SubscriptionDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC"));

        ArrayList<SubscriptionDetails> list2 = new ArrayList<>();
        list2.add(new SubscriptionDetails("brijesh@gmail.com","brijesh","ghi@gmail.com","GHI"));
        list2.add(new SubscriptionDetails("darshan@gmail.com","darshan","abc@gmail.com","ABC"));
        list2.add(new SubscriptionDetails("rahul@gmail.com","rahul","klm@gmail.com","KLM"));
        list2.add(new SubscriptionDetails("smit@gmail.com","smit","def@gmail.com","DEF"));

        SubscribedNgo subscribedNgo = new SubscribedNgo();
        ArrayList<SubscriptionDetails> recyclerList = subscribedNgo.getSortedList(list1);

        assertEquals(true,recyclerList.equals(list2));
    }


    @Test
    public void test4() {
        ArrayList<SubscriptionDetails> list1 = new ArrayList<>();

        ArrayList<SubscriptionDetails> list2 = new ArrayList<>();

        SubscribedNgo subscribedNgo = new SubscribedNgo();
        ArrayList<SubscriptionDetails> recyclerList = subscribedNgo.getSortedList(list1);

        assertEquals(true,recyclerList.equals(list2));
    }

}