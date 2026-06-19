package dev.javacadabra.reservasviaje.reserva.infraestructura.configuracion;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ZeebeWorkerContextAspect {

    @Around("@annotation(io.camunda.zeebe.spring.client.annotation.JobWorker)")
    public Object enriquecerContextoLog(ProceedingJoinPoint joinPoint) throws Throwable {
        ActivatedJob job = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof ActivatedJob)
                .map(arg -> (ActivatedJob) arg)
                .findFirst()
                .orElse(null);

        if (job != null) {
            MDC.put("processInstanceKey", String.valueOf(job.getProcessInstanceKey()));
            log.info("🔗 Proceso: {} [{}] | Job: {}",
                    job.getProcessInstanceKey(),
                    job.getBpmnProcessId(),
                    job.getKey());
        }
        try {
            return joinPoint.proceed();
        } finally {
            MDC.clear();
        }
    }
}
