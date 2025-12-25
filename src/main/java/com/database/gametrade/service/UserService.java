package com.database.gametrade.service;

import com.database.gametrade.dto.BuyerInfoDTO;
import com.database.gametrade.dto.VendorInfoDTO;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.GameInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.entity.VendorInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.UserInfoRepository;
import com.database.gametrade.repository.VendorInfoRepository;
import com.database.gametrade.util.LogUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BuyerInfoRepository buyerInfoRepository;

    @Autowired
    private VendorInfoRepository vendorInfoRepository;

    @Autowired
    private LogUtil logUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
     * 厂商注册
     */
    @Transactional
    public boolean registerVendor(String account, String password, String contact,
                                  String companyName, String registeredAddress, String contactPerson) {
        // 检查账号是否已存在
        if (userInfoRepository.existsByAccount(account)) {
            return false;
        }

        // 检查联系方式是否已存在
        if (userInfoRepository.existsByContact(contact)) {
            return false;
        }

        // 检查企业名是否已存在
        if (vendorInfoRepository.existsByCompanyName(companyName)) {
            return false;
        }

        // 创建用户信息
        UserInfo userInfo = new UserInfo(account, "vendor", passwordEncoder.encode(password), contact);
        userInfoRepository.save(userInfo);

        // 创建厂商信息
        VendorInfo vendorInfo = new VendorInfo(companyName, account, registeredAddress, contactPerson);
        vendorInfoRepository.save(vendorInfo);

        return true;
    }

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

    /**
     * 检查昵称是否存在
     */
    public boolean checkNicknameExists(String nickname) {
        return buyerInfoRepository.existsByNickname(nickname);
    }

    /**
     * 检查企业名是否存在
     */
    public boolean checkCompanyNameExists(String companyName) {
        return vendorInfoRepository.existsByCompanyName(companyName);
    }

    // 简单的登录状态跟踪（生产环境应使用Redis或数据库）
    private final Map<String, String> loggedInUsers = new HashMap<>();

    /**
     * 记录用户登录状态
     */
    public void recordLogin(String account, String role) {
        loggedInUsers.put(account, role);
    }

    /**
     * 用户登出
     */
    public boolean logout(String account) {
        if (loggedInUsers.containsKey(account)) {
            loggedInUsers.remove(account);
            return true;
        }
        return false;
    }

    /**
     * 检查用户是否已登录
     */
    public boolean isUserLoggedIn(String account) {
        return loggedInUsers.containsKey(account);
    }

    /**
     * 获取用户角色
     */
    public String getUserRole(String account) {
        return loggedInUsers.get(account);
    }

    /**
     * 根据用户角色查询个人信息
     */
    public Object getUserPersonalInfo(String account) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return null;
        }

        UserInfo user = userOptional.get();
        String role = user.getRole();
        String contact = user.getContact();

        // 根据角色查询对应的个人信息并转换为DTO
        if ("buyer".equals(role)) {
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
        } else if ("vendor".equals(role)) {
            Optional<VendorInfo> vendorInfo = vendorInfoRepository.findByAccount(account);
            if (vendorInfo.isPresent()) {
                VendorInfo vendor = vendorInfo.get();
                return new VendorInfoDTO(
                        vendor.getCompanyName(),
                        vendor.getAccount(),
                        vendor.getRegisteredAddress(),
                        vendor.getContactPerson(),
                        contact
                );
            }
        }

        return null;
    }

    /**
     * 修改用户个人信息
     */
    @Transactional
    public boolean updatePersonalInfo(String account, Object personalInfo) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return false;
        }

        UserInfo user = userOptional.get();
        String role = user.getRole();

        // 根据角色修改对应的个人信息
        if ("buyer".equals(role)) {
            Optional<BuyerInfo> buyerInfoOptional = buyerInfoRepository.findByAccount(account);
            if (buyerInfoOptional.isPresent()) {
                BuyerInfo buyerInfo = buyerInfoOptional.get();
                
                // 将Object转换为BuyerInfoDTO
                if (personalInfo instanceof Map) {
                    // 兼容旧版本Map格式
                    Map<String, Object> personalInfoMap = (Map<String, Object>) personalInfo;
                    // 注意：nickname是主键，不允许修改，所以跳过nickname字段
                    if (personalInfoMap.containsKey("gender")) {
                        buyerInfo.setGender((String) personalInfoMap.get("gender"));
                    }
                    if (personalInfoMap.containsKey("birthday")) {
                        Object birthdayObj = personalInfoMap.get("birthday");
                        if (birthdayObj instanceof String) {
                            // 处理字符串格式的日期
                            try {
                                buyerInfo.setBirthdate(java.time.LocalDate.parse((String) birthdayObj));
                            } catch (Exception e) {
                                // 日期格式错误，使用默认值
                                buyerInfo.setBirthdate(java.time.LocalDate.of(2000, 1, 1));
                            }
                        } else if (birthdayObj instanceof java.time.LocalDate) {
                            buyerInfo.setBirthdate((java.time.LocalDate) birthdayObj);
                        }
                    }
                } else if (personalInfo instanceof BuyerInfoDTO buyerInfoDTO) {
                    // 使用DTO格式
                    // 注意：nickname是主键，不允许修改，所以跳过nickname字段
                    if (buyerInfoDTO.getGender() != null) {
                        buyerInfo.setGender(buyerInfoDTO.getGender());
                    }
                    if (buyerInfoDTO.getBirthday() != null) {
                        buyerInfo.setBirthdate(buyerInfoDTO.getBirthday());
                    }
                }

                buyerInfoRepository.save(buyerInfo);
                return true;
            }
        } else if ("vendor".equals(role)) {
            Optional<VendorInfo> vendorInfoOptional = vendorInfoRepository.findByAccount(account);
            if (vendorInfoOptional.isPresent()) {
                VendorInfo vendorInfo = vendorInfoOptional.get();
                
                // 将Object转换为VendorInfoDTO
                if (personalInfo instanceof Map) {
                    // 兼容旧版本Map格式
                    Map<String, Object> personalInfoMap = (Map<String, Object>) personalInfo;
                    if (personalInfoMap.containsKey("companyName")) {
                        vendorInfo.setCompanyName((String) personalInfoMap.get("companyName"));
                    }
                    if (personalInfoMap.containsKey("registeredAddress")) {
                        vendorInfo.setRegisteredAddress((String) personalInfoMap.get("registeredAddress"));
                    }
                    if (personalInfoMap.containsKey("contactPerson")) {
                        vendorInfo.setContactPerson((String) personalInfoMap.get("contactPerson"));
                    }
                } else if (personalInfo instanceof VendorInfoDTO vendorInfoDTO) {
                    // 使用DTO格式
                    if (vendorInfoDTO.getCompanyName() != null) {
                        vendorInfo.setCompanyName(vendorInfoDTO.getCompanyName());
                    }
                    if (vendorInfoDTO.getRegisteredAddress() != null) {
                        vendorInfo.setRegisteredAddress(vendorInfoDTO.getRegisteredAddress());
                    }
                    if (vendorInfoDTO.getContactPerson() != null) {
                        vendorInfo.setContactPerson(vendorInfoDTO.getContactPerson());
                    }
                }

                vendorInfoRepository.save(vendorInfo);
                return true;
            }
        }

        return false;
    }

    /**
     * 创建游戏（调用存储过程）
     */
    @Transactional
    public int createGame(String account, String gameName, String category, BigDecimal price,
                         String description, String downloadLink, String licenseNumber) {
        try {
            // 调用存储过程 sp_create_game
            Query query = entityManager.createNativeQuery("{call sp_create_game(?, ?, ?, ?, ?, ?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            query.setParameter(3, category);
            query.setParameter(4, price);
            query.setParameter(5, description);
            query.setParameter(6, downloadLink);
            query.setParameter(7, licenseNumber);
            
            // 执行存储过程并获取返回值
            Object result = query.getSingleResult();
            
            if (result instanceof Integer) {
                int returnValue = (Integer) result;
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏创建存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else if (result instanceof Number) {
                int returnValue = ((Number) result).intValue();
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏创建存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else {
                // 如果返回值不是数字类型，返回错误代码
                return -99;
            }
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 厂商拥有游戏查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryVendorGames(String account) {
        try {
            // 调用存储过程 sp_query_vendor_games
            Query query = entityManager.createNativeQuery("{call sp_query_vendor_games(?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return java.util.Collections.emptyList();
            }
            
            // 返回查询结果
            return resultList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("厂商游戏查询存储过程执行异常 - 账号: " + account, e);
            return -99;
        }
    }

    /**
     * 游戏具体信息查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryGameInfo(String gameName) {
        try {
            // 调用存储过程 sp_query_game_info
            Query query = entityManager.createNativeQuery("{call sp_query_game_info(?)}");
            
            // 设置存储过程参数
            query.setParameter(1, gameName);
            
            // 执行存储过程并获取结果
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 游戏不存在
                return -1;
            }
            
            // 返回查询结果（第一条记录）
            return resultList.get(0);
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏信息查询存储过程执行异常 - 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 游戏信息模糊查询
     */
    @Transactional(readOnly = true)
    public Object queryGameInfoFuzzy(String keyword) {
        try {
            // 使用JPA查询实现模糊查询
            String jpql = "SELECT g FROM GameInfo g WHERE g.gameName LIKE :keyword ORDER BY g.gameName";
            List<GameInfo> resultList = entityManager.createQuery(jpql, GameInfo.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
            
            // 转换为Map列表返回
            List<Map<String, Object>> result = new ArrayList<>();
            for (GameInfo game : resultList) {
                Map<String, Object> gameMap = new HashMap<>();
                gameMap.put("游戏名", game.getGameName());
                gameMap.put("游戏类别", game.getCategory());
                gameMap.put("价格", game.getPrice());
                gameMap.put("企业名", game.getCompanyName());
                gameMap.put("上线时间", game.getReleaseTime());
                gameMap.put("游戏简介", game.getDescription());
                gameMap.put("状态", game.getStatus());
                gameMap.put("下载链接", game.getDownloadLink());
                gameMap.put("版号", game.getLicenseNumber());
                result.add(gameMap);
            }
            
            return result;
        } catch (Exception e) {
            // 捕获查询异常
            logUtil.logError("游戏信息模糊查询异常 - 关键词: " + keyword, e);
            return null;
        }
    }

    /**
     * 游戏信息修改（调用存储过程）
     */
    @Transactional
    public int updateGameInfo(String account, String gameName, BigDecimal price,
                             String description, String licenseNumber, String downloadLink) {
        try {
            // 调用存储过程 sp_update_game_info
            Query query = entityManager.createNativeQuery("{call sp_update_game_info(?, ?, ?, ?, ?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            query.setParameter(3, price);
            query.setParameter(4, description);
            query.setParameter(5, licenseNumber);
            query.setParameter(6, downloadLink);
            
            // 执行存储过程并获取返回值
            Object result = query.getSingleResult();
            
            if (result instanceof Integer) {
                int returnValue = (Integer) result;
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏信息修改存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else if (result instanceof Number) {
                int returnValue = ((Number) result).intValue();
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏信息修改存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else {
                // 如果返回值不是数字类型，返回错误代码
                return -99;
            }
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏信息修改存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 创建游戏上架申请（调用存储过程）
     */
    @Transactional
    public int createGameApplication(String account, String gameName) {
        try {
            // 调用存储过程 sp_create_game_application
            Query query = entityManager.createNativeQuery("{call sp_create_game_application(?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            
            // 执行存储过程并获取返回值
            Object result = query.getSingleResult();
            
            if (result instanceof Integer) {
                int returnValue = (Integer) result;
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏上架申请存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else if (result instanceof Number) {
                int returnValue = ((Number) result).intValue();
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏上架申请存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else {
                // 如果返回值不是数字类型，返回错误代码
                return -99;
            }
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏上架申请存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 游戏下架（调用存储过程）
     */
    @Transactional
    public int offShelfGame(String account, String gameName) {
        try {
            // 调用存储过程 sp_off_shelf_game
            Query query = entityManager.createNativeQuery("{call sp_off_shelf_game(?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            
            // 执行存储过程并获取返回值
            Object result = query.getSingleResult();
            
            if (result instanceof Integer) {
                int returnValue = (Integer) result;
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏下架存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else if (result instanceof Number) {
                int returnValue = ((Number) result).intValue();
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("游戏下架存储过程执行成功 - 账号: " + account + ", 游戏名: " + gameName);
                }
                return returnValue;
            } else {
                // 如果返回值不是数字类型，返回错误代码
                return -99;
            }
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏下架存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 查询游戏上架申请（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryGameApplications(String account, String approvalStatus) {
        try {
            // 调用存储过程 sp_query_applications_by_status
            Query query = entityManager.createNativeQuery("{call sp_query_applications_by_status(?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, approvalStatus);
            
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return java.util.Collections.emptyList();
            }
            
            // 返回查询结果
            return resultList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏上架申请查询存储过程执行异常 - 账号: " + account, e);
            return -99;
        }
    }

    /**
     * 取消游戏上架申请（调用存储过程）
     */
    @Transactional
    public int cancelGameApplication(String account, Integer applicationId) {
        try {
            // 调用存储过程 sp_cancel_game_application
            Query query = entityManager.createNativeQuery("{call sp_cancel_game_application(?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, applicationId);
            
            // 执行存储过程并获取返回值
            Object result = query.getSingleResult();
            
            if (result instanceof Integer) {
                int returnValue = (Integer) result;
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("取消游戏上架申请存储过程执行成功 - 账号: " + account + ", 申请编号: " + applicationId);
                }
                return returnValue;
            } else if (result instanceof Number) {
                int returnValue = ((Number) result).intValue();
                if (returnValue == 0) {
                    // 存储过程执行成功
                    logUtil.logDebug("取消游戏上架申请存储过程执行成功 - 账号: " + account + ", 申请编号: " + applicationId);
                }
                return returnValue;
            } else {
                // 如果返回值不是数字类型，返回错误代码
                return -99;
            }
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("取消游戏上架申请存储过程执行异常 - 账号: " + account + ", 申请编号: " + applicationId, e);
            return -99;
        }
    }

}
