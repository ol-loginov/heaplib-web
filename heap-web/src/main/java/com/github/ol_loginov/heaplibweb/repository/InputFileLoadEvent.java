package com.github.ol_loginov.heaplibweb.repository;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "InputFileLoadEvent")
public class InputFileLoadEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private Instant tm;
    @ManyToOne
    @JoinColumn(name = "inputFileLoadId")
    private InputFileLoad inputFileLoad;
    @Column
    private float progress = 0f;
    @Column(nullable = false)
    private String message = "";
}
