package com.proclub.datareader.services;

import com.proclub.datareader.dao.AuditLog;
import com.proclub.datareader.repositories.AuditLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditLogService {

    private AuditLogRepo _repo;

    /**
     * ctor
     *
     * @param repo - ActivityLevelRepo
     */
    @Autowired
    public AuditLogService(AuditLogRepo repo) {
        _repo = repo;
    }

    List<AuditLog> findByUser(String uid) {
        return _repo.findByFkUserGuid(uid);
    }
    List<AuditLog> findByUserAndDateTime(String uid, LocalDateTime dtStart) {
        return _repo.findByFkUserGuidAndDateTimeAfter(uid, dtStart);
    }

    List<AuditLog> findByUserAndDateAndActivity(String uid, LocalDateTime dtStart, String activity) {
        return _repo.findByFkUserGuidAndDateTimeAfterAndActivityOrderByDateTimeDesc(uid, dtStart, activity);
    }

    List<AuditLog> findbyUserAndDateRangeAndActivity (String uid, LocalDateTime dtStart, LocalDateTime dtEnd, String activity) {
        return _repo.findByFkUserGuidAndDateTimeBetweenAndActivityOrderByDateTimeDesc(uid, dtStart, dtEnd, activity);
    }

    List<AuditLog> findByDateTimeAfter(LocalDateTime dtStart) {
        return _repo.findByDateTimeAfter(dtStart);
    }

    List<AuditLog> findByDateRange(LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findByDateTimeBetween(dtStart, dtEnd);
    }

    List<AuditLog> findByActivityAndDateTime(String activity, LocalDateTime dtStart) {
        return _repo.findByActivityAndDateTimeAfter(activity, dtStart);
    }
    List<AuditLog> findByActivityAndDateRange(String activity, LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findByActivityAndDateTimeBetween(activity, dtStart, dtEnd);
    }
    List<AuditLog> findByUserAndActivityAndDateRange(String uid, String activity, LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findByFkUserGuidAndActivityAndDateTimeBetween(uid, activity, dtStart, dtEnd);
    }

    Optional<AuditLog> findById(long id) {
        return _repo.findById(id);
    }

    AuditLog createOrUpdate(AuditLog log) {
        return _repo.save(log);
    }

}
