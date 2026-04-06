package com.bank.simulator.service;

import com.bank.simulator.dto.LoginRequest;
import com.bank.simulator.dto.LoginResponse;
import com.bank.simulator.dto.SignupRequest;
import com.bank.simulator.entity.UserEntity;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserEntity signup(SignupRequest request);
    LoginResponse login(LoginRequest request);
    Map<String, Object> checkCustomerExists(String email);
    List<Map<String, Object>> getAllUsers();
    Map<String, Object> getUserByEmail(String email);
    void updateUserStatus(String email, boolean active);
}
