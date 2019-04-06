package com.erfangc.sac.core.service;

import com.erfangc.sac.backend.tests.StatefulTestBase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class InMemorySimpleAccessControlTest extends StatefulTestBase {

    @Before
    public void setUp() {
        sac = InMemorySimpleAccessControl.getInstance();
    }

    @Test
    public void getInstance() {
        assertSame(sac, InMemorySimpleAccessControl.getInstance());
    }

}