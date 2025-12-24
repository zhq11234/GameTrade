package com.database.gametrade.repository;

import com.database.gametrade.entity.GameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameInfoRepository extends JpaRepository<GameInfo, String> {

    // 根据游戏名查找游戏信息
    Optional<GameInfo> findByGameName(String gameName);

    // 检查游戏名是否存在
    boolean existsByGameName(String gameName);

    // 根据游戏类别查找游戏列表
    List<GameInfo> findByCategory(String category);

    // 根据企业名查找游戏列表
    List<GameInfo> findByCompanyName(String companyName);

    // 根据状态查找游戏列表
    List<GameInfo> findByStatus(String status);

    // 根据版号查找游戏信息
    Optional<GameInfo> findByLicenseNumber(String licenseNumber);

    // 检查版号是否存在
    boolean existsByLicenseNumber(String licenseNumber);
}
