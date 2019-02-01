package com.proclub.datareader.services;

import com.proclub.datareader.dao.User;
import com.proclub.datareader.dao.UserRowMapper;
import com.proclub.datareader.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserRepo _repo;
    private JdbcTemplate _jdbcTemplate;
    private UserRowMapper _mapper = new UserRowMapper();

    /**
     * ctor
     *
     * @param repo - UserRepo
     */
    @Autowired
    public UserService(UserRepo repo, JdbcTemplate jdbcTemplate) {
        _repo = repo;
        _jdbcTemplate = jdbcTemplate;
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


    public Optional<User> findById(String id) {
        //return _repo.findById(id);

        try {
            String guid = id.toString().toUpperCase();
            User user = (User) _jdbcTemplate.queryForObject(String.format("select * from Users where UserGuid = '%s'", guid), _mapper);
            if (user != null) {
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        }
        catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
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
