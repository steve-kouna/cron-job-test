package ca.koona.cron_job.configs;

import ca.koona.cron_job.dao.entities.User;
import ca.koona.cron_job.dao.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BatchConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Bean
    public Job inactivateUserJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("inactivateUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<User> reader,
                      ItemProcessor<User, User> processor,
                      ItemWriter<User> writer) {
        log.info("inactivateUserJob - step 1");

        return new StepBuilder("Step1", jobRepository)
                .<User, User>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<User> reader(UserRepository userRepository) {
        log.info("inactivateUsersJob - search user");

        return new ItemReader<User>() {
            List<User> activeUsers = userRepository.findByStatus("active");

            @Override
            public User read() {
                if (!activeUsers.isEmpty()) {
                    return activeUsers.removeFirst();
                }
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<User, User> processor() {
        return user-> {
            if (user.getLastLoginDate().isBefore(LocalDateTime.now().minusMonths(6))) {
                log.info("user {} is inactive because last login date is {} ", user.getUsername(), user.getLastLoginDate());
                user.setStatus("inactive");
            }

            return user;
        };
    }

    @Bean
    public ItemWriter<User> writer(UserRepository userRepository) {
        log.info("inactivateUserJob - save users");
        return userRepository::saveAll;
    }
}
