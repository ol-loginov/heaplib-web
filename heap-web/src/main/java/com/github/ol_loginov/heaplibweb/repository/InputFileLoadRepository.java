package com.github.ol_loginov.heaplibweb.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InputFileLoadRepository extends ListCrudRepository<InputFileLoad, Integer> {
    List<InputFileLoad> findAllByOrderByLoadStartDesc();

    List<InputFileLoad> findAllByStatusIn(List<InputFileStatus> statuses);
    List<InputFileLoad> findAllByStatusNotIn(List<InputFileStatus> statuses);
}
