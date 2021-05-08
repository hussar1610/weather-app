package pl.makselan.weatherapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.makselan.weatherapp.entity.AppUser;
import pl.makselan.weatherapp.entity.ERole;
import pl.makselan.weatherapp.entity.Role;
import pl.makselan.weatherapp.payload.request.LoginRequest;
import pl.makselan.weatherapp.payload.request.SignupRequest;
import pl.makselan.weatherapp.payload.response.JwtResponse;
import pl.makselan.weatherapp.payload.response.MessageResponse;
import pl.makselan.weatherapp.repository.AppUserRepository;
import pl.makselan.weatherapp.repository.RoleRepository;
import pl.makselan.weatherapp.security.jwt.JwtUtils;
import pl.makselan.weatherapp.security.AppUserDetails;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {

    private AuthenticationManager authenticationManager;

    private AppUserRepository appUserRepository;

    private RoleRepository roleRepository;

    private JwtUtils jwtUtils;

    public AuthenticationController(AuthenticationManager authenticationManager, AppUserRepository appUserRepository, RoleRepository roleRepository, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        AppUserDetails appUserDetails = (AppUserDetails) authentication.getPrincipal();

        List<String> roles = appUserDetails.getAuthorities()
                .stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        appUserDetails.getId(),
                        appUserDetails.getUsername(),
                        roles
                )
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

        if (appUserRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error - Username is already taken!"));
        }

        AppUser appUser = new AppUser(
                signupRequest.getUsername(),
                signupRequest.getPassword()
        );

        Set<Role> roles = new HashSet<>();
        Set<String> stringRoles = signupRequest.getRole();

        if (stringRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error Role is not found"));
            roles.add(userRole);
        } else {
            for (String role : stringRoles) {
                switch (role) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            }
        }

        appUser.setRoles(roles);
        appUserRepository.save(appUser);

        return ResponseEntity.ok(
                new MessageResponse("Registration successful!")
        );
    }
}