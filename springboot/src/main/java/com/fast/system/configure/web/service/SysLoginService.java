package com.fast.system.configure.web.service;

import com.fast.system.configure.security.context.AuthenticationContextHolder;
import com.fast.system.general.core.domain.model.LoginUser;
import com.fast.system.general.utils.ServletUtils;
import com.fast.system.general.utils.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 登录校验方法
 *
 * @author fast
 */
@Component
public class SysLoginService {
    @Resource
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new RuntimeException("用户不存在/密码错误");
        }
        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                // 登录失败也记录日志
                printAccessLog(username, false);
                throw new RuntimeException("用户不存在/密码错误");
            } else {
                printAccessLog(username, false);
                throw new RuntimeException(e.getMessage());
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 登录成功记录日志
        printAccessLog(username, true);
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 打印访问日志（用于演示局域网部署 - 区分不同用户访问）
     */
    private void printAccessLog(String username, boolean success) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            String ip = getClientIp(request);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = sdf.format(new Date());
            String status = success ? "✅登录成功" : "❌登录失败";
            System.out.println("========================================");
            System.out.println("[访问日志] " + status);
            System.out.println("  时间: " + time);
            System.out.println("  用户: " + username);
            System.out.println("  IP  : " + ip);
            System.out.println("  UA  : " + (request != null && request.getHeader("User-Agent") != null
                    ? request.getHeader("User-Agent").substring(0, Math.min(50, request.getHeader("User-Agent").length()))
                    : "unknown"));
            System.out.println("========================================");
        } catch (Exception e) {
            System.out.println("[访问日志] 记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP（处理反向代理情况）
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
