package com.keyloack.integrationkeyloack.controller;

import com.keyloack.integrationkeyloack.entity.User;
import com.keyloack.integrationkeyloack.service.UserService;
import com.keyloack.integrationkeyloack.service.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserServiceImpl userServiceImpl;

    public AdminController(UserServiceImpl userServiceImpl){
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userServiceImpl.createUser(user));
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable Long id, @RequestParam String roleName) {
        userServiceImpl.assignRoleToUser(id, roleName);
        return ResponseEntity.ok().build();
    }
}
