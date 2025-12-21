package com.database.gametrade.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Controller
public class RootController {

    /**
     * 根路径端点 - 返回格式化的HTML页面
     * GET /
     */
    @GetMapping("/")
    @ResponseBody
    public String root() {
        String formattedTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        
        return "<!DOCTYPE html>" +
               "<html lang=\"zh-CN\">" +
               "<head>" +
               "    <meta charset=\"UTF-8\">" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
               "    <title>GameTrade - 游戏交易平台</title>" +
               "    <style>" +
               "        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }" +
               "        h1 { color: #333; }" +
               "        .info { background: #f5f5f5; padding: 20px; border-radius: 5px; margin: 20px 0; }" +
               "        .endpoint { background: #e9ecef; padding: 10px; margin: 5px 0; border-left: 4px solid #007bff; }" +
               "        .status { color: #28a745; font-weight: bold; }" +
               "    </style>" +
               "</head>" +
               "<body>" +
               "    <h1>🎮 GameTrade 游戏交易平台</h1>" +
               "    <div class=\"info\">" +
               "        <p><strong>服务名称:</strong> GameTrade Backend API</p>" +
               "        <p><strong>版本:</strong> 1.0.0</p>" +
               "        <p><strong>描述:</strong> 游戏交易平台后端服务</p>" +
               "        <p><strong>时间:</strong> " + formattedTime + "</p>" +
               "        <p><strong>状态:</strong> <span class=\"status\">运行中</span></p>" +
               "    </div>" +
               "    " +
               "    <h2>可用接口端点:</h2>" +
               "    <div class=\"endpoint\"><strong>健康检查:</strong> GET /health</div>" +
               "    <div class=\"endpoint\"><strong>就绪检查:</strong> GET /health/ready</div>" +
               "    <div class=\"endpoint\"><strong>存活检查:</strong> GET /health/live</div>" +
               "    <div class=\"endpoint\"><strong>用户注册:</strong> POST /api/users/register</div>" +
               "    <div class=\"endpoint\"><strong>用户登录:</strong> POST /api/users/login</div>" +
               "    " +
               "    <p style=\"margin-top: 30px; color: #666;\">这是一个为JavaFX客户端提供API服务的后端应用。</p>" +
               "</body>" +
               "</html>";
    }
}
