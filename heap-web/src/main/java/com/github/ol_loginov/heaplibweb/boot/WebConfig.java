package com.github.ol_loginov.heaplibweb.boot;

import com.github.ol_loginov.heaplibweb.support.JstlViewWIthCheck;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@EnableWebMvc
@ComponentScan("com.github.ol_loginov.heaplibweb.controllers")
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
}
