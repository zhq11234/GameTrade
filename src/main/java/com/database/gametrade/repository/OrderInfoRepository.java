package com.database.gametrade.repository;

import com.database.gametrade.entity.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, String> {

    // 根据订单ID查找订单信息
    Optional<OrderInfo> findByOrderId(String orderId);

    // 根据昵称查找订单列表
    List<OrderInfo> findByNickname(String nickname);

    // 根据游戏名查找订单列表
    List<OrderInfo> findByGameName(String gameName);

    // 根据订单状态查找订单列表
    List<OrderInfo> findByOrderStatus(String orderStatus);

    // 根据昵称和订单状态查找订单列表
    List<OrderInfo> findByNicknameAndOrderStatus(String nickname, String orderStatus);

    // 检查订单ID是否存在
    boolean existsByOrderId(String orderId);

    // 根据下单时间降序排列查找订单
    List<OrderInfo> findByOrderByOrderTimeDesc();

    // 根据昵称查找订单（按下单时间降序排列）
    List<OrderInfo> findByNicknameOrderByOrderTimeDesc(String nickname);
}
