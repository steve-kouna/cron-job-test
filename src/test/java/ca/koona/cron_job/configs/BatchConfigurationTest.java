package ca.koona.cron_job.configs;

import ca.koona.cron_job.dao.entities.User;
import ca.koona.cron_job.dao.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchConfigurationTest {

    @InjectMocks
    private BatchConfiguration batchConfiguration;

    @Mock
    private UserRepository userRepository;

    private User activeRecentUser;
    private User activeOldUser;

    @BeforeEach
    void setUp() {
        activeRecentUser = new User("recent-user", LocalDateTime.now().minusMonths(1), "active");
        activeOldUser = new User("old-user", LocalDateTime.now().minusMonths(7), "active");
    }

    // --- Reader ---

    @Test
    void reader_shouldReturnUsersOneByOne() throws Exception {
        when(userRepository.findByStatus("active")).thenReturn(List.of(activeRecentUser, activeOldUser));

        ItemReader<User> reader = batchConfiguration.reader(userRepository);

        assertThat(reader.read()).isEqualTo(activeRecentUser);
        assertThat(reader.read()).isEqualTo(activeOldUser);
    }

    @Test
    void reader_shouldReturnNullWhenNoMoreUsers() throws Exception {
        when(userRepository.findByStatus("active")).thenReturn(List.of(activeRecentUser));

        ItemReader<User> reader = batchConfiguration.reader(userRepository);
        reader.read();

        assertThat(reader.read()).isNull();
    }

    @Test
    void reader_shouldReturnNullWhenNoActiveUsers() throws Exception {
        when(userRepository.findByStatus("active")).thenReturn(List.of());

        ItemReader<User> reader = batchConfiguration.reader(userRepository);

        assertThat(reader.read()).isNull();
    }

    // --- Processor ---

    @Test
    void processor_shouldInactivateUserWithLastLoginOlderThan6Months() throws Exception {
        ItemProcessor<User, User> processor = batchConfiguration.processor();

        User result = processor.process(activeOldUser);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("inactive");
    }

    @Test
    void processor_shouldKeepActiveUserWithRecentLogin() throws Exception {
        ItemProcessor<User, User> processor = batchConfiguration.processor();

        User result = processor.process(activeRecentUser);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("active");
    }

    @Test
    void processor_shouldInactivateUserWithLastLoginExactly6MonthsAgo() throws Exception {
        User borderUser = new User("border-user", LocalDateTime.now().minusMonths(6).minusDays(1), "active");
        ItemProcessor<User, User> processor = batchConfiguration.processor();

        User result = processor.process(borderUser);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("inactive");
    }

    // --- Writer ---

    @Test
    void writer_shouldCallSaveAllWithUsers() throws Exception {
        List<User> users = List.of(activeRecentUser, activeOldUser);
        ItemWriter<User> writer = batchConfiguration.writer(userRepository);

        writer.write(new Chunk<>(users));

        verify(userRepository, times(1)).saveAll(users);
    }

    @Test
    void writer_shouldCallSaveAllWithEmptyList() throws Exception {
        ItemWriter<User> writer = batchConfiguration.writer(userRepository);

        writer.write(new Chunk<>(List.of()));

        verify(userRepository, times(1)).saveAll(List.of());
    }
}
