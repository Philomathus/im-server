package com.feiwin.imserver.config;

import com.feiwin.imserver.security.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilter() {
        FilterRegistrationBean<JwtFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new JwtFilter());

        filter.addUrlPatterns("/api/**");
        return filter;
    }

}
