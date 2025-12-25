package com.database.gametrade.service;

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
    public boolean updateVendorPersonalInfo(String account, Object personalInfo) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return false;
        }

        Optional<VendorInfo> vendorInfoOptional = vendorInfoRepository.findByAccount(account);
        if (vendorInfoOptional.isPresent()) {
            VendorInfo vendorInfo = vendorInfoOptional.get();
            
            // 将Object转换为VendorInfoDTO
            if (personalInfo instanceof java.util.Map) {
                // 兼容旧版本Map格式
                java.util.Map<String, Object> personalInfoMap = (Map<String, Object>) personalInfo;
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
            logUtil.logError("游戏创建存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
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
            logUtil.logError("游戏信息查询存储过程执行异常 - 游戏名: " + gameName, e);
            return -99;
        }
    }

    /**
     * 模糊查询游戏信息（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryGameInfoFuzzy(String keyword) {
        try {
            // 调用存储过程 sp_query_game_info_fuzzy
            Query query = entityManager.createNativeQuery("{call sp_query_game_info_fuzzy(?)}");
            
            // 设置存储过程参数
            query.setParameter(1, keyword);
            
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
            logUtil.logError("模糊查询游戏信息存储过程执行异常 - 关键词: " + keyword, e);
            return null;
        }
    }

    /**
     * 修改游戏信息（调用存储过程）
     */
    @Transactional
    public int updateGame(String account, String gameName, String category, BigDecimal price,
                         String description, String downloadLink, String licenseNumber) {
        try {
            // 调用存储过程 sp_update_game_info
            Query query = entityManager.createNativeQuery("{call sp_update_game_info(?, ?, ?, ?, ?, ?, ?)}");
            
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

    /**
     * 游戏销售数据查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryGameSales(String account) {
        try {
            // 调用存储过程 sp_query_game_sales_data
            Query query = entityManager.createNativeQuery("{call sp_query_game_sales_data(?)}");
            
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
            logUtil.logError("游戏销售数据查询存储过程执行异常 - 账号: " + account, e);
            return -99;
        }
    }

    /**
     * 厂商游戏评价查询（调用存储过程）
     */
    @Transactional(readOnly = true)
    public Object queryGameReviews(String account, String gameName) {
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
                return java.util.Collections.emptyList();
            }
            
            // 返回查询结果
            return resultList;
        } catch (Exception e) {
            // 捕获存储过程执行异常
            logUtil.logError("厂商游戏评价查询存储过程执行异常 - 账号: " + account + ", 游戏名: " + gameName, e);
            return -99;
        }
    }
}
