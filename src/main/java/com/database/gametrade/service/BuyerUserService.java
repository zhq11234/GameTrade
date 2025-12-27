package com.database.gametrade.service;

import com.database.gametrade.dto.*;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.UserInfoRepository;
import com.database.gametrade.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BuyerUserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BuyerInfoRepository buyerInfoRepository;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 按游戏名查询游戏
     * 如果gameName为空，则返回所有游戏（使用同一个存储过程）
     */
    public List<GameSearchResponseDTO> searchGameByName(String gameName) {
        String sql = "EXEC sp_SearchGameByName ?";
        // 如果gameName为空，传入空字符串或null，让存储过程处理
        String searchName = (gameName == null || gameName.trim().isEmpty()) ? "" : gameName;
        return jdbcTemplate.query(sql, new GameSearchRowMapper(), searchName);
    }

    /**
     * 按游戏分类查询游戏
     */
    public List<GameSearchResponseDTO> searchGameByCategory(String category) {
        String sql = "EXEC sp_SearchGameByCategory ?";
        return jdbcTemplate.query(sql, new GameSearchRowMapper(), category);
    }

    /**
     * 按游戏热度查询游戏
     */
    public List<GameSearchResponseDTO> searchGameByPopularity(BigDecimal minPopularity) {
        String sql = "EXEC sp_SearchGameByPopularity ?";
        return jdbcTemplate.query(sql, new GameSearchRowMapper(), minPopularity);
    }

    /**
     * 按买家偏好查询游戏
     */
    public List<GameSearchResponseDTO> searchGameByBuyerPreference(String buyerNickname) {
        String sql = "EXEC sp_SearchGameByBuyerPreference ?";
        return jdbcTemplate.query(sql, new GameSearchRowMapper(), buyerNickname);
    }

    /**
     * 游戏详细信息查询
     */
    public GameInfoDTO getGameDetails(String gameName) {
        String sql = "EXEC sp_GetGameDetails ?";
        try {
            List<GameInfoDTO> results = jdbcTemplate.query(sql, new GameInfoRowMapper(), gameName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logUtil.logWarning("查询游戏详细信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 游戏评价查询
     */
    public List<GameReviewDTO> getGameReviews(String gameName) {
        String sql = "EXEC sp_GetGameReviews ?";
        return jdbcTemplate.query(sql, new GameReviewRowMapper(), gameName);
    }

    /**
     * 进行游戏评价
     */
    @Transactional
    public boolean submitGameReview(String buyerNickname, String gameName, BigDecimal score, String comment) {
        String sql = "EXEC sp_SubmitGameReview ?, ?, ?, ?";
        try {
            jdbcTemplate.update(sql, buyerNickname, gameName, score, comment);
            return true;
        } catch (Exception e) {
            logUtil.logWarning("提交游戏评价失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 游戏库查询
     */
    public List<GameLibraryResponseDTO> getBuyerGameLibrary(String buyerNickname) {
        String sql = "EXEC sp_GetBuyerGameLibrary ?";
        return jdbcTemplate.query(sql, new GameLibraryRowMapper(), buyerNickname);
    }

    /**
     * 游戏下载
     */
    public GameDownloadResponseDTO getGameDownloadLink(String buyerNickname, String gameName) {
        String sql = "EXEC sp_GetGameDownloadLink ?, ?";
        try {
            List<GameDownloadResponseDTO> results = jdbcTemplate.query(sql, new GameDownloadRowMapper(), buyerNickname, gameName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logUtil.logWarning("获取游戏下载链接失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 生成订单
     */
    @Transactional
    public OrderResponseDTO createOrder(String buyerNickname, String gameName) {
        String sql = "EXEC sp_CreateOrder ?, ?";
        try {
            List<OrderResponseDTO> results = jdbcTemplate.query(sql, new OrderResponseRowMapper(), buyerNickname, gameName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logUtil.logWarning("生成订单失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 查询订单
     */
    public List<OrderResponseDTO> getBuyerOrders(String buyerNickname) {
        String sql = "EXEC sp_GetBuyerOrders ?";
        return jdbcTemplate.query(sql, new OrderResponseRowMapper(), buyerNickname);
    }

    /**
     * 支付订单
     */
    @Transactional
    public boolean payOrder(String orderId) {
        String sql = "EXEC sp_PayOrder ?";
        try {
            jdbcTemplate.update(sql, orderId);
            return true;
        } catch (Exception e) {
            logUtil.logWarning("支付订单失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 取消订单
     */
    @Transactional
    public boolean cancelOrder(String orderId) {
        String sql = "EXEC sp_CancelOrder ?";
        try {
            jdbcTemplate.update(sql, orderId);
            return true;
        } catch (Exception e) {
            logUtil.logWarning("取消订单失败: " + e.getMessage());
            return false;
        }
    }

    // RowMapper类
    private static class GameSearchRowMapper implements RowMapper<GameSearchResponseDTO> {
        @Override
        public GameSearchResponseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new GameSearchResponseDTO(
                rs.getString("GameName"),
                rs.getString("Category"),
                rs.getBigDecimal("Price"),
                rs.getBigDecimal("Score"),
                rs.getInt("SalesVolume"),
                rs.getString("CompanyName"),
                rs.getString("Description")
            );
        }
    }

    private static class GameInfoRowMapper implements RowMapper<GameInfoDTO> {
        @Override
        public GameInfoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new GameInfoDTO(
                rs.getString("GameName"),
                rs.getString("Category"),
                rs.getBigDecimal("Price"),
                rs.getString("CompanyName"),
                rs.getTimestamp("ReleaseTime") != null ? rs.getTimestamp("ReleaseTime").toLocalDateTime() : null,
                rs.getString("Description"),
                rs.getString("Status"),
                rs.getString("DownloadLink"),
                rs.getString("LicenseNumber")
            );
        }
    }

    private static class GameReviewRowMapper implements RowMapper<GameReviewDTO> {
        @Override
        public GameReviewDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new GameReviewDTO(
                rs.getString("BuyerNickname"),
                rs.getInt("Score"),
                rs.getString("Comment"),
                rs.getTimestamp("ReviewTime") != null ? rs.getTimestamp("ReviewTime").toLocalDateTime() : null
            );
        }
    }

    private static class GameLibraryRowMapper implements RowMapper<GameLibraryResponseDTO> {
        @Override
        public GameLibraryResponseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new GameLibraryResponseDTO(
                rs.getString("GameName"),
                rs.getString("LicenseNumber")
            );
        }
    }

    private static class GameDownloadRowMapper implements RowMapper<GameDownloadResponseDTO> {
        @Override
        public GameDownloadResponseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new GameDownloadResponseDTO(
                rs.getString("DownloadLink"),
                rs.getString("GameName"),
                rs.getString("LicenseNumber"),
                rs.getString("Description")
            );
        }
    }

    private static class OrderResponseRowMapper implements RowMapper<OrderResponseDTO> {
        @Override
        public OrderResponseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new OrderResponseDTO(
                rs.getString("OrderId"),
                rs.getString("BuyerNickname"),
                rs.getString("GameName"),
                rs.getString("Category"),
                rs.getBigDecimal("Price"),
                rs.getTimestamp("OrderTime") != null ? rs.getTimestamp("OrderTime").toLocalDateTime() : null,
                rs.getTimestamp("PaymentTime") != null ? rs.getTimestamp("PaymentTime").toLocalDateTime() : null,
                rs.getString("OrderStatus")
            );
        }
    }

    /**
     * 买家注册
     */
    @Transactional
    public boolean registerBuyer(String account, String password, String contact, String nickname) {
        // 检查账号是否已存在
        if (userInfoRepository.existsByAccount(account)) {
            return false;
        }

        // 检查联系方式是否已存在
        if (userInfoRepository.existsByContact(contact)) {
            return false;
        }

        // 检查昵称是否已存在
        if (buyerInfoRepository.existsByNickname(nickname)) {
            return false;
        }

        // 创建用户信息
        UserInfo userInfo = new UserInfo(account, "buyer", passwordEncoder.encode(password), contact);
        userInfoRepository.save(userInfo);

        // 创建买家信息
        BuyerInfo buyerInfo = new BuyerInfo(nickname, account);
        buyerInfoRepository.save(buyerInfo);

        return true;
    }

    /**
     * 检查昵称是否存在
     */
    public boolean checkNicknameExists(String nickname) {
        return buyerInfoRepository.existsByNickname(nickname);
    }

    /**
     * 查询买家个人信息
     */
    public Object getBuyerPersonalInfo(String account) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return null;
        }

        UserInfo user = userOptional.get();
        String contact = user.getContact();

        // 查询买家信息并转换为DTO
        Optional<BuyerInfo> buyerInfo = buyerInfoRepository.findByAccount(account);
        if (buyerInfo.isPresent()) {
            BuyerInfo buyer = buyerInfo.get();
            return new BuyerInfoDTO(
                    buyer.getNickname(),
                    buyer.getAccount(),
                    buyer.getGender(),
                    buyer.getBirthdate(),
                    contact
            );
        }

        return null;
    }

    /**
     * 修改买家个人信息
     * 支持修改性别和出生日期，昵称是主键不允许修改
     * @return 0: 成功, 1: 账号不存在, 2: 买家信息不存在, 3: 昵称是主键不允许修改
     */
    @Transactional
    public int updateBuyerPersonalInfo(String account, Object personalInfo) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            logUtil.logWarning("修改买家个人信息失败 - 账号不存在: " + account);
            return 1;
            // 账号不存在
        }

        Optional<BuyerInfo> buyerInfoOptional = buyerInfoRepository.findByAccount(account);
        if (buyerInfoOptional.isPresent()) {
            BuyerInfo buyerInfo = buyerInfoOptional.get();
            
            boolean hasUpdates = false;
            
            // 将Object转换为BuyerInfoDTO
            if (personalInfo instanceof java.util.Map) {
                // 兼容旧版本Map格式
                java.util.Map<String, Object> personalInfoMap = (Map<String, Object>) personalInfo;
                
                // 检查是否尝试修改昵称
                if (personalInfoMap.containsKey("nickname")) {
                    String newNickname = (String) personalInfoMap.get("nickname");
                    if (!newNickname.equals(buyerInfo.getNickname())) {
                        logUtil.logWarning("修改买家个人信息失败 - 昵称是主键不允许修改: " + account);
                        return 3;
                        // 昵称是主键不允许修改
                    }
                }
                
                // 修改性别
                if (personalInfoMap.containsKey("gender")) {
                    String newGender = (String) personalInfoMap.get("gender");
                    if (!newGender.equals(buyerInfo.getGender())) {
                        buyerInfo.setGender(newGender);
                        hasUpdates = true;
                    }
                }
                
                // 修改出生日期
                if (personalInfoMap.containsKey("birthday")) {
                    Object birthdayObj = personalInfoMap.get("birthday");
                    java.time.LocalDate newBirthdate = null;
                    
                    if (birthdayObj instanceof String) {
                        // 处理字符串格式的日期
                        try {
                            newBirthdate = java.time.LocalDate.parse((String) birthdayObj);
                        } catch (Exception e) {
                            logUtil.logWarning("日期格式错误，使用默认值: " + birthdayObj);
                            newBirthdate = java.time.LocalDate.of(2000, 1, 1);
                        }
                    } else if (birthdayObj instanceof java.time.LocalDate) {
                        newBirthdate = (java.time.LocalDate) birthdayObj;
                    }
                    
                    if (newBirthdate != null && !newBirthdate.equals(buyerInfo.getBirthdate())) {
                        buyerInfo.setBirthdate(newBirthdate);
                        hasUpdates = true;
                    }
                }
            } else if (personalInfo instanceof BuyerInfoDTO buyerInfoDTO) {
                // 使用DTO格式
                
                // 检查是否尝试修改昵称
                if (buyerInfoDTO.getNickname() != null && 
                    !buyerInfoDTO.getNickname().equals(buyerInfo.getNickname())) {
                    logUtil.logWarning("修改买家个人信息失败 - 昵称是主键不允许修改: " + account);
                    return 3; // 昵称是主键不允许修改
                }
                
                // 修改性别
                if (buyerInfoDTO.getGender() != null && 
                    !buyerInfoDTO.getGender().equals(buyerInfo.getGender())) {
                    buyerInfo.setGender(buyerInfoDTO.getGender());
                    hasUpdates = true;
                }
                
                // 修改出生日期
                if (buyerInfoDTO.getBirthday() != null && 
                    !buyerInfoDTO.getBirthday().equals(buyerInfo.getBirthdate())) {
                    buyerInfo.setBirthdate(buyerInfoDTO.getBirthday());
                    hasUpdates = true;
                }
            }

            // 如果有更新，则保存
            if (hasUpdates) {
                buyerInfoRepository.save(buyerInfo);
                logUtil.logDebug("修改买家个人信息成功 - 账号: " + account);
                return 0; // 成功
            } else {
                logUtil.logDebug("修改买家个人信息 - 没有需要更新的字段 - 账号: " + account);
                return 0; // 没有需要更新的字段也返回成功
            }
        }

        logUtil.logWarning("修改买家个人信息失败 - 买家信息不存在: " + account);
        return 2; // 买家信息不存在
    }
}
