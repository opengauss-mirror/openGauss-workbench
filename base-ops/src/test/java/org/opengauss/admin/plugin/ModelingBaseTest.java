package org.opengauss.admin.plugin;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class ModelingBaseTest {

    @Before
    public void before(){
        MockitoAnnotations.openMocks(this);
        System.out.println("start test:" + this.getClass());
    }

    @After
    public void after(){
        System.out.println("start end:" + this.getClass());
    }

}
