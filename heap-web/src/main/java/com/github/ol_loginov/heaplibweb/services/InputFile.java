package com.github.ol_loginov.heaplibweb.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputFile {
    private String path;
    private Instant modificationTime;
}
