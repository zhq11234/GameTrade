package com.database.gametrade.service;

import com.database.gametrade.dto.BuyerInfoDTO;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.UserInfoRepository;
import com.database.gametrade.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     */
    @Transactional
    public boolean updateBuyerPersonalInfo(String account, Object personalInfo) {
        // 首先获取用户基本信息
        Optional<UserInfo> userOptional = userInfoRepository.findByAccount(account);
        if (userOptional.isEmpty()) {
            return false;
        }

        Optional<BuyerInfo> buyerInfoOptional = buyerInfoRepository.findByAccount(account);
        if (buyerInfoOptional.isPresent()) {
            BuyerInfo buyerInfo = buyerInfoOptional.get();
            
            // 将Object转换为BuyerInfoDTO
            if (personalInfo instanceof java.util.Map) {
                // 兼容旧版本Map格式
                java.util.Map<String, Object> personalInfoMap = (Map<String, Object>) personalInfo;
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

        return false;
    }
}
