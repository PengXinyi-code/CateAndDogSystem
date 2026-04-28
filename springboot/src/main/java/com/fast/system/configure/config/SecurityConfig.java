package com.fast.system.configure.config;

import com.fast.system.configure.security.filter.JwtAuthenticationTokenFilter;
import com.fast.system.configure.security.handle.LogoutSuccessHandlerImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * spring security配置
 *
 * @author fast
 */
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig {
    /**
     * 自定义用户认证逻辑
     */
    @Resource
    private UserDetailsService userDetailsService;

    /**
     * 退出处理类
     */
    @Resource
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * token认证过滤器
     */
    @Resource
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 跨域过滤器
     */
    @Resource
    private CorsFilter corsFilter;

    /**
     * 身份验证实现
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    /**
     * anyRequest          |   匹配所有请求路径
     * permitAll           |   用户可以任意访问
     * authenticated       |   用户登录后可访问
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // CSRF禁用，因为不使用session
                .csrf(csrf -> csrf.disable())
                // 禁用HTTP响应标头
                .headers((headersCustomizer) -> {
                    headersCustomizer.cacheControl(cache -> cache.disable()).frameOptions(options -> options.sameOrigin());
                })
                // 基于token，所以不需要session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 注解标记允许匿名访问的url
                .authorizeHttpRequests((requests) -> {
                    // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                    requests.requestMatchers("/login", "/register").permitAll()
                            // <--- 新增这一行：放行 /uploads/** 开头的路径，让图片可以公开访问
                            .requestMatchers("/uploads/**").permitAll()
                            .requestMatchers("/profile/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js", "/**.jpg", "/**.png", "/**.gif", "/**.jpeg").permitAll()
                            // <--- 重点：加上这一行，放行所有 /api/ 开头的接口
                            .requestMatchers("/api/**").permitAll()
                            // 静态资源，可匿名访问
                            .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js", "/profile/**").permitAll()
                            .requestMatchers("/druid/**").permitAll()
                            // 除上面外的所有请求全部需要鉴权认证
                            .anyRequest().authenticated();
                })
                // 添加Logout filter
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                // 添加JWT filter
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加CORS filter
                .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }

    /**
     * 自定义密码编码器，用于明文密码验证
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}
