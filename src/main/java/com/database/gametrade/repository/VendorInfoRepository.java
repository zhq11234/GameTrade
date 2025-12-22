package com.database.gametrade.repository;

import com.database.gametrade.entity.VendorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorInfoRepository extends JpaRepository<VendorInfo, String> {
    
    // 根据企业名查找厂商信息
    Optional<VendorInfo> findByCompanyName(String companyName);
    
    // 检查企业名是否存在
    boolean existsByCompanyName(String companyName);
    
    // 根据账号查找厂商信息
    Optional<VendorInfo> findByAccount(String account);
    
    // 检查账号是否存在
    boolean existsByAccount(String account);
}
