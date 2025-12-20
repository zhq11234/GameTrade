package com.database.gametrade.service;

import com.database.gametrade.entity.User;
import com.database.gametrade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 用户登录验证
     */
    public User login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 简单密码验证（实际项目中应该使用加密密码）
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        // 登录失败
        return null;
    }
    
    /**
     * 用户注册
     */
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            return false; // 用户名已存在
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            return false; // 邮箱已存在
        }
        
        // 保存用户
        userRepository.save(user);
        return true;
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
