package com.example.ngoapp;

import com.example.ngoapp.DataClass.EventDetails;
import com.example.ngoapp.DataClass.NgoDetails;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DatabaseTest {

    @Test
    public void test1() {

        NgoDetails ngoDetails = new NgoDetails("Navjeevan Trust","Rajkot, Gujarat","9033403367","navjeevan@gmail.com","22.27803242903383","70.75746674090624");
        Database.getInstance().setNgo(ngoDetails);

        assertEquals(true,ngoDetails.equals(Database.getInstance().getNgo()));
    }

    @Test
    public void test2() {

        EventDetails eventDetails = new EventDetails("Cloth Drive","Navjeevan Trust","Cloth drive for poor people","Gandhinagar","12/02/2019","05:30","12/03/2019","12:30","food");
        String key = "keyAbcd0986";
        Database.getInstance().setEditNgoEvent(eventDetails,key);

        assertEquals(true,eventDetails.equals(Database.getInstance().getEditEventDetails()));
        assertEquals(key,Database.getInstance().getEditNgoEventKey());
    }


    @Test
    public void test3() {

        ArrayList<EventDetails> list1 = new ArrayList<>();
        list1.add(new EventDetails("Cloth Drive","Navjeevan Trust","Cloth drive for poor people","Gandhinagar","12/02/2019","05:30","12/03/2019","12:30","food"));
        list1.add(new EventDetails("Health Camp","Navjeevan Trust","Health Camp for poor people","Gandhinagar","12/02/2019","05:30","12/03/2019","12:30","food"));
        list1.add(new EventDetails("Blood Donation","Navjeevan Trust","Blood Donation for poor people","Gandhinagar","12/02/2019","05:30","12/03/2019","12:30","food"));
        list1.add(new EventDetails("Education Awareness","Navjeevan Trust","Education Awareness for poor people","Gandhinagar","12/02/2019","05:30","12/03/2019","12:30","food"));

        ArrayList<String> list2 = new ArrayList<>();
        list2.add("key1Abcd0986");
        list2.add("key2Efgh8d7d");
        list2.add("key3Igkl05s5");
        list2.add("key4sjhdgfsd");

        Database.getInstance().setAllEvents(list1,list2);

        assertEquals(true,list1.equals(Database.getInstance().getAllEvents()));
        assertEquals(true,list2.equals(Database.getInstance().getAllEventsKey()));
    }
}