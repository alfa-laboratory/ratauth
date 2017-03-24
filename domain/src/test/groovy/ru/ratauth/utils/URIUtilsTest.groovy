package ru.ratauth.utils

import org.junit.Test

/**
 * Created by ruslanmikhalev on 24/03/17.
 */
class URIUtilsTest {

    @Test
    public void compareHosts() {
        String url = "https://testsense.alfabank.ru/test/mobile-web/web/repayment/early/0003"
        List<String> list = [ "https://click.alfabank.ru", "https://testsense.alfabank.ru", "https://dev.alfabank.ru"];
        assert URIUtils.compareHosts(url, list);
    }

}
