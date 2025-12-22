package com.database.gametrade.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 日志工具类 - 统一处理应用日志
 */
@Component
public class LogUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    
    /**
     * 记录买家登录成功日志
     * @param account 账号
     * @param nickname 昵称
     */
    public void logBuyerLoginSuccess(String account, String nickname) {
        logger.info("买家登录成功 - 账号: {}, 昵称: {}", account, nickname);
    }
    
    /**
     * 记录买家登录失败日志
     * @param account 账号
     * @param reason 失败原因
     */
    public void logBuyerLoginFailure(String account, String reason) {
        logger.warn("买家登录失败 - 账号: {}, 原因: {}", account, reason);
    }
    
    /**
     * 记录买家登出日志
     * @param account 账号
     * @param nickname 昵称
     */
    public void logBuyerLogout(String account, String nickname) {
        logger.info("买家登出 - 账号: {}, 昵称: {}", account, nickname);
    }
    
    /**
     * 记录商家登录成功日志
     * @param account 账号
     * @param companyName 企业名
     */
    public void logVendorLoginSuccess(String account, String companyName) {
        logger.info("商家登录成功 - 账号: {}, 企业名: {}", account, companyName);
    }
    
    /**
     * 记录商家登录失败日志
     * @param account 账号
     * @param reason 失败原因
     */
    public void logVendorLoginFailure(String account, String reason) {
        logger.warn("商家登录失败 - 账号: {}, 原因: {}", account, reason);
    }
    
    /**
     * 记录商家登出日志
     * @param account 账号
     * @param companyName 企业名
     */
    public void logVendorLogout(String account, String companyName) {
        logger.info("商家登出 - 账号: {}, 企业名: {}", account, companyName);
    }
    
    /**
     * 记录买家注册请求日志
     * @param account 账号
     * @param nickname 昵称
     */
    public void logBuyerRegisterRequest(String account, String nickname) {
        logger.info("买家注册请求 - 账号: {}, 昵称: {}", account, nickname);
    }
    
    /**
     * 记录买家注册成功日志
     * @param account 账号
     * @param nickname 昵称
     */
    public void logBuyerRegisterSuccess(String account, String nickname) {
        logger.info("买家注册成功 - 账号: {}, 昵称: {}", account, nickname);
    }
    
    /**
     * 记录买家注册失败日志
     * @param account 账号
     * @param reason 失败原因
     */
    public void logBuyerRegisterFailure(String account, String reason) {
        logger.warn("买家注册失败 - 账号: {}, 原因: {}", account, reason);
    }
    
    /**
     * 记录商家注册请求日志
     * @param account 账号
     * @param companyName 企业名
     */
    public void logVendorRegisterRequest(String account, String companyName) {
        logger.info("商家注册请求 - 账号: {}, 企业名: {}", account, companyName);
    }
    
    /**
     * 记录商家注册成功日志
     * @param account 账号
     * @param companyName 企业名
     */
    public void logVendorRegisterSuccess(String account, String companyName) {
        logger.info("商家注册成功 - 账号: {}, 企业名: {}", account, companyName);
    }
    
    /**
     * 记录商家注册失败日志
     * @param account 账号
     * @param reason 失败原因
     */
    public void logVendorRegisterFailure(String account, String reason) {
        logger.warn("商家注册失败 - 账号: {}, 原因: {}", account, reason);
    }
    
    /**
     * 记录错误信息
     * @param errorMessage 错误信息
     * @param exception 异常对象（可选）
     */
    public void logError(String errorMessage, Exception exception) {
        if (exception != null) {
            logger.error("系统错误 - 信息: {}, 异常: {}", errorMessage, exception.getMessage(), exception);
        } else {
            logger.error("系统错误 - 信息: {}", errorMessage);
        }
    }
    
    /**
     * 记录警告信息
     * @param warningMessage 警告信息
     */
    public void logWarning(String warningMessage) {
        logger.warn("系统警告 - 信息: {}", warningMessage);
    }
    
    /**
     * 记录调试信息
     * @param debugMessage 调试信息
     */
    public void logDebug(String debugMessage) {
        logger.debug("调试信息 - {}", debugMessage);
    }
}
