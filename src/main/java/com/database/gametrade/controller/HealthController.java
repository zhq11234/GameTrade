package com.database.gametrade.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查端点
     * GET /health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now().toString());
        status.put("service", "GameTrade Backend");
        status.put("version", "1.0.0");
        
        // 添加系统信息
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory());
        systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
        
        status.put("system", systemInfo);
        
        return ResponseEntity.ok(status);
    }

    /**
     * 就绪检查端点
     * GET /health/ready
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "READY");
        status.put("timestamp", Instant.now().toString());
        status.put("message", "Service is ready to accept requests");
        
        // 这里可以添加数据库连接检查等
        // 如果数据库连接正常，返回READY，否则返回NOT_READY
        
        return ResponseEntity.ok(status);
    }

    /**
     * 存活检查端点
     * GET /health/live
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ALIVE");
        status.put("timestamp", Instant.now().toString());
        status.put("message", "Service is alive");
        
        return ResponseEntity.ok(status);
    }
}
