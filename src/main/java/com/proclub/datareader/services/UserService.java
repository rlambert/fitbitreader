package com.proclub.datareader.services;

import com.proclub.datareader.dao.User;
import com.proclub.datareader.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private UserRepo _repo;


    /**
     * ctor
     *
     * @param repo - UserRepo
     */
    @Autowired
    public UserService(UserRepo repo) {
        _repo = repo;
    }

    public User createUser(User user) {
        return _repo.save(user);
    }

    public User updateUser(User user) {
        return _repo.save(user);
    }

    public void deleteUser(User user) {
        _repo.delete(user);
    }

    public Optional<User> findById(UUID id) {
        return _repo.findById(id);
    }

    public long count() {
        return _repo.count();
    }

    public List<User> findByModifiedDateTimeAfter(int ts) {
        return _repo.findByModifiedDateTimeAfter(ts);
    }

    public List<User> findByModifiedDateTimeBetween(int tsStart, int tsEnd) {
        return _repo.findByModifiedDateTimeBetween(tsStart, tsEnd);
    }

    public List<User> findByEmail(String email) {
        return _repo.findByEmail(email);
    }

}
