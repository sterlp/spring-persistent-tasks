package org.sterl.spring.persistent_tasks.task.component;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.task.util.ReflectionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskTransactionComponent {

    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate template;
    private final Set<Propagation> joinTransaction = EnumSet.of(
            Propagation.MANDATORY, Propagation.REQUIRED, Propagation.SUPPORTS);
    private final Map<PersistentTask<? extends Serializable>, Optional<TransactionTemplate>> cache = new ConcurrentHashMap<>();
    
    public Optional<TransactionTemplate> getTransactionTemplate(PersistentTask<? extends Serializable> task) {
        if (cache.containsKey(task)) return cache.get(task);

        Optional<TransactionTemplate> result;
        // first we apply a default
        if (task.isTransactional()) result = Optional.of(template);
        else result = Optional.empty();
        
        var annotation = ReflectionUtil.getAnnotation(task, Transactional.class);
        if (annotation != null) {
            log.debug("found {} on task={}, creating custom ", annotation, task.getClass().getName());
            result = Optional.ofNullable(builTransactionTemplate(task, annotation));
        }
        cache.put(task, result);
        return result;
    }

    private TransactionTemplate builTransactionTemplate(PersistentTask<? extends Serializable> task, Transactional annotation) {
        TransactionTemplate result;
        if (joinTransaction.contains(annotation.propagation())) {
            // No direct mapping for 'rollbackFor' or 'noRollbackFor'
            if (annotation.noRollbackFor().length > 0 || annotation.rollbackFor().length > 0) {
                throw new IllegalArgumentException("noRollbackFor or rollbackFor not supported. Please remove the settings on " 
                        + task.getClass());
            } else {
                var dev = convertTransactionalToDefinition(annotation);
                dev.setName(task.getClass().getSimpleName());
                result = new TransactionTemplate(transactionManager, dev);
            }
        } else {
            log.info("Propagation={} disables join of transaction for {}", 
                    annotation.propagation(), task.getClass().getName());
            result = null;
        }
        return result;
    }
    
    static DefaultTransactionDefinition convertTransactionalToDefinition(Transactional transactional) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        // Map Transactional attributes to DefaultTransactionDefinition
        def.setIsolationLevel(transactional.isolation().value());
        def.setPropagationBehavior(Propagation.REQUIRED.value());
        def.setTimeout(transactional.timeout());
        def.setReadOnly(false);

        return def;
    }
}
