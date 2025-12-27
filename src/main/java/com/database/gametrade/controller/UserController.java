package com.database.gametrade.controller;

import com.database.gametrade.dto.LoginRequestDTO;
import com.database.gametrade.dto.UserResponseDTO;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.entity.VendorInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.VendorInfoRepository;
import com.database.gametrade.service.UserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private BuyerInfoRepository buyerInfoRepository;

    @Autowired
    private VendorInfoRepository vendorInfoRepository;

    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        UserInfo user = userService.login(loginRequest.getAccount(), loginRequest.getPassword());
        if (user != null) {
            // 登录成功，返回用户信息（不包含密码）
            UserResponseDTO userResponse = new UserResponseDTO(
                    user.getAccount(),
                    user.getRole(),
                    user.getContact(),
                    user.getRegisterTime()
            );

            // 根据用户角色记录不同的登录成功日志
            if ("buyer".equals(user.getRole())) {
                // 查询买家信息获取昵称
                Optional<BuyerInfo> buyerInfo = buyerInfoRepository.findByAccount(user.getAccount());
                String nickname = buyerInfo.map(BuyerInfo::getNickname).orElse("未知昵称");
                logUtil.logBuyerLoginSuccess(user.getAccount(), nickname);
            } else if ("vendor".equals(user.getRole())) {
                // 查询厂商信息获取企业名
                Optional<VendorInfo> vendorInfo = vendorInfoRepository.findByAccount(user.getAccount());
                String companyName = vendorInfo.map(VendorInfo::getCompanyName).orElse("未知企业名");
                logUtil.logVendorLoginSuccess(user.getAccount(), companyName);
            } else {
                logUtil.logWarning("未知用户角色登录成功 - 账号: " + user.getAccount() + ", 角色: " + user.getRole());
            }

            return ResponseEntity.ok(userResponse);
        } else {
            logUtil.logBuyerLoginFailure(loginRequest.getAccount(), "账号或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("账号或密码错误");
        }
    }

    /**
     * 检查账号是否存在
     * GET /api/users/check-account
     */
    @GetMapping("/check-account")
    public ResponseEntity<Boolean> checkAccountExists(@RequestParam String account) {
        try {
            // URL解码account参数，处理特殊字符
            String decodedAccount = java.net.URLDecoder.decode(account, "UTF-8");
            boolean exists = userService.checkAccountExists(decodedAccount);
            return ResponseEntity.ok(exists);
        } catch (java.io.UnsupportedEncodingException e) {
            // 如果解码失败，使用原始参数
            boolean exists = userService.checkAccountExists(account);
            return ResponseEntity.ok(exists);
        }
    }

    /**
     * 检查联系方式是否存在
     * GET /api/users/check-contact
     */
    @GetMapping("/check-contact")
    public ResponseEntity<Boolean> checkContactExists(@RequestParam String contact) {
        try {
            // URL解码contact参数，处理特殊字符
            String decodedContact = java.net.URLDecoder.decode(contact, "UTF-8");
            boolean exists = userService.checkContactExists(decodedContact);
            return ResponseEntity.ok(exists);
        } catch (java.io.UnsupportedEncodingException e) {
            // 如果解码失败，使用原始参数
            boolean exists = userService.checkContactExists(contact);
            return ResponseEntity.ok(exists);
        }
    }
}
