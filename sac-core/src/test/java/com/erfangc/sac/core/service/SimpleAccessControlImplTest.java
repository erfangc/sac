package com.erfangc.sac.core.service;

import com.erfangc.sac.backend.tests.BackendTestBase;
import org.junit.Before;

public class SimpleAccessControlImplTest extends BackendTestBase {

    @Before
    public void setUp() {
        sac = InMemorySimpleAccessControl.getInstance();
        initializePolicyBackendStates();
    }

}