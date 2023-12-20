package com.github.ol_loginov.heaplibweb.boot;

import com.github.ol_loginov.heaplibweb.support.JstlViewWIthCheck;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@EnableWebMvc
@ComponentScan("com.github.ol_loginov.heaplibweb.controllers")
@Slf4j
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public UrlBasedViewResolver viewResolver() {
        var bean = new UrlBasedViewResolver();
        bean.setContentType("text/html;charset=UTF-8");
        bean.setPrefix("/WEB-INF/jsp/");
        bean.setSuffix(".jsp");
        bean.setViewClass(JstlViewWIthCheck.class);
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.setOrder(-1);
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
        registry.addResourceHandler("/favicon/**").addResourceLocations("/favicon/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/");
        registry.addResourceHandler("/browserconfig.xml").addResourceLocations("/");
        registry.addResourceHandler("/site.webmanifest").addResourceLocations("/");
    }

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent event) {
        var context = (ServletWebServerApplicationContext) event.getApplicationContext();
        System.out.println("\n\n**************************************************\n");
        System.out.println("You may visit: http://localhost:" + context.getWebServer().getPort() + "/");
        System.out.println("\n**************************************************\n\n");
    }
}
