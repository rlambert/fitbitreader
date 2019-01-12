package com.proclub.datareader.services;

import com.proclub.datareader.dao.ActivityLevel;
import com.proclub.datareader.repositories.ActivityLevelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityLevelService {

    private ActivityLevelRepo _repo;

    /**
     * ctor
     *
     * @param repo - ActivityLevelRepo
     */
    @Autowired
    public ActivityLevelService(ActivityLevelRepo repo) {
        _repo = repo;
    }

    public ActivityLevel createActivityLevel(ActivityLevel activityLevel) {
        return _repo.save(activityLevel);
    }

    public ActivityLevel updateActivityLevel(ActivityLevel activityLevel) {
        return _repo.save(activityLevel);
    }

    public void deleteActivityLevel(ActivityLevel activityLevel) {
        _repo.delete(activityLevel);
    }

    public void deleteActivityLevelById(long id) { _repo.deleteById(id);}

    public Optional<ActivityLevel> findById(long id) {
        return _repo.findById(id);
    }

    public long count() {
        return _repo.count();
    }

    public List<ActivityLevel> findAll() {
        return _repo.findAll();
    }

    public List<ActivityLevel> findAllByModifiedDateTimeBetween(Instant dtStart, Instant dtEnd) {
        return _repo.findAllByModifiedDateTimeBetween(dtStart, dtEnd);
    }

    public List<ActivityLevel> findAllSorted(Sort sort) {
        return _repo.findAll(sort);
    }

    public List<ActivityLevel> findByTrackDate(Instant dtStart, Instant dtEnd) {
        return _repo.findAllByTrackDateTimeBetween(dtStart, dtEnd);
    }

}
