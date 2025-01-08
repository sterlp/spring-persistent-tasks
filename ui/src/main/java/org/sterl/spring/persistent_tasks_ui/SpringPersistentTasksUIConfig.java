package org.sterl.spring.persistent_tasks_ui;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringPersistentTasksUIConfig implements WebMvcConfigurer {
    private static final String BASE = "/task-ui";
    private static final String INDEX_HTML = BASE + "/index.html";
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(BASE).setViewName("forward:" + INDEX_HTML);
        registry.addViewController(BASE + "/{path:^(?!index\\.html$).*$}")
                .setViewName("forward:" + INDEX_HTML);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(BASE + "/assets/**") 
            .addResourceLocations("classpath:/static" + BASE +  "/assets/") 
            .setCacheControl(CacheControl.maxAge(90, TimeUnit.DAYS));
    }
}
