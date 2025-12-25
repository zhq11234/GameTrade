package com.database.gametrade.service;

import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.repository.UserInfoRepository;
import com.database.gametrade.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private LogUtil logUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录验证
     */
    public UserInfo login(String account, String password) {
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isPresent()) {
            UserInfo user = userOptional.get();
            // 使用BCrypt验证密码
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        // 登录失败
        return null;
    }

    /**
     * 检查账号是否存在
     */
    public boolean checkAccountExists(String account) {
        return userInfoRepository.existsByAccount(account);
    }

    /**
     * 检查联系方式是否存在
     */
    public boolean checkContactExists(String contact) {
        return userInfoRepository.existsByContact(contact);
    }
}
