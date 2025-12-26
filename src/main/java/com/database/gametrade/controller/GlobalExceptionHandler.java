package com.database.gametrade.controller;

import com.database.gametrade.util.LogUtil;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private LogUtil logUtil;

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logUtil.logWarning("参数验证失败 - 错误数量: " + ex.getBindingResult().getErrorCount());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "系统内部错误");
        error.put("message", ex.getMessage());
        
        // 记录错误日志
        logUtil.logError("系统内部错误", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "数据异常");
        error.put("message", "请求数据不完整或格式错误");
        
        logUtil.logError("空指针异常", ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "参数错误");
        error.put("message", ex.getMessage());
        
        logUtil.logError("非法参数异常", ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理SQL语法异常
     */
    @ExceptionHandler(SQLGrammarException.class)
    public ResponseEntity<Map<String, String>> handleSQLGrammarException(SQLGrammarException ex) {
        Map<String, String> error = new HashMap<>();
        
        // 通用错误处理，不针对具体错误信息
        error.put("error", "数据库操作错误");
        error.put("message", "数据库查询执行失败，请检查数据或稍后重试");
        
        logUtil.logError("SQL语法异常", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 处理数据访问异常，特别处理存储过程返回的业务错误
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccessException(DataAccessException ex) {
        Map<String, String> error = new HashMap<>();
        String errorMessage = ex.getMessage();
        
        // 检查是否为存储过程返回的业务错误
        if (errorMessage != null) {
            if (errorMessage.contains("厂商账号不存在") || errorMessage.contains("不是供应商角色")) {
                error.put("error", "业务错误");
                error.put("message", "厂商账号不存在或不是供应商角色");
                logUtil.logWarning("存储过程业务错误 - 厂商账号不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (errorMessage.contains("游戏不存在或不属于该厂商")) {
                error.put("error", "业务错误");
                error.put("message", "游戏不存在或不属于该厂商");
                logUtil.logWarning("存储过程业务错误 - 游戏不存在或不属于该厂商");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (errorMessage.contains("游戏已上架，无需重复申请")) {
                error.put("error", "业务错误");
                error.put("message", "游戏已上架，无需重复申请");
                logUtil.logWarning("存储过程业务错误 - 游戏已上架，无需重复申请");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else if (errorMessage.contains("该游戏已有待审批的申请")) {
                error.put("error", "业务错误");
                error.put("message", "该游戏已有待审批的申请");
                logUtil.logWarning("存储过程业务错误 - 该游戏已有待审批的申请");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }
        
        // 如果是真正的数据访问错误
        error.put("error", "数据访问错误");
        error.put("message", "数据库操作失败，请稍后重试");
        
        logUtil.logError("数据访问异常", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 处理SQL异常，精确捕获JDBC异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> handleSQLException(SQLException ex) {
        Map<String, String> error = new HashMap<>();
        String errorMessage = ex.getMessage();
        
        // 检查是否为存储过程返回的业务错误
        if (errorMessage != null) {
            if (errorMessage.contains("厂商账号不存在") || errorMessage.contains("不是供应商角色")) {
                error.put("error", "业务错误");
                error.put("message", "厂商账号不存在或不是供应商角色");
                logUtil.logWarning("SQL异常 - 存储过程业务错误: 厂商账号不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (errorMessage.contains("游戏不存在或不属于该厂商")) {
                error.put("error", "业务错误");
                error.put("message", "游戏不存在或不属于该厂商");
                logUtil.logWarning("SQL异常 - 存储过程业务错误: 游戏不存在或不属于该厂商");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (errorMessage.contains("游戏已上架，无需重复申请")) {
                error.put("error", "业务错误");
                error.put("message", "游戏已上架，无需重复申请");
                logUtil.logWarning("SQL异常 - 存储过程业务错误: 游戏已上架，无需重复申请");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else if (errorMessage.contains("该游戏已有待审批的申请")) {
                error.put("error", "业务错误");
                error.put("message", "该游戏已有待审批的申请");
                logUtil.logWarning("SQL异常 - 存储过程业务错误: 该游戏已有待审批的申请");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }
        
        // 如果是真正的SQL错误
        error.put("error", "SQL错误");
        error.put("message", "数据库操作失败，请检查SQL语句或联系管理员");
        
        logUtil.logError("SQL异常", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
