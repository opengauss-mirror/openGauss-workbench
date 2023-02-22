package org.opengauss.admin.plugin;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class BaseTest {

    @Before
    public void before(){
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void after(){
    }

}
