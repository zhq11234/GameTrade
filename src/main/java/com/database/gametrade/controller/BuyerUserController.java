package com.database.gametrade.controller;

import com.database.gametrade.dto.BuyerRegisterRequestDTO;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.service.BuyerUserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/buyers")
public class BuyerUserController {

    @Autowired
    private BuyerUserService buyerUserService;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private BuyerInfoRepository buyerInfoRepository;

    /**
     * 买家注册
     * POST /api/buyers/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody BuyerRegisterRequestDTO registerRequest) {
        logUtil.logBuyerRegisterRequest(registerRequest.getAccount(), registerRequest.getNickname());

        boolean success = buyerUserService.registerBuyer(
                registerRequest.getAccount(),
                registerRequest.getPassword(),
                registerRequest.getContact(),
                registerRequest.getNickname()
        );

        if (success) {
            logUtil.logBuyerRegisterSuccess(registerRequest.getAccount(), registerRequest.getNickname());
            return ResponseEntity.ok().build();
        } else {
            logUtil.logBuyerRegisterFailure(registerRequest.getAccount(), "账号、联系方式或昵称已存在");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("账号、联系方式或昵称已存在");
        }
    }

    /**
     * 买家登出
     * POST /api/buyers/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutBuyer(@RequestParam String account) {
        // 这里可以添加登出逻辑，比如清除session或token
        Optional<BuyerInfo> buyerInfo = buyerInfoRepository.findByAccount(account);
        String nickname = buyerInfo.map(BuyerInfo::getNickname).orElse("未知昵称");
        logUtil.logBuyerLogout(account, nickname);
        return ResponseEntity.ok().body("买家登出成功");
    }

    /**
     * 检查昵称是否存在
     * GET /api/buyers/check-nickname
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNicknameExists(@RequestParam String nickname) {
        boolean exists = buyerUserService.checkNicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }

    /**
     * 查询买家个人信息
     * GET /api/buyers/personal-info
     */
    @GetMapping("/personal-info")
    public ResponseEntity<?> getPersonalInfo(@RequestParam String account) {
        logUtil.logDebug("查询买家个人信息 - 账号: " + account);

        Object personalInfo = buyerUserService.getBuyerPersonalInfo(account);
        if (personalInfo == null) {
            logUtil.logWarning("查询买家个人信息失败 - 账号不存在: " + account);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("买家不存在");
        }

        logUtil.logDebug("查询买家个人信息成功 - 账号: " + account);
        return ResponseEntity.ok(personalInfo);
    }

    /**
     * 修改买家个人信息
     * PUT /api/buyers/personal-info
     */
    @PutMapping("/personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestParam String account, @RequestBody Object personalInfo) {
        logUtil.logDebug("修改买家个人信息 - 账号: " + account);

        boolean success = buyerUserService.updateBuyerPersonalInfo(account, personalInfo);
        if (success) {
            logUtil.logDebug("修改买家个人信息成功 - 账号: " + account);
            return ResponseEntity.ok().body("买家个人信息修改成功");
        } else {
            logUtil.logWarning("修改买家个人信息失败 - 账号不存在: " + account);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("买家不存在或修改失败");
        }
    }
}
