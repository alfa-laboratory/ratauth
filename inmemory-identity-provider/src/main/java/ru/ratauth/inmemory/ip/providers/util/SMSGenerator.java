package ru.ratauth.inmemory.ip.providers.util;

import lombok.RequiredArgsConstructor;
import org.fluttercode.datafactory.impl.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SMSGenerator {

    private static final int MIN_SMS_CODE_VALUE = 1000;
    private static final int MAX_SMS_CODE_VALUE = 9999;
    private final DataFactory dataFactory;

    public int generateSMSCode() {
        return dataFactory.getNumberBetween(MIN_SMS_CODE_VALUE, MAX_SMS_CODE_VALUE);
    }

}