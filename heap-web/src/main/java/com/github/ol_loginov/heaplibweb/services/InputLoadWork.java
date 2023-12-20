package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.InputFileLoad;
import com.github.ol_loginov.heaplibweb.repository.InputFileLoadRepository;
import com.github.ol_loginov.heaplibweb.repository.InputFileStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InputLoadWork implements Runnable {
    private final TransactionOperations transactionOperations;
    private final InputFileLoadRepository inputFileLoadRepository;

    private InputFileLoad entity;
    private int entityId;

    public InputLoadWork withEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    @Override
    public void run() {
        try {
            runUnsafe();
        } catch (Throwable e) {
            log.error("load failure: {}", e.getMessage(), e);
        }
    }

    private void runUnsafe() {
        loadEntity();
    }

    private void loadEntity() {
        entity = transactionOperations.execute(st -> inputFileLoadRepository
                .findById(entityId)
                .orElseThrow());
        if (entity == null) {
            throw new DataRetrievalFailureException("no load with id #" + entityId);
        }

        entity.setStatus(InputFileStatus.LOADING);
        entity.setLoadProgress(0f);
        transactionOperations.execute(st -> inputFileLoadRepository.save(entity));
    }
}
