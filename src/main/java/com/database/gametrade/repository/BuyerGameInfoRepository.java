package com.database.gametrade.repository;

import com.database.gametrade.entity.BuyerGameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerGameInfoRepository extends JpaRepository<BuyerGameInfo, BuyerGameInfo.BuyerGameInfoId> {

    // 根据昵称查找买家游戏信息
    List<BuyerGameInfo> findByNickname(String nickname);

    // 根据游戏名查找买家游戏信息
    List<BuyerGameInfo> findByGameName(String gameName);

    // 根据昵称和游戏名查找买家游戏信息
    Optional<BuyerGameInfo> findByNicknameAndGameName(String nickname, String gameName);

    // 根据版号查找买家游戏信息
    Optional<BuyerGameInfo> findByLicenseNumber(String licenseNumber);

    // 检查特定用户和游戏的买家游戏信息是否存在
    boolean existsByNicknameAndGameName(String nickname, String gameName);

    // 检查版号是否存在
    boolean existsByLicenseNumber(String licenseNumber);

    // 根据游戏名查找评分最高的买家游戏信息（按评分降序排列）
    List<BuyerGameInfo> findByGameNameOrderByScoreDesc(String gameName);

    // 根据昵称查找买家游戏信息（按评价时间降序排列）
    List<BuyerGameInfo> findByNicknameOrderByReviewTimeDesc(String nickname);
}
