package com.github.ol_loginov.heaplibweb.boot;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@EnableTransactionManagement
@ComponentScan("com.github.ol_loginov.heaplibweb.repository")
@EnableJpaRepositories("com.github.ol_loginov.heaplibweb.repository")
@EntityScan("com.github.ol_loginov.heaplibweb.repository")
public class RepositoryConfig {
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
