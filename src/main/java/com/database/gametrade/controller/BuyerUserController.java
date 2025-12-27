package com.database.gametrade.controller;

import com.database.gametrade.dto.*;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.service.BuyerUserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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
    @PostMapping("/personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestParam String account, @RequestBody Object personalInfo) {
        logUtil.logDebug("修改买家个人信息 - 账号: " + account);

        int result = buyerUserService.updateBuyerPersonalInfo(account, personalInfo);

        return switch (result) {
            case 0 -> {
                // 成功
                logUtil.logDebug("修改买家个人信息成功 - 账号: " + account);
                yield ResponseEntity.ok().body("买家个人信息修改成功");
            }
            case 1 -> {
                // 账号不存在
                logUtil.logWarning("修改买家个人信息失败 - 账号不存在: " + account);
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("买家账号不存在");
            }
            case 2 -> {
                // 买家信息不存在
                logUtil.logWarning("修改买家个人信息失败 - 买家信息不存在: " + account);
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("买家信息不存在");
            }
            case 3 -> {
                // 昵称是主键不允许修改
                logUtil.logWarning("修改买家个人信息失败 - 昵称是主键不允许修改: " + account);
                yield ResponseEntity.status(HttpStatus.BAD_REQUEST).body("昵称是主键不允许修改");
            }
            default -> {
                logUtil.logWarning("修改买家个人信息失败 - 未知错误: " + account);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("修改失败，请稍后重试");
            }
        };
    }

    /**
     * 按游戏名查询游戏
     * GET /api/buyers/games/search-by-name
     */
    @GetMapping("/games/search-by-name")
    public ResponseEntity<List<GameSearchResponseDTO>> searchGameByName(@RequestParam(required = false) String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            logUtil.logDebug("按游戏名查询游戏 - 查询所有游戏");
        } else {
            logUtil.logDebug("按游戏名查询游戏 - 游戏名: " + gameName);
        }
        
        List<GameSearchResponseDTO> result = buyerUserService.searchGameByName(gameName);
        return ResponseEntity.ok(result);
    }

    /**
     * 按游戏分类查询游戏
     * GET /api/buyers/games/search-by-category
     */
    @GetMapping("/games/search-by-category")
    public ResponseEntity<List<GameSearchResponseDTO>> searchGameByCategory(@RequestParam String category) {
        try {
            // URL解码category参数，处理特殊字符
            String decodedCategory = java.net.URLDecoder.decode(category, "UTF-8");
            logUtil.logDebug("按游戏分类查询游戏 - 分类: " + decodedCategory);
            
            List<GameSearchResponseDTO> result = buyerUserService.searchGameByCategory(decodedCategory);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - category: " + category + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("按游戏分类查询游戏 - 分类: " + category);
            List<GameSearchResponseDTO> result = buyerUserService.searchGameByCategory(category);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 按游戏热度查询游戏
     * GET /api/buyers/games/search-by-popularity
     */
    @GetMapping("/games/search-by-popularity")
    public ResponseEntity<List<GameSearchResponseDTO>> searchGameByPopularity(
            @RequestParam(required = false) BigDecimal minPopularity) {
        logUtil.logDebug("按游戏热度查询游戏 - 最小热度: " + minPopularity);
        
        List<GameSearchResponseDTO> result = buyerUserService.searchGameByPopularity(
                minPopularity != null ? minPopularity : BigDecimal.ZERO);
        return ResponseEntity.ok(result);
    }

    /**
     * 按买家偏好查询游戏
     * GET /api/buyers/games/search-by-preference
     */
    @GetMapping("/games/search-by-preference")
    public ResponseEntity<List<GameSearchResponseDTO>> searchGameByBuyerPreference(@RequestParam String buyerNickname) {
        try {
            // URL解码buyerNickname参数，处理特殊字符
            String decodedBuyerNickname = java.net.URLDecoder.decode(buyerNickname, "UTF-8");
            logUtil.logDebug("按买家偏好查询游戏 - 买家昵称: " + decodedBuyerNickname);
            
            List<GameSearchResponseDTO> result = buyerUserService.searchGameByBuyerPreference(decodedBuyerNickname);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - buyerNickname: " + buyerNickname + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("按买家偏好查询游戏 - 买家昵称: " + buyerNickname);
            List<GameSearchResponseDTO> result = buyerUserService.searchGameByBuyerPreference(buyerNickname);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏详细信息查询
     * GET /api/buyers/games/details
     */
    @GetMapping("/games/details")
    public ResponseEntity<?> getGameDetails(@RequestParam String gameName) {
        try {
            // URL解码gameName参数，处理特殊字符
            String decodedGameName = java.net.URLDecoder.decode(gameName, "UTF-8");
            logUtil.logDebug("查询游戏详细信息 - 游戏名: " + decodedGameName);
            
            GameInfoDTO result = buyerUserService.getGameDetails(decodedGameName);
            if (result == null) {
                logUtil.logWarning("查询游戏详细信息失败 - 游戏不存在或已下架: " + decodedGameName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或已下架");
            }
            
            logUtil.logDebug("查询游戏详细信息成功 - 游戏名: " + decodedGameName);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - gameName: " + gameName + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("查询游戏详细信息 - 游戏名: " + gameName);
            GameInfoDTO result = buyerUserService.getGameDetails(gameName);
            if (result == null) {
                logUtil.logWarning("查询游戏详细信息失败 - 游戏不存在或已下架: " + gameName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或已下架");
            }
            logUtil.logDebug("查询游戏详细信息成功 - 游戏名: " + gameName);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏评价查询
     * GET /api/buyers/games/reviews
     */
    @GetMapping("/games/reviews")
    public ResponseEntity<List<GameReviewDTO>> getGameReviews(@RequestParam String gameName) {
        try {
            // URL解码gameName参数，处理特殊字符
            String decodedGameName = java.net.URLDecoder.decode(gameName, "UTF-8");
            logUtil.logDebug("查询游戏评价 - 游戏名: " + decodedGameName);
            
            List<GameReviewDTO> result = buyerUserService.getGameReviews(decodedGameName);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - gameName: " + gameName + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("查询游戏评价 - 游戏名: " + gameName);
            List<GameReviewDTO> result = buyerUserService.getGameReviews(gameName);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 进行游戏评价
     * POST /api/buyers/games/reviews
     */
    @PostMapping("/games/reviews")
    public ResponseEntity<?> submitGameReview(@Valid @RequestBody GameReviewRequestDTO reviewRequest) {
        logUtil.logDebug("提交游戏评价 - 买家昵称: " + reviewRequest.getBuyerNickname() + ", 游戏名: " + reviewRequest.getGameName());
        
        boolean success = buyerUserService.submitGameReview(
                reviewRequest.getBuyerNickname(),
                reviewRequest.getGameName(),
                reviewRequest.getScore(),
                reviewRequest.getComment()
        );
        
        if (success) {
            logUtil.logDebug("提交游戏评价成功 - 买家昵称: " + reviewRequest.getBuyerNickname() + ", 游戏名: " + reviewRequest.getGameName());
            return ResponseEntity.ok().body("评价提交成功");
        } else {
            logUtil.logWarning("提交游戏评价失败 - 买家昵称: " + reviewRequest.getBuyerNickname() + ", 游戏名: " + reviewRequest.getGameName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("评价提交失败");
        }
    }

    /**
     * 游戏库查询
     * GET /api/buyers/game-library
     */
    @GetMapping("/game-library")
    public ResponseEntity<List<GameLibraryResponseDTO>> getBuyerGameLibrary(@RequestParam String buyerNickname) {
        try {
            // URL解码buyerNickname参数，处理特殊字符
            String decodedBuyerNickname = java.net.URLDecoder.decode(buyerNickname, "UTF-8");
            logUtil.logDebug("查询游戏库 - 买家昵称: " + decodedBuyerNickname);
            
            List<GameLibraryResponseDTO> result = buyerUserService.getBuyerGameLibrary(decodedBuyerNickname);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - buyerNickname: " + buyerNickname + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("查询游戏库 - 买家昵称: " + buyerNickname);
            List<GameLibraryResponseDTO> result = buyerUserService.getBuyerGameLibrary(buyerNickname);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏下载
     * GET /api/buyers/games/download
     */
    @GetMapping("/games/download")
    public ResponseEntity<?> getGameDownloadLink(@RequestParam String buyerNickname, @RequestParam String gameName) {
        try {
            // URL解码参数，处理特殊字符
            String decodedBuyerNickname = java.net.URLDecoder.decode(buyerNickname, "UTF-8");
            String decodedGameName = java.net.URLDecoder.decode(gameName, "UTF-8");
            logUtil.logDebug("获取游戏下载链接 - 买家昵称: " + decodedBuyerNickname + ", 游戏名: " + decodedGameName);
            
            GameDownloadResponseDTO result = buyerUserService.getGameDownloadLink(decodedBuyerNickname, decodedGameName);
            if (result == null) {
                logUtil.logWarning("获取游戏下载链接失败 - 游戏不在游戏库中: " + decodedGameName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不在您的游戏库中");
            }
            
            logUtil.logDebug("获取游戏下载链接成功 - 买家昵称: " + decodedBuyerNickname + ", 游戏名: " + decodedGameName);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - buyerNickname: " + buyerNickname + ", gameName: " + gameName + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("获取游戏下载链接 - 买家昵称: " + buyerNickname + ", 游戏名: " + gameName);
            GameDownloadResponseDTO result = buyerUserService.getGameDownloadLink(buyerNickname, gameName);
            if (result == null) {
                logUtil.logWarning("获取游戏下载链接失败 - 游戏不在游戏库中: " + gameName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不在您的游戏库中");
            }
            logUtil.logDebug("获取游戏下载链接成功 - 买家昵称: " + buyerNickname + ", 游戏名: " + gameName);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 生成订单
     * POST /api/buyers/orders
     */
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        logUtil.logDebug("生成订单 - 买家昵称: " + orderRequest.getBuyerNickname() + ", 游戏名: " + orderRequest.getGameName());
        
        OrderResponseDTO result = buyerUserService.createOrder(orderRequest.getBuyerNickname(), orderRequest.getGameName());
        if (result == null) {
            logUtil.logWarning("生成订单失败 - 买家昵称: " + orderRequest.getBuyerNickname() + ", 游戏名: " + orderRequest.getGameName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("生成订单失败");
        }
        
        logUtil.logDebug("生成订单成功 - 订单ID: " + result.getOrderId());
        return ResponseEntity.ok(result);
    }

    /**
     * 查询订单
     * GET /api/buyers/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getBuyerOrders(@RequestParam String buyerNickname) {
        try {
            // URL解码buyerNickname参数，处理特殊字符
            String decodedBuyerNickname = java.net.URLDecoder.decode(buyerNickname, "UTF-8");
            logUtil.logDebug("查询订单 - 买家昵称: " + decodedBuyerNickname);
            
            List<OrderResponseDTO> result = buyerUserService.getBuyerOrders(decodedBuyerNickname);
            return ResponseEntity.ok(result);
        } catch (java.io.UnsupportedEncodingException e) {
            logUtil.logWarning("URL解码失败 - buyerNickname: " + buyerNickname + ", 错误: " + e.getMessage());
            // 如果解码失败，使用原始参数
            logUtil.logDebug("查询订单 - 买家昵称: " + buyerNickname);
            List<OrderResponseDTO> result = buyerUserService.getBuyerOrders(buyerNickname);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 支付订单
     * PUT /api/buyers/orders/pay
     */
    @PutMapping("/orders/pay")
    public ResponseEntity<?> payOrder(@RequestParam String orderId) {
        logUtil.logDebug("支付订单 - 订单ID: " + orderId);
        
        boolean success = buyerUserService.payOrder(orderId);
        if (success) {
            logUtil.logDebug("支付订单成功 - 订单ID: " + orderId);
            return ResponseEntity.ok().body("订单支付成功");
        } else {
            logUtil.logWarning("支付订单失败 - 订单ID: " + orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("订单支付失败");
        }
    }

    /**
     * 取消订单
     * PUT /api/buyers/orders/cancel
     */
    @PutMapping("/orders/cancel")
    public ResponseEntity<?> cancelOrder(@RequestParam String orderId) {
        logUtil.logDebug("取消订单 - 订单ID: " + orderId);
        
        boolean success = buyerUserService.cancelOrder(orderId);
        if (success) {
            logUtil.logDebug("取消订单成功 - 订单ID: " + orderId);
            return ResponseEntity.ok().body("订单取消成功");
        } else {
            logUtil.logWarning("取消订单失败 - 订单ID: " + orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("订单取消失败");
        }
    }
}
