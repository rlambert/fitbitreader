package com.proclub.datareader.services;

import com.proclub.datareader.dao.Client;
import com.proclub.datareader.repositories.ClientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ClientService {

    private ClientRepo _repo;

    /**
     * ctor
     *
     * @param repo - ClientRepo
     */
    @Autowired
    public ClientService(ClientRepo repo) {
        _repo = repo;
    }

    public Client createClient(Client client) {
        return _repo.save(client);
    }

    public Client updateClient(Client client) {
        return _repo.save(client);
    }

    public void deleteClient(Client client) {
        _repo.delete(client);
    }

    public List<Client> findByFnameAndLname(String fname, String lname) {
        return _repo.findByFnameAndLname(fname, lname);
    }

    public List<Client> findByLastName(String lname) {
        return _repo.findByLname(lname);
    }

    public List<Client> findByFkTrackerGuid(String guid) {
        return _repo.findByFkTrackerGuid(guid);
    }

    public List<Client> findByEmail(String email) {
        return _repo.findByEmail(email);
    }

    public Optional<Client> findById(int id) {
        return _repo.findById(id);
    }

    public long count() {
        return _repo.count();
    }

    public List<Client> findAll() {
        return _repo.findAll();
    }
    public List<Client> findAll(Sort sort) {
        return _repo.findAll(sort);
    }
}