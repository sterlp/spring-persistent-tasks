package org.sterl.spring.persistent_tasks_ui;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringPersistentTasksUIConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/persistentTask-ui").setViewName("/persistentTask-ui/index.html");
        //registry.addRedirectViewController("/persistentTask-ui/", "/persistentTask-ui");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/persistentTask-ui/assets/**") 
            .addResourceLocations("classpath:/static/persistentTask-ui/assets/") 
            .setCacheControl(CacheControl.maxAge(90, TimeUnit.DAYS));
    }
}
