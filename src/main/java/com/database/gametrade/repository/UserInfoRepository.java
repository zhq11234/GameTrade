package com.database.gametrade.repository;

import com.database.gametrade.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    
    // 根据账号查找用户信息
    Optional<UserInfo> findByAccount(String account);
    
    // 检查账号是否存在
    boolean existsByAccount(String account);
    
    // 检查联系方式是否存在
    boolean existsByContact(String contact);
    
    // 根据账号和密码查找用户（用于登录验证）
    Optional<UserInfo> findByAccountAndPassword(String account, String password);
}
