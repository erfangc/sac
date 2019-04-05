package com.erfangc.sac.redis;

import com.erfangc.sac.interfaces.ImmutableGroup;
import io.lettuce.core.RedisException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.embedded.RedisServer;

public class RedisSimpleAccessControlCloseTest {

    private RedisServer redisServer;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(8080);
        redisServer.start();
    }

    @After
    public void tearDown() {
        redisServer.stop();
    }

    @Test(expected = RedisException.class)
    public void testClose() {
        final RedisSimpleAccessControl instance = new RedisSimpleAccessControl("localhost:8080");
        instance.close();
        instance.createGroup(ImmutableGroup.builder().id("g1").name("new group").build());
    }

}
