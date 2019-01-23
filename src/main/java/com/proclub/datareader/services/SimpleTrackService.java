package com.proclub.datareader.services;

import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.repositories.SimpleTrackRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimpleTrackService {

    private SimpleTrackRepo _repo;

    /**
     * ctor
     *
     * @param repo - SimpleTrackRepo
     */
    @Autowired
    public SimpleTrackService(SimpleTrackRepo repo) {
        _repo = repo;
    }

    public SimpleTrack createSimpleTrack(SimpleTrack SimpleTrack) {
        return _repo.save(SimpleTrack);
    }

    public SimpleTrack updateSimpleTrack(SimpleTrack SimpleTrack) {
        return _repo.save(SimpleTrack);
    }

    public void deleteSimpleTrack(SimpleTrack SimpleTrack) {
        _repo.delete(SimpleTrack);
    }

    public void deleteSimpleTrackById(UUID id) { _repo.deleteById(id);}

    public Optional<SimpleTrack> findById(UUID id) {
        return _repo.findById(id);
    }

    public long count() {
        return _repo.count();
    }

    public List<SimpleTrack> findAll() {
        return _repo.findAll();
    }

    public List<SimpleTrack> findAllByModifiedDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findAllByModifiedDateTimeBetween(dtStart, dtEnd);
    }

    public List<SimpleTrack> findAllSorted(Sort sort) {
        return _repo.findAll(sort);
    }

    public List<SimpleTrack> findByTrackDate(LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findAllByTrackDateTimeBetween(dtStart, dtEnd);
    }

    public List<SimpleTrack> findByUserTrackDateRange(UUID userId, LocalDateTime dtStart, LocalDateTime dtEnd,
                                                       SimpleTrack.Entity etype) {
        // we get to assume FitBit as the source system for this app
        short src = SimpleTrack.SourceSystem.FITBIT.sourceSystem;
        return _repo.findByTrackDateTimeBetweenAndFkUserGuidAndSourceSystemAndEntityTypeOrderByTrackDateTime(
                            dtStart, dtEnd, userId, src, etype.entityValue);
    }

}
