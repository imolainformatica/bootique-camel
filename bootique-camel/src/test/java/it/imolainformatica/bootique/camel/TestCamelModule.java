package it.imolainformatica.bootique.camel;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class TestCamelModule {

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testAutoLoadable(CamelModuleProvider.class);
    }
}
