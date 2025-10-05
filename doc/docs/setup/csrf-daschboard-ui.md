# Admin Dashbaord UI and CSRF

Axios should work with the following spring config out of the box with csrf:

## Maven Setup

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-ui</artifactId>
    <version>${spt.version}</version>
</dependency>
```

## Spring setup

```java
@EnableWebSecurity
@SpringBootApplication
@EnableSpringPersistentTasks
@EnableSpringPersistentTasksUI
public class XYApplication
```

## Spring Boot CSRF config

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .httpBasic(org.springframework.security.config.Customizer.withDefaults())
        .csrf(c ->
            c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
             .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        );
    return http.build();
}
```

more informations: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
