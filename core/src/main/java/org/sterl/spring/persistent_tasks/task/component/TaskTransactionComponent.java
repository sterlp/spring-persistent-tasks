package org.sterl.spring.persistent_tasks.task.component;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
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
    
    /**
     * Returns a transaction template if and only if we can join the transaction with the anotated apply method
     */
    public Optional<TransactionTemplate> buildOrGetDefaultTransactionTemplate(PersistentTask<? extends Serializable> task) {
        Optional<TransactionTemplate> result;
        var annotation = ReflectionUtil.getAnnotation(task, Transactional.class);
        if (annotation == null) {
            result = useDefaultTransactionTemplate(task);
        } else {
            result = Optional.ofNullable(builTransactionTemplate(task, annotation));
        }
        return result;
    }

    public Optional<TransactionTemplate> useDefaultTransactionTemplate(PersistentTask<? extends Serializable> task) {
        Optional<TransactionTemplate> result;
        // first we apply a default
        if (task.isTransactional()) result = Optional.of(template);
        else result = Optional.empty();
        
        log.debug("Using default template={} for task={}", result, task.getClass().getName());
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
                log.debug("Using custom template={} for task={}", result, task.getClass().getName());
            }
        } else {
            log.info("Propagation={} disables join of transaction for task={}", annotation.propagation(), task.getClass().getName());
            result = null;
        }
        return result;
    }
    
    static DefaultTransactionDefinition convertTransactionalToDefinition(Transactional transactional) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        def.setIsolationLevel(transactional.isolation().value());
        def.setPropagationBehavior(Propagation.REQUIRED.value());
        def.setTimeout(transactional.timeout());
        def.setReadOnly(false);

        return def;
    }
}
