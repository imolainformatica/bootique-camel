package it.imolainformatica.bootique.camel;

import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;

public class CamelModuleProvider implements BQModuleProvider {

    @Override
    public BQModule module() {
        return new CamelModule();
    }

}
