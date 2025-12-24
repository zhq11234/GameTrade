package com.database.gametrade.service;

import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.entity.VendorInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.UserInfoRepository;
import com.database.gametrade.repository.VendorInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

        // 根据角色查询对应的个人信息
        if ("buyer".equals(role)) {
            Optional<BuyerInfo> buyerInfo = buyerInfoRepository.findByAccount(account);
            if (buyerInfo.isPresent()) {
                return buyerInfo.get();
            }
        } else if ("vendor".equals(role)) {
            Optional<VendorInfo> vendorInfo = vendorInfoRepository.findByAccount(account);
            if (vendorInfo.isPresent()) {
                return vendorInfo.get();
            }
        }

        return null;
    }

    /**
     * 修改用户个人信息
     */
    @Transactional
    public boolean updatePersonalInfo(String account, Map<String, Object> personalInfo) {
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

                // 更新买家信息
                if (personalInfo.containsKey("nickname")) {
                    buyerInfo.setNickname((String) personalInfo.get("nickname"));
                }
                if (personalInfo.containsKey("gender")) {
                    buyerInfo.setGender((String) personalInfo.get("gender"));
                }
                if (personalInfo.containsKey("birthdate")) {
                    buyerInfo.setBirthdate((java.time.LocalDate) personalInfo.get("birthdate"));
                }

                buyerInfoRepository.save(buyerInfo);
                return true;
            }
        } else if ("vendor".equals(role)) {
            Optional<VendorInfo> vendorInfoOptional = vendorInfoRepository.findByAccount(account);
            if (vendorInfoOptional.isPresent()) {
                VendorInfo vendorInfo = vendorInfoOptional.get();

                // 更新厂商信息
                if (personalInfo.containsKey("companyName")) {
                    vendorInfo.setCompanyName((String) personalInfo.get("companyName"));
                }
                if (personalInfo.containsKey("registeredAddress")) {
                    vendorInfo.setRegisteredAddress((String) personalInfo.get("registeredAddress"));
                }
                if (personalInfo.containsKey("contactPerson")) {
                    vendorInfo.setContactPerson((String) personalInfo.get("contactPerson"));
                }

                vendorInfoRepository.save(vendorInfo);
                return true;
            }
        }

        return false;
    }

}
