package com.fast.system.configure.web.service;

import com.fast.system.domain.SysUser;
import com.fast.system.general.core.domain.model.LoginUser;
import com.fast.system.general.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * token验证处理
 *
 * @author fast
 */
@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 直接从token中解析用户信息，而不是从缓存中获取
                LoginUser user = buildLoginUser(claims);
                return user;
            } catch (Exception e) {
                log.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", loginUser.getUsername());
        claims.put("userId", loginUser.getUserId());
        // 将Set转换为List存储，避免类型转换问题
        if (loginUser.getPermissions() != null) {
            claims.put("permissions", new java.util.ArrayList<>(loginUser.getPermissions()));
        } else {
            claims.put("permissions", new java.util.ArrayList<>());
        }
        claims.put("loginTime", System.currentTimeMillis());
        claims.put("expireTime", System.currentTimeMillis() + expireTime * MILLIS_MINUTE);
        // 添加用户信息到token
        claims.put("os", loginUser.getOs());
        // 用户基本信息
        SysUser user = loginUser.getUser();
        if (user != null) {
            claims.put("userName", user.getUserName());
            claims.put("nickName", user.getNickName());
            claims.put("email", user.getEmail());
            claims.put("phonenumber", user.getPhonenumber());
            claims.put("sex", user.getSex());
            claims.put("avatar", user.getAvatar());
        }
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginUser 登录信息
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        // 令牌过期验证
        if (expireTime - currentTime <= 0) {
            throw new RuntimeException("Token已过期");
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 不再需要缓存用户信息，因为信息都在token中
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims) {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 根据Claims构建LoginUser对象
     *
     * @param claims JWT中的声明
     * @return LoginUser对象
     */
    private LoginUser buildLoginUser(Claims claims) {
        LoginUser loginUser = new LoginUser();

        // 从claims中提取用户信息，增加空值检查
        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            loginUser.setUserId(Long.valueOf(userIdObj.toString()));
        }

        // 处理权限列表类型转换问题
        Object permissionsObj = claims.get("permissions");
        if (permissionsObj instanceof List) {
            List<String> permissionsList = (List<String>) permissionsObj;
            Set<String> permissionsSet = new HashSet<>(permissionsList);
            loginUser.setPermissions(permissionsSet);
        } else if (permissionsObj instanceof Set) {
            loginUser.setPermissions((Set<String>) permissionsObj);
        } else if (permissionsObj instanceof Collection) {
            loginUser.setPermissions(new HashSet<>((Collection<String>) permissionsObj));
        } else {
            loginUser.setPermissions(new HashSet<>());
        }

        Object loginTimeObj = claims.get("loginTime");
        if (loginTimeObj != null) {
            loginUser.setLoginTime(Long.valueOf(loginTimeObj.toString()));
        }

        Object expireTimeObj = claims.get("expireTime");
        if (expireTimeObj != null) {
            loginUser.setExpireTime(Long.valueOf(expireTimeObj.toString()));
        }

        loginUser.setOs((String) claims.get("os"));

        // 构建SysUser对象
        SysUser user = new SysUser();
        user.setUserId(loginUser.getUserId());
        user.setUserName((String) claims.get("userName"));
        user.setNickName((String) claims.get("nickName"));
        user.setEmail((String) claims.get("email"));
        user.setPhonenumber((String) claims.get("phonenumber"));
        user.setSex((String) claims.get("sex"));
        user.setAvatar((String) claims.get("avatar"));
        loginUser.setUser(user);

        return loginUser;
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        return token;
    }
}