package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepo extends JpaRepository<User, String> {

    List<User> findByModifiedDateTimeAfter(int ts);

    List<User> findByModifiedDateTimeBetween(int tsStart, int tsEnd);

    List<User> findByEmail(String email);

    List<User> findByUserGuid(String userId);

    List<User> findByPostalCode(String code);
}
