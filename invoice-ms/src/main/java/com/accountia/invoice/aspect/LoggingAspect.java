package com.accountia.invoice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP Logging Aspect — automatically logs every service method call.
 *
 * <p>How AOP works here:
 * <ol>
 *   <li>{@code @Aspect} marks this class as an aspect (cross-cutting concern).</li>
 *   <li>{@code @Around} wraps every matching method: it runs code BEFORE calling
 *       the real method ({@code proceed()}), and AFTER it returns (or throws).</li>
 *   <li>The pointcut expression {@code "execution(* com.accountia.invoice.service.*.*(..))"} matches
 *       ANY method in ANY class inside the service package.</li>
 * </ol>
 *
 * <p>This gives us free observability on every business operation without touching
 * any service class — a key advantage of AOP over manual logging.
 *
 * <p>Output example:
 * <pre>
 *   [InvoiceService.createInvoice] START — args: [CreateInvoiceRequest{businessId='abc',...}]
 *   [InvoiceService.createInvoice] END — 42 ms — OK
 * </pre>
 */
@Aspect
@Component
public class LoggingAspect {

    // SLF4J logger — each log entry includes the class name for easy filtering
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Intercepts every public method in the service package.
     *
     * <p>{@code execution(* com.accountia.invoice.service.*.*(..))} means:
     * <ul>
     *   <li>{@code *} — any return type</li>
     *   <li>{@code com.accountia.invoice.service.*} — any class in this package</li>
     *   <li>{@code .*} — any method name</li>
     *   <li>{@code (..)} — any number/type of arguments</li>
     * </ul>
     *
     * @param joinPoint the intercepted method call (gives access to args, class, method name)
     * @return whatever the real method returns (or rethrows its exception)
     */
    @Around("execution(* com.accountia.invoice.service.*.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract class + method name from the join point
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        String label = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        log.debug("[{}] START — args count: {}", label, joinPoint.getArgs().length);

        try {
            // Call the real method — this is where the actual business logic executes
            Object result = joinPoint.proceed();

            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("[{}] END — {} ms — OK", label, elapsed);

            return result;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            // Log the error at WARN level with the exception message (not full stack — that's in GlobalExceptionHandler)
            log.warn("[{}] ERROR — {} ms — {}: {}", label, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex; // Re-throw so GlobalExceptionHandler can handle it
        }
    }
}
