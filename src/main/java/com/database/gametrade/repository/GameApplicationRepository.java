package com.database.gametrade.repository;

import com.database.gametrade.entity.GameApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameApplicationRepository extends JpaRepository<GameApplication, Integer> {

    // 根据申请ID查找申请信息
    Optional<GameApplication> findByApplicationId(Integer applicationId);

    // 根据游戏名查找申请列表
    List<GameApplication> findByGameName(String gameName);

    // 根据企业名查找申请列表
    List<GameApplication> findByCompanyName(String companyName);

    // 根据审批状态查找申请列表
    List<GameApplication> findByApprovalStatus(String approvalStatus);

    // 根据游戏名和企业名查找申请信息
    Optional<GameApplication> findByGameNameAndCompanyName(String gameName, String companyName);

    // 检查特定游戏和企业的申请是否存在
    boolean existsByGameNameAndCompanyName(String gameName, String companyName);
}
