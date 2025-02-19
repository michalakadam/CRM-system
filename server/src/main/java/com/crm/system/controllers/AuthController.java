package com.crm.system.controllers;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.*;

import com.crm.system.exception.UserAlreadyExistsException;
import com.crm.system.playload.request.LoginDTO;
import com.crm.system.playload.request.SignUpDTO;
import com.crm.system.playload.response.MessageResponse;
import com.crm.system.security.services.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Auth controller", description = "Auth management APIs")
@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserDetailsServiceImpl userDetailsService, AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signin")
    @Operation(summary = "Login in system", tags = { "auth", "login" })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            ResponseEntity<?> responseEntity = userDetailsService.authenticateUser(authentication);
            log.info("User {} is logged", Objects.requireNonNull(responseEntity.getBody()).toString());
            return responseEntity;
        } catch (AuthenticationCredentialsNotFoundException e) {
            log.error("Authorisation Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Authorisation Error: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "New user registration", tags = { "auth", "registration" })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpDTO signUpRequest) {
        try {
            userDetailsService.registerUser(signUpRequest);
            log.info("User registered successfully!");
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (UserAlreadyExistsException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Registration error: " + e.getMessage()));
        }
    }
    @Operation(summary = "Logout", tags = { "auth", "logout" })
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = userDetailsService.logoutUser();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
    @Operation(summary = "Delete user", tags = { "auth", "admin", "delete" })
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUserById(@RequestParam long userId) {
        try {
            String responseText = userDetailsService.deleteUserById(userId);
            return ResponseEntity.ok(new MessageResponse(responseText));
        } catch (UserPrincipalNotFoundException | IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    @Operation(summary = "checkAuthorization", tags = { "auth", "check" })
    @GetMapping("/check/user-role")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> checkAuthorizationForUserRole()  {
        return ResponseEntity.ok(true);
    }

    @Operation(summary = "check authorization", tags = { "auth", "check" })
    @GetMapping("/check/admin-role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> checkAuthorizationForAdminRole() {
        return ResponseEntity.ok(true);
    }

    @Operation(summary = "check authorization", tags = { "auth", "check" })
    @GetMapping("/check")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok(true);
    }
}

