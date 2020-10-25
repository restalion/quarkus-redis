package com.sourcesense.quarkus.redis.runtime;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

@Recorder
public class RedisClientTemplate {

    public RuntimeValue<JedisPool> configureJedisPool(BeanContainer container, ShutdownContext shutdown) {
        RedisClientProducer producer = container.instance(RedisClientProducer.class);
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        long maxWaitMillis = ConfigProvider.getConfig().getOptionalValue("quarkus.redis.maxWaitMillis", Long.class).orElse(5000l);
        int maxTotal = ConfigProvider.getConfig().getOptionalValue("quarkus.redis.maxTotal", Integer.class).orElse(15);
        String configUri = ConfigProvider.getConfig().getOptionalValue("quarkus.redis.uri", String.class).orElse("redis://localhost:6379/2");

        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setJmxEnabled(false);

        URI uri = URI.create(configUri);
        JedisPool pool = new JedisPool(poolConfig, uri);
        producer.setJedisPool(pool);

        shutdown.addShutdownTask(() -> pool.close());

        return new RuntimeValue<>(pool);
    }

    public RuntimeValue<ShardedJedisPool> configureShardedJedisPool(BeanContainer container, ShutdownContext shutdown) {
        RedisClientProducer producer = container.instance(RedisClientProducer.class);
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        long maxWaitMillis = ConfigProvider.getConfig().getOptionalValue("quarkus.redis.maxWaitMillis", Long.class).orElse(5000l);
        int maxTotal = ConfigProvider.getConfig().getOptionalValue("quarkus.redis.maxTotal", Integer.class).orElse(15);
        String configUri = ConfigProvider.getConfig().getOptionalValue("quarkus.redis.uri", String.class).orElse("redis://localhost:6379/2");

        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setJmxEnabled(false);
        
        List<JedisShardInfo> shards = Arrays.stream(configUri.split(",")).map(s -> {
            URI uri = URI.create(s);
            return new JedisShardInfo(uri);
        }).collect(Collectors.toList());

        ShardedJedisPool pool = new ShardedJedisPool(poolConfig, shards);
        producer.setShardedJedisPool(pool);

        shutdown.addShutdownTask(() -> pool.close());

        return new RuntimeValue<>(pool);

    }
}
