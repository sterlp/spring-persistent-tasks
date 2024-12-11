package org.sterl.spring.persistent_tasks_ui;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PersistentTasksUiConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/task-ui").setViewName("/task-ui/index.html");
        registry.addRedirectViewController("/task-ui/", "/task-ui");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/task-ui/assets/**") 
                .addResourceLocations("classpath:/static/task-ui/assets/") 
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }
}
