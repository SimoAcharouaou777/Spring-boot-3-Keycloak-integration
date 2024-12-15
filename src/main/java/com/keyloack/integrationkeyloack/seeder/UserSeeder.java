package com.keyloack.integrationkeyloack.seeder;

import com.keyloack.integrationkeyloack.entity.Role;
import com.keyloack.integrationkeyloack.entity.User;
import com.keyloack.integrationkeyloack.repository.RoleRepository;
import com.keyloack.integrationkeyloack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class UserSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRolesAndUsers();
    }

    private void seedRolesAndUsers() {

        Role adminRole = createRoleIfNotExists("ADMIN");
        Role employeeRole = createRoleIfNotExists("EMPLOYEE");
        Role userRole = createRoleIfNotExists("USER");


        createUserIfNotExists("admin", "admin123", Set.of(adminRole));
        createUserIfNotExists("employee", "employee123", Set.of(employeeRole));
        createUserIfNotExists("user", "user123", Set.of(userRole));
    }

    private Role createRoleIfNotExists(String roleName) {
        Optional<Role> roleOptional = roleRepository.findByName(roleName);
        if (roleOptional.isPresent()) {
            return roleOptional.get();
        }

        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    private void createUserIfNotExists(String username, String password, Set<Role> roles) {
        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("User already exists: " + username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        userRepository.save(user);

        System.out.println("Created user: " + username + " with roles: " + roles);
    }
}
