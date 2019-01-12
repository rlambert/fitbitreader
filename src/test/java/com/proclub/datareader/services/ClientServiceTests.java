package com.proclub.datareader.services;

import com.proclub.datareader.dao.Client;
import com.proclub.datareader.services.ClientService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
@AutoConfigureTestDatabase
public class ClientServiceTests {

    @Autowired
    private ClientService _service;


    @Test
    public void testCrud() {

        Client client1 = new Client(TEST_CLIENT_TYPE1, TEST_FNAME1, TEST_LNAME1, TEST_EMAIL1, TEST_POSTAL_CODE1,
                TEST_FKTRACKER_GUID1, TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_FKGRPID1, TEST_LOGIN1, TEST_PWD1,
                0, true);

        client1 = _service.createClient(client1);
        assertTrue(client1.getClientId() != 0);

        Optional<Client> optClient1 = _service.findById(client1.getClientId());
        assertTrue(optClient1.isPresent());
        Client client2 = optClient1.get();
        assertEquals(client1, client2);

        // now update
        client1.setFname("Wilma");
        _service.updateClient(client1);

        // see if update worked
        optClient1 = _service.findById(client1.getClientId());
        assertTrue(optClient1.isPresent());
        assertTrue(optClient1.isPresent());
        client2 = optClient1.get();
        assertEquals(client2.getFname(), client1.getFname());

        // see if we have 1 in our list
        List<Client> clients = _service.findAll();
        assertTrue(clients.size() == 1);

        client2 = new Client(TEST_CLIENT_TYPE2, TEST_FNAME2, TEST_LNAME2, TEST_EMAIL2, TEST_POSTAL_CODE2,
                TEST_FKTRACKER_GUID2, TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_FKGRPID2, TEST_LOGIN2, TEST_PWD2,
                1, false);

        client2 = _service.createClient(client2);
        assertTrue(client2.getClientId() > 0);
        assertTrue(client2.getClientId() > client1.getClientId());

        long count = _service.count();
        assertEquals(2, count);

        // make sure we return 2
        clients = _service.findAll();
        assertTrue(clients.size() == 2);

        clients = _service.findByFnameAndLname(TEST_FNAME2, TEST_LNAME2);
        assertTrue(clients.size() == 1);

        _service.deleteClient(client1);
        count = _service.count();
        assertEquals(1, count);

        _service.deleteClient(client2);
        count = _service.count();
        assertEquals(0, count);
    }
}