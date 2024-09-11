package org.example.expert.domain.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class UserAop {

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment())")
    private void adminDeleteComment() {}

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole())")
    private void adminChangeUserRole() {}

    @Before("adminDeleteComment() || adminChangeUserRole()")
    public void deleteComment(JoinPoint joinPoint) throws Throwable {
        //현재 요청 정보를 가져옴
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();

        Long userId = (Long) request.getAttribute("userId");
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl = request.getRequestURI();

        log.info("요청한 사용자의 ID: ", userId);
        log.info("API 요청 시각: ", requestTime);
        log.info("API 요청 URL: ", requestUrl);
    }
}
