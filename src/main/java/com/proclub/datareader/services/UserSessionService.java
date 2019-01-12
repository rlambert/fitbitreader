package com.proclub.datareader.services;

import com.proclub.datareader.dao.UserSession;
import com.proclub.datareader.repositories.UserSessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSessionService {

    private UserSessionRepo _repo;

    /**
     * ctor
     *
     * @param repo - UserRepo
     */
    @Autowired
    public UserSessionService(UserSessionRepo repo) {
        _repo = repo;
    }

    public UserSession createUserSession(UserSession session) {
        return _repo.save(session);
    }

    public UserSession updateSession(UserSession session) {
        return _repo.save(session);
    }

    public void deleteUserSession(UserSession session) {
        _repo.delete(session);
    }

    public Optional<UserSession> findById(UUID id) {
        return _repo.findById(id);
    }

    public long count() {
        return _repo.count();
    }

    /**
     *     List<UserSession> findAllOrOrderBySessionTokenExpiresDate();
     *
     *     List<UserSession> findBySessionTokenExpiresDateAfterOrOrderBySessionTokenExpiresDate(int startDate);
     *
     *     List<UserSession> findByRememberUntilAfterOrderByRememberUntil(int startDate);
     */
    public List<UserSession> findAll() {
        return _repo.findAll();
    }

    public List<UserSession> findBySessionTokenExpiresDate(int startTs) {
        return _repo.findBySessionTokenExpiresDateAfterOrderBySessionTokenExpiresDate(startTs);
    }

    public List<UserSession> findByRememberUntil(int startTs) {
        return _repo.findByRememberUntilAfterOrderByRememberUntil(startTs);
    }

    public List<UserSession> findAll(Sort sort) {
        return _repo.findAll(sort);
    }
}
