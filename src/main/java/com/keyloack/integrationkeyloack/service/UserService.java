package com.keyloack.integrationkeyloack.service;

import com.keyloack.integrationkeyloack.entity.User;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;


public interface UserService {
    User createUser(User user);
    void assignRoleToUser(Long id, String roleName);
    User getUser(String username);
}
