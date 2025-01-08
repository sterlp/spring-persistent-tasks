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
    private static final String INDEX_HTML = "forward:/task-ui/index.html";
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward root 
        registry.addViewController(BASE).setViewName(INDEX_HTML);
        // Forward paths under /task-ui that do not contain a "." to index.html
        registry.addViewController(BASE + "/{path:[^\\.]*}")
                .setViewName(INDEX_HTML);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static assets with caching enabled
        registry.addResourceHandler(BASE + "/assets/**")
                .addResourceLocations("classpath:/static" + BASE + "/assets/")
                .setCacheControl(CacheControl.maxAge(90, TimeUnit.DAYS));

        // Serve other static resources under /task-ui
        registry.addResourceHandler(BASE + "/**")
                .addResourceLocations("classpath:/static" + BASE + "/");
    }
}
