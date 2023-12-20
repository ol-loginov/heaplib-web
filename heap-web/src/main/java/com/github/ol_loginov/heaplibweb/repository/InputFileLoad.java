package com.github.ol_loginov.heaplibweb.repository;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "InputFileLoad")
public class InputFileLoad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String relativePath;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InputFileStatus status;
    @Column(nullable = false)
    private Instant loadStart;
    @Column
    private Instant loadFinish;
    @Column
    private Float loadProgress;
    @Column
    private String loadError;
}
