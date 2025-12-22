package com.database.gametrade.repository;

import com.database.gametrade.entity.BuyerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerInfoRepository extends JpaRepository<BuyerInfo, String> {
    
    // 根据昵称查找买家信息
    Optional<BuyerInfo> findByNickname(String nickname);
    
    // 检查昵称是否存在
    boolean existsByNickname(String nickname);
    
    // 根据账号查找买家信息
    Optional<BuyerInfo> findByAccount(String account);
    
    // 检查账号是否存在
    boolean existsByAccount(String account);
}
