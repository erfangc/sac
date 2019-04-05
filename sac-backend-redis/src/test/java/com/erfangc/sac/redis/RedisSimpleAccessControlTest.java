package com.erfangc.sac.redis;

import com.erfangc.sac.backend.tests.BackendTestBase;
import org.junit.After;
import org.junit.Before;
import redis.embedded.RedisServer;

import java.io.IOException;

public class RedisSimpleAccessControlTest extends BackendTestBase {
    private RedisServer redisServer;

    @After
    public void tearDown() {
        redisServer.stop();
        System.clearProperty("sac.redis.endpoint");
    }

    @Before
    public void setUp() throws IOException {
        redisServer = new RedisServer(8080);
        redisServer.start();
        System.setProperty("sac.redis.endpoint", "localhost:8080");
        sac = RedisSimpleAccessControl.getInstance();
        initializePolicyBackendStates();
    }

}