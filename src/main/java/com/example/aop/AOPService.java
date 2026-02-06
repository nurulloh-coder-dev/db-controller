package com.example.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.example.service.FileService;

@Aspect
@Component
@Slf4j
public class AOPService {

    private final FileService fileService;

    public AOPService(FileService fileService) {
        this.fileService = fileService;
    }

    @Around(
            value = "execution(* com.example.controller.*.create(..))"
    )
    public Object writeOnCreate(ProceedingJoinPoint joinPoint) throws Throwable {
        return getLog(joinPoint);
    }

    @Around(
            value = "execution(* com.example.controller.*.update(..))"
    )
    public Object writeOnUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        return getLog(joinPoint);
    }

    @Around(
            value = "execution(* com.example.controller.*.delete(..))"
    )
    public Object writeOnDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        return getLog(joinPoint);
    }

    @Around(
            value = "execution(* com.example.controller.*.get(..))"
    )
    public Object writeOnGet(ProceedingJoinPoint joinPoint) throws Throwable {
        return getLog(joinPoint);
    }

    @Around(
            value = "execution(* com.example.controller.*.getAll(..))"
    )
    public Object writeOnGetAll(ProceedingJoinPoint joinPoint) throws Throwable {
        return getLog(joinPoint);
    }

    private Object getLog(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long end = System.currentTimeMillis();
        fileService.writeToFile("Success: %s %s Execution time was %s ms".formatted(request.getMethod(), request.getRequestURI(),(end-start)));
        return proceed;
    }


    @AfterThrowing(
            value = "execution(* com.example.controller.*.*(..))",
            throwing = "ex"
    )
    public void write(Throwable ex){
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();
        fileService.writeToFile("Error: %s %s, reason: %s".formatted(request.getMethod(), request.getRequestURI(), ex.getMessage()));
    }
}
