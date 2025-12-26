package com.database.gametrade.service;

import com.database.gametrade.dto.GameInfoDTO;
import com.database.gametrade.dto.VendorGameDTO;
import com.database.gametrade.dto.VendorInfoDTO;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.entity.VendorInfo;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VendorUserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private VendorInfoRepository vendorInfoRepository;

    @Autowired
    private LogUtil logUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
     * 检查企业名是否存在
     */
    public boolean checkCompanyNameExists(String companyName) {
        return vendorInfoRepository.existsByCompanyName(companyName);
    }

    /**
     * 查询厂商个人信息
     */
    public Object getVendorPersonalInfo(String account) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return null;
        }

        UserInfo user = userOptional.get();
        String contact = user.getContact();

        // 查询厂商信息并转换为DTO
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

        return null;
    }

    /**
     * 修改厂商个人信息
     */
    @Transactional
    public int updateVendorPersonalInfo(String account, Object personalInfo) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return -1;
            // 账号不存在
        }

        UserInfo userInfo = userOptional.get();
        Optional<VendorInfo> vendorInfoOptional = vendorInfoRepository.findByAccount(account);
        if (vendorInfoOptional.isPresent()) {
            VendorInfo vendorInfo = vendorInfoOptional.get();
            
            // 将Object转换为VendorInfoDTO
            if (personalInfo instanceof java.util.Map) {
                // 兼容旧版本Map格式
                java.util.Map<String, Object> personalInfoMap = (Map<String, Object>) personalInfo;
                
                // 更新联系方式（需要检查唯一性）
                if (personalInfoMap.containsKey("contact")) {
                    String newContact = (String) personalInfoMap.get("contact");
                    // 检查联系方式是否已存在且不属于当前用户
                    if (userInfoRepository.existsByContact(newContact) && !newContact.equals(userInfo.getContact())) {
                        return -2;
                        // 联系方式已存在
                    }
                    userInfo.setContact(newContact);
                }
                
                if (personalInfoMap.containsKey("registeredAddress")) {
                    vendorInfo.setRegisteredAddress((String) personalInfoMap.get("registeredAddress"));
                }
                if (personalInfoMap.containsKey("contactPerson")) {
                    vendorInfo.setContactPerson((String) personalInfoMap.get("contactPerson"));
                }
            } else if (personalInfo instanceof VendorInfoDTO vendorInfoDTO) {
                // 使用DTO格式
                // 更新联系方式（需要检查唯一性）
                if (vendorInfoDTO.getContact() != null) {
                    String newContact = vendorInfoDTO.getContact();
                    // 检查联系方式是否已存在且不属于当前用户
                    if (userInfoRepository.existsByContact(newContact) && !newContact.equals(userInfo.getContact())) {
                        return -2;
                        // 联系方式已存在
                    }
                    userInfo.setContact(newContact);
                }
                
                if (vendorInfoDTO.getRegisteredAddress() != null) {
                    vendorInfo.setRegisteredAddress(vendorInfoDTO.getRegisteredAddress());
                }
                if (vendorInfoDTO.getContactPerson() != null) {
                    vendorInfo.setContactPerson(vendorInfoDTO.getContactPerson());
                }
            }

            // 保存用户信息和厂商信息
            userInfoRepository.save(userInfo);
            vendorInfoRepository.save(vendorInfo);
            return 0;
            // 成功
        }

        return -1;
        // 厂商信息不存在
    }

    /**
     * 创建游戏（调用存储过程）
     */
    @Transactional
    public int createGame(String account, String gameName, String category, BigDecimal price,
                         String description, String downloadLink, String licenseNumber) {
        try {
            // 使用原生SQL调用存储过程并获取返回值
            Query query = entityManager.createNativeQuery("DECLARE @result INT; EXEC @result = sp_create_game ?, ?, ?, ?, ?, ?, ?; SELECT @result");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            query.setParameter(3, category);
            query.setParameter(4, price);
            query.setParameter(5, description);
            query.setParameter(6, downloadLink);
            query.setParameter(7, licenseNumber);
            
            // 执行查询并获取返回值
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
            logUtil.logError("游戏创建存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 厂商拥有游戏查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryVendorGames(String account,String status) {
        try {
            // 调用存储过程 sp_query_vendor_games
            Query query = entityManager.createNativeQuery("{call sp_query_vendor_games(?,?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, status);
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return java.util.Collections.emptyList();
            }
            
            // 将数据库结果转换为DTO对象列表
            List<VendorGameDTO> dtoList = new java.util.ArrayList<>();
            for (Object result : resultList) {
                if (result instanceof Object[] row) {
                    // 根据存储过程返回的字段顺序映射到DTO
                    VendorGameDTO dto = new VendorGameDTO();
                    dto.setGameName(row[0] != null ? row[0].toString() : null);
                    // 游戏名
                    dto.setCategory(row[1] != null ? row[1].toString() : null);
                    // 游戏类别
                    dto.setPrice(row[2] != null ? new BigDecimal(row[2].toString()) : null);
                    // 价格
                    dto.setStatus(row[3] != null ? row[3].toString() : null);
                    // 游戏状态
                    dto.setDescription(row[4] != null ? row[4].toString() : null);
                    // 简介
                    dtoList.add(dto);
                }
            }
            
            // 返回DTO列表
            return dtoList;
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
            
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return java.util.Collections.emptyList();
            }
            
            // 将数据库结果转换为DTO对象列表
            List<GameInfoDTO> dtoList = new java.util.ArrayList<>();
            for (Object result : resultList) {
                if (result instanceof Object[] row) {
                    // 根据存储过程返回的字段顺序映射到DTO
                    GameInfoDTO dto = new GameInfoDTO();
                    dto.setGameName(row[0] != null ? row[0].toString() : null);
                    // 游戏名
                    dto.setCategory(row[1] != null ? row[1].toString() : null);
                    // 游戏类别
                    dto.setPrice(row[2] != null ? new BigDecimal(row[2].toString()) : null);
                    // 价格
                    dto.setCompanyName(row[3] != null ? row[3].toString() : null);
                    // 企业名
                    dto.setReleaseTime(row[4] != null ? java.time.LocalDateTime.parse(row[4].toString()) : null);
                    // 上线时间
                    dto.setDescription(row[5] != null ? row[5].toString() : null);
                    // 游戏简介
                    dto.setStatus(row[6] != null ? row[6].toString() : null);
                    // 状态
                    dto.setDownloadLink(row[7] != null ? row[7].toString() : null);
                    // 下载链接
                    dto.setLicenseNumber(row[8] != null ? row[8].toString() : null);
                    // 版号
                    dtoList.add(dto);
                }
            }
            
            // 返回DTO列表
            return dtoList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏信息查询存储过程执行异常 - 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 模糊查询游戏信息（调用存储过程）
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> queryGameInfoFuzzy(String keyword) {
        try {
            // 调用存储过程 sp_query_game_info_fuzzy
            Query query = entityManager.createNativeQuery("{call sp_query_game_info_fuzzy(?)}");
            
            // 设置存储过程参数
            query.setParameter(1, keyword);
            
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return new java.util.ArrayList<>();
            }
            
            // 将数据库结果转换为Map列表
            List<Map<String, Object>> mapList = new java.util.ArrayList<>();
            for (Object result : resultList) {
                if (result instanceof Object[] row) {
                    Map<String, Object> gameMap = new java.util.HashMap<>();
                    // 根据存储过程返回的字段顺序映射到Map
                    gameMap.put("gameName", row[0] != null ? row[0].toString() : null);
                    gameMap.put("category", row[1] != null ? row[1].toString() : null);
                    gameMap.put("price", row[2] != null ? new BigDecimal(row[2].toString()) : null);
                    gameMap.put("companyName", row[3] != null ? row[3].toString() : null);
                    gameMap.put("releaseTime", row[4] != null ? row[4].toString() : null);
                    gameMap.put("description", row[5] != null ? row[5].toString() : null);
                    gameMap.put("status", row[6] != null ? row[6].toString() : null);
                    gameMap.put("downloadLink", row[7] != null ? row[7].toString() : null);
                    gameMap.put("licenseNumber", row[8] != null ? row[8].toString() : null);
                    mapList.add(gameMap);
                }
            }
            
            // 返回Map列表
            return mapList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("模糊查询游戏信息存储过程执行异常 - 关键词: " + keyword, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 修改游戏信息（调用存储过程）
     */
    @Transactional
    public int updateGame(String account, String gameName, String category, BigDecimal price,
                         String description, String downloadLink, String licenseNumber) {
        try {
            // 使用原生SQL调用存储过程并获取返回值
            Query query = entityManager.createNativeQuery("DECLARE @result INT; EXEC @result = sp_update_game_info ?, ?, ?, ?, ?, ?, ?; SELECT @result");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            query.setParameter(3, category);
            query.setParameter(4, price);
            query.setParameter(5, description);
            query.setParameter(6, downloadLink);
            query.setParameter(7, licenseNumber);
            
            // 执行查询并获取返回值
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
    public List<Map<String, Object>> queryGameApplications(String account, String approvalStatus) {
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
                return new java.util.ArrayList<>();
            }
            
            // 将数据库结果转换为Map列表
            List<Map<String, Object>> mapList = new java.util.ArrayList<>();
            for (Object result : resultList) {
                if (result instanceof Object[] row) {
                    Map<String, Object> applicationMap = new java.util.HashMap<>();
                    // 根据存储过程返回的字段顺序映射到Map
                    // 假设存储过程返回字段：application_id, game_name, application_time, approval_status, remarks
                    applicationMap.put("applicationId", row[0] != null ? row[0].toString() : null);
                    applicationMap.put("gameName", row[1] != null ? row[1].toString() : null);
                    applicationMap.put("applicationTime", row[2] != null ? row[2].toString() : null);
                    applicationMap.put("approvalStatus", row[3] != null ? row[3].toString() : null);
                    applicationMap.put("remarks", row[4] != null ? row[4].toString() : null);
                    mapList.add(applicationMap);
                }
            }
            
            // 返回Map列表
            return mapList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏上架申请查询存储过程执行异常 - 账号: " + account, e);
            return new java.util.ArrayList<>();
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

    /**
     * 游戏销售数据查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> queryGameSales(String account) {
        try {
            // 调用存储过程 sp_query_game_sales_data
            Query query = entityManager.createNativeQuery("{call sp_query_game_sales_data(?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return new java.util.ArrayList<>();
            }
            
            // 将数据库结果转换为Map列表
            List<Map<String, Object>> mapList = new java.util.ArrayList<>();
            for (Object result : resultList) {
                if (result instanceof Object[] row) {
                    Map<String, Object> salesMap = new java.util.HashMap<>();
                    // 根据存储过程返回的字段顺序映射到Map
                    salesMap.put("gameName", row[0] != null ? row[0].toString() : null);
                    salesMap.put("companyName", row[1] != null ? row[1].toString() : null);
                    salesMap.put("price", row[2] != null ? new BigDecimal(row[2].toString()) : null);
                    salesMap.put("salesCount", row[3] != null ? Integer.parseInt(row[3].toString()) : null);
                    salesMap.put("visitorCount", row[4] != null ? Integer.parseInt(row[4].toString()) : null);
                    salesMap.put("salesAmount", row[5] != null ? new BigDecimal(row[5].toString()) : null);
                    salesMap.put("conversionRate", row[6] != null ? new BigDecimal(row[6].toString()) : null);
                    mapList.add(salesMap);
                }
            }
            
            // 返回Map列表
            return mapList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("游戏销售数据查询存储过程执行异常 - 账号: " + account, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 厂商游戏评价查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> queryGameReviews(String account, String gameName) {
        try {
            // 调用存储过程 sp_query_game_reviews
            Query query = entityManager.createNativeQuery("{call sp_query_game_reviews(?, ?)}");
            
            // 设置存储过程参数
            query.setParameter(1, account);
            query.setParameter(2, gameName);
            
            // 执行存储过程并获取结果列表
            List<?> resultList = query.getResultList();
            
            if (resultList.isEmpty()) {
                // 没有查询到结果，返回空列表
                return new java.util.ArrayList<>();
            }
            
            // 将数据库结果转换为Map列表
            List<Map<String, Object>> mapList = new java.util.ArrayList<>();
            for (Object result : resultList) {
                if (result instanceof Object[] row) {
                    Map<String, Object> reviewMap = new java.util.HashMap<>();
                    // 根据存储过程返回的字段顺序映射到Map
                    reviewMap.put("nickname", row[0] != null ? row[0].toString() : null);
                    reviewMap.put("rating", row[1] != null ? Integer.parseInt(row[1].toString()) : null);
                    reviewMap.put("comment", row[2] != null ? row[2].toString() : null);
                    reviewMap.put("reviewTime", row[3] != null ? row[3].toString() : null);
                    mapList.add(reviewMap);
                }
            }
            
            // 返回Map列表
            return mapList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("厂商游戏评价查询存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return new java.util.ArrayList<>();
        }
    }
}
