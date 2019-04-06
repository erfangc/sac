package com.erfangc.sac.core.service;

import com.erfangc.sac.backend.tests.BackendTestBase;
import com.erfangc.sac.core.backend.inmemory.InMemoryBackend;
import org.junit.Before;

public class SimpleAccessControlImplTest extends BackendTestBase {

    @Before
    public void setUp() {
        sac = new SimpleAccessControlImpl(new InMemoryBackend());
        initializePolicyBackendStates();
    }

}