package net.dloud.platform.center.conf;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.parse.utils.SourceGet;
import org.jdbi.v3.core.Jdbi;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author QuDasheng
 * @create 2018-09-13 09:50
 **/
@Slf4j
@Configuration
@EnableTransactionManagement
public class SourceConfig {
    @Autowired
    private Environment env;

    @Bean
    @Primary
    public DataSource dataSource() {
        return SourceGet.getHikariSource(
                env.getProperty("mysql.url"), env.getProperty("mysql.username"),
                env.getProperty("mysql.password"), env.getProperty("mysql.drive-name"));
    }

    @Bean("pubDataSource")
    public DataSource pubDataSource() {
        return SourceGet.getHikariSource(
                env.getProperty("mysql.url"), env.getProperty("mysql.username"),
                env.getProperty("mysql.password"), env.getProperty("mysql.drive-name"));
    }

    @Bean
    public RedissonClient redissonClient() {
        return SourceGet.getRedissonClient(
                env.getProperty("redis.url"), env.getProperty("redis.password"));
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public Jdbi createHandle(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}
