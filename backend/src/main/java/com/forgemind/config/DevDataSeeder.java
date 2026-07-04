package com.forgemind.config;

import com.forgemind.users.entity.Role;
import com.forgemind.users.entity.User;
import com.forgemind.users.repository.UserRepository;
import com.forgemind.workspaces.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkspaceService workspaceService;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@forgemind.ai")) {
            return;
        }

        User admin = User.builder()
                .email("admin@forgemind.ai")
                .username("admin")
                .fullName("ForgeMind Admin")
                .passwordHash(passwordEncoder.encode("Admin@12345"))
                .role(Role.ADMIN)
                .emailVerified(true)
                .enabled(true)
                .build();

        admin = userRepository.save(admin);
        workspaceService.createPersonalWorkspace(admin);

        log.info("Seeded default admin -> email: admin@forgemind.ai / password: Admin@12345");
    }
}