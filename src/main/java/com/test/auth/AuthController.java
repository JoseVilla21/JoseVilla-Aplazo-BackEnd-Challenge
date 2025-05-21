package com.test.auth;

import com.test.auth.dto.AuthRequest;
import com.test.auth.dto.AuthResponse;
import com.test.auth.dto.RegisterRequest;
import com.test.data.model.Role;
import com.test.data.model.User;
import com.test.data.repository.RoleRepository;
import com.test.data.repository.UserRepository;
import com.test.response.ErrorResponse;
import lombok.Data;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpResponse;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UserRepository userRepo, RoleRepository roleRepo,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepo;
        this.roleRepository = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        log.info("Register endpoint called for username: {}", request.getUsername());
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                log.warn("Attempt to register with existing username: {}", request.getUsername());
                return ResponseEntity.badRequest().body("Username already exists");
            }
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEnabled(true);

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> {
                        log.error("ROLE_USER not found in database");
                        return new RuntimeException("ROLE_USER not found");
                    });
            user.getRoles().add(userRole);

            userRepository.save(user);
            log.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.ok("User registered");
        } catch (RuntimeException e) {
            log.error("Error occurred during registration for username: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        } finally {
            log.info("Register endpoint finished for username: {}", request.getUsername());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            log.info("Login successful for username: {}", request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            log.warn("Login failed for username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .code("APZ000007")
                    .error("UNAUTHORIZED")
                    .timestamp(Instant.now().getEpochSecond())
                    .message("Acceso no autorizado")
                    .path("/api/auth/login")
                    .build());
        }
    }
}

