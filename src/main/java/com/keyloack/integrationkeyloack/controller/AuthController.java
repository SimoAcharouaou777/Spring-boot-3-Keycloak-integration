package com.keyloack.integrationkeyloack.controller;

import com.keyloack.integrationkeyloack.dto.AuthRequest;
import com.keyloack.integrationkeyloack.dto.AuthResponse;
import com.keyloack.integrationkeyloack.dto.RegisterRequest;
import com.keyloack.integrationkeyloack.service.AuthService;
import com.keyloack.integrationkeyloack.service.UserDetailsServiceImpl;
import com.keyloack.integrationkeyloack.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        Map<String , String > tokens = authService.authenticateAndGenerateToken(authRequest);
        return ResponseEntity.ok(tokens);

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        String response = authService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String,String> request){
        String refreshToken = request.get("refresh_token");
        if(refreshToken == null){
            return ResponseEntity.badRequest().body("Refresh token is missing");
        }

        try{
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(jwtUtil.isTokenValid(refreshToken, userDetails)){
                Collection<SimpleGrantedAuthority> authorities = userDetails.getAuthorities()
                        .stream()
                        .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                        .toList();

                String newAccessToken = jwtUtil.generateToken(username, authorities);
                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}
