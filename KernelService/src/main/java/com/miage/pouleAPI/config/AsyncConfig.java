package com.miage.pouleAPI.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
public class AsyncConfig {

    /**
     * Configuration de l'executor pour les tâches asynchrones (emails, etc.)
     * Dimensionné pour 10 000 à 30 000 utilisateurs simultanés
     * 
     * Capacité totale : 50 threads + 10 000 en queue = 10 050 emails en parallèle
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Threads toujours actifs (optimisé pour charge continue)
        // 20 threads = peut traiter ~1200-2400 emails/min (env. 2-5s par email)
        executor.setCorePoolSize(20);
        
        // Threads maximum en pic de charge (onboarding massif, reset password groupé)
        // 50 threads = peut traiter ~3000-6000 emails/min
        executor.setMaxPoolSize(50);
        
        // File d'attente massive pour absorber les pics
        // 10 000 emails en buffer = ~3-5 minutes de traitement en cas de pic
        executor.setQueueCapacity(10000);
        
        // Durée de vie des threads inactifs (au-delà du core)
        executor.setKeepAliveSeconds(60);
        
        // Préfixe des noms de threads (pour monitoring et debug)
        executor.setThreadNamePrefix("async-email-");
        
        // Politique de rejet : log l'erreur au lieu de crash silencieux
        executor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());
        
        // Attend la fin des tâches en cours lors du shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Handler personnalisé pour gérer les rejets de tâches
     * Permet de logger et monitorer les emails qui n'ont pas pu être envoyés
     */
    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.error("❌ ALERTE: Email rejeté - Pool saturé! " +
                     "Active: {}, Queue: {}, Total submitted: {}", 
                     executor.getActiveCount(),
                     executor.getQueue().size(),
                     executor.getTaskCount());
        }
    }
}
