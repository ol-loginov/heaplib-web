package com.github.ol_loginov.heaplibweb.boot;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableJpaRepositories("com.github.ol_loginov.heaplibweb.repository")
@EntityScan("com.github.ol_loginov.heaplibweb.repository")
public class RepositoryConfig {
}
