package com.database.gametrade.repository;

import com.database.gametrade.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, BrowseHistory.BrowseHistoryId> {

    // 根据昵称查找浏览记录
    List<BrowseHistory> findByNickname(String nickname);

    // 根据游戏名查找浏览记录
    List<BrowseHistory> findByGameName(String gameName);

    // 根据昵称和游戏名查找浏览记录
    Optional<BrowseHistory> findByNicknameAndGameName(String nickname, String gameName);

    // 检查特定用户和游戏的浏览记录是否存在
    boolean existsByNicknameAndGameName(String nickname, String gameName);

    // 根据昵称查找浏览次数最多的游戏（按浏览次数降序排列）
    List<BrowseHistory> findByNicknameOrderByBrowseCountDesc(String nickname);

    // 根据游戏名查找浏览次数最多的用户（按浏览次数降序排列）
    List<BrowseHistory> findByGameNameOrderByBrowseCountDesc(String gameName);
}
