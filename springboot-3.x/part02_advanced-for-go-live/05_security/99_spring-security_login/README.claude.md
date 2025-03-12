# Prompt

用 java 17, spring boot 3.2, spring security, grade, 寫一個 login 的基本框架, 滿足以下條件:

1. 使用 Spring Security 的 login form 當登入頁面
2. 使用者用本地認證，預設密碼使用 BCryptPasswordEncoder
3. Session Token 為 JWT, JWT 相關參數請放在 application.yaml
4. Database 使用 MySQL, 使用者資料表至少要有 username, password, role 欄位
5. Session Token 存放在 Redis Cache, 當使用者瀏覽的時候, backend 會先拿 cookie 中的 session Id 去 cache 檢查，如果存在，那表示使用者還是登入的狀態。

請提供完整的程式。

---

# Claude

我將為您创建一个基于Java 17、Spring Boot 3.2、Spring Security和Gradle的登录框架，满足您的所有要求。

```text
project-structure/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── auth/
│   │   │               ├── AuthApplication.java
│   │   │               ├── config/
│   │   │               │   ├── JwtConfig.java
│   │   │               │   ├── RedisConfig.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── WebConfig.java
│   │   │               ├── controller/
│   │   │               │   └── AuthController.java
│   │   │               ├── model/
│   │   │               │   ├── User.java
│   │   │               │   ├── Role.java
│   │   │               │   └── JwtSession.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   └── RoleRepository.java
│   │   │               ├── security/
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   ├── JwtTokenProvider.java
│   │   │               │   └── UserDetailsServiceImpl.java
│   │   │               └── service/
│   │   │                   ├── AuthService.java
│   │   │                   ├── AuthServiceImpl.java
│   │   │                   ├── RedisService.java
│   │   │                   └── RedisServiceImpl.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── templates/
│   │       └── static/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── auth/
│                       └── AuthApplicationTests.java
└── build.gradle

```

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'jakarta.xml.bind:jakarta.xml.bind-api:4.0.0'
    
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

```

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 60000

jwt:
  secret: YourSecretKeyHereItShouldBeVeryLongAndRandomForSecurityReasons
  expirationMs: 86400000 # 24 hours
  refreshExpirationMs: 604800000 # 7 days
  cookieName: JWT_SESSION
  
server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    org.springframework.security: DEBUG
    com.example.auth: DEBUG

```

```java
package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}

```

```java
// User.java
package com.example.auth.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Constructors
    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}

// Role.java
package com.example.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    public Role() {
    }

    public Role(ERole name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }

    public enum ERole {
        ROLE_USER,
        ROLE_ADMIN
    }
}

// JwtSession.java
package com.example.auth.model;

import java.io.Serializable;
import java.util.List;

public class JwtSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private List<String> roles;
    private String token;
    private long expirationTime;

    public JwtSession() {
    }

    public JwtSession(String username, List<String> roles, String token, long expirationTime) {
        this.username = username;
        this.roles = roles;
        this.token = token;
        this.expirationTime = expirationTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}

```

```java
// UserRepository.java
package com.example.auth.repository;

import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}

// RoleRepository.java
package com.example.auth.repository;

import com.example.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.ERole name);
}

```

```java
// JwtTokenProvider.java
package com.example.auth.security;

import com.example.auth.model.JwtSession;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;
    
    @Value("${jwt.cookieName}")
    private String cookieName;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public JwtSession getJwtSessionFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        String username = claims.getSubject();
        long expirationTime = claims.getExpiration().getTime();

        return new JwtSession(username, roles, token, expirationTime);
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        JwtSession jwtSession = getJwtSessionFromToken(token);
        
        Collection<GrantedAuthority> authorities = jwtSession.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(jwtSession.getUsername(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}

// JwtAuthenticationFilter.java
package com.example.auth.security;

import com.example.auth.model.JwtSession;
import com.example.auth.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.getTokenFromRequest(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Get username from token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                
                // Check if session exists in Redis
                JwtSession jwtSession = redisService.getJwtSession(username);
                
                if (jwtSession != null && jwtSession.getToken().equals(token)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    logger.info("User session not found in Redis or token mismatch");
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

// UserDetailsServiceImpl.java
package com.example.auth.security;

import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}

```

```java
// AuthService.java
package com.example.auth.service;

import com.example.auth.model.JwtSession;
import org.springframework.security.core.Authentication;

public interface AuthService {
    void logout(String username);
    JwtSession createJwtSession(Authentication authentication);
}

// AuthServiceImpl.java
package com.example.auth.service;

import com.example.auth.model.JwtSession;
import com.example.auth.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    public void logout(String username) {
        redisService.deleteJwtSession(username);
    }

    @Override
    public JwtSession createJwtSession(Authentication authentication) {
        String token = jwtTokenProvider.generateToken(authentication);
        JwtSession jwtSession = jwtTokenProvider.getJwtSessionFromToken(token);
        
        // Store in Redis
        redisService.saveJwtSession(jwtSession);
        
        return jwtSession;
    }
}

// RedisService.java
package com.example.auth.service;

import com.example.auth.model.JwtSession;

public interface RedisService {
    void saveJwtSession(JwtSession jwtSession);
    JwtSession getJwtSession(String username);
    void deleteJwtSession(String username);
}

// RedisServiceImpl.java
package com.example.auth.service;

import com.example.auth.model.JwtSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "jwt_session:";

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveJwtSession(JwtSession jwtSession) {
        String key = KEY_PREFIX + jwtSession.getUsername();
        long timeToLive = (jwtSession.getExpirationTime() - System.currentTimeMillis()) / 1000;
        
        redisTemplate.opsForValue().set(key, jwtSession, timeToLive, TimeUnit.SECONDS);
    }

    @Override
    public JwtSession getJwtSession(String username) {
        String key = KEY_PREFIX + username;
        return (JwtSession) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteJwtSession(String username) {
        String key = KEY_PREFIX + username;
        redisTemplate.delete(key);
    }
}

```

```java
// JwtConfig.java
package com.example.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private int expirationMs;
    private int refreshExpirationMs;
    private String cookieName;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(int expirationMs) {
        this.expirationMs = expirationMs;
    }

    public int getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(int refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
}

// RedisConfig.java
package com.example.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private int redisDatabase;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        redisConfig.setDatabase(redisDatabase);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}

// SecurityConfig.java
package com.example.auth.config;

import com.example.auth.security.JwtAuthenticationFilter;
import com.example.auth.security.JwtTokenProvider;
import com.example.auth.service.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, redisService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/api/auth/login")
                .defaultSuccessUrl("/api/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/api/auth/login?logout")
                .permitAll()
            );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

// WebConfig.java
package com.example.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

```

```java
// AuthController.java
package com.example.auth.controller;

import com.example.auth.model.JwtSession;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Value("${jwt.cookieName}")
    private String jwtCookieName;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));

        SecurityContextHolder.get
```