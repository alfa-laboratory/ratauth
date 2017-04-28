package ru.ratauth.inmemory.ip.providers.util;

import lombok.RequiredArgsConstructor;
import org.fluttercode.datafactory.impl.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginGenerator {

    private final DataFactory dataFactory;

    public String generateLogin() {
        return dataFactory.getLastName().toLowerCase();
    }

}
