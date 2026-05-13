package com.afarms.user.config;

import com.afarms.user.model.entity.User;
import com.afarms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin-key}")
    private String adminKey;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing database with default admin user...");

        boolean adminExists = userRepository.findByEmail(adminEmail).isPresent() ||
                (adminUsername != null && userRepository.findByEmail(adminUsername).isPresent());

        if (!adminExists) {
            log.info("Creating default admin user...");
            User admin = User.builder()
                    .email(adminEmail)
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .farmId(null)
                    .build();
            userRepository.save(admin);
            log.info("Default admin created. Admin Key: {}", adminKey);
        } else {
            log.info("Admin already exists.");
        }

        log.info("Stats - Masters: {}, SubUsers: {}, Admins: {}",
                userRepository.findByRole("MASTER").size(),
                userRepository.findByRole("SUB_USER").size(),
                userRepository.findByRole("ADMIN").size());
    }
}