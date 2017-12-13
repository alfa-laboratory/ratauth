package ru.ratauth.server.utils

import spock.lang.Specification

class RedirectUtilsTest extends Specification {

    def 'Redirect uri #uri generated from #parameters could be correct '() {
        expect: 'Generated redirect uri is correct'
        RedirectUtils.createRedirectURI(url, parameters) == redirectURI

        where:
        url                             | parameters    | redirectURI
        "domain.mine.ru"                | ''            | "domain.mine.ru"
        "https://domain.mine.ru"        | ''            | "https://domain.mine.ru"
        "https://domain.mine.ru"        | 'q1=p1'       | "https://domain.mine.ru?q1=p1"
        "https://domain.mine.ru?"       | 'q1=p1'       | "https://domain.mine.ru?q1=p1"
        "https://domain.mine.ru?q2=p2"  | 'q1=p1'       | "https://domain.mine.ru?q2=p2&q1=p1"
        "https://domain.mine.ru?q2=p2&" | 'q1=p1'       | "https://domain.mine.ru?q2=p2&q1=p1"
        "https://domain.mine.ru"        | 'q1=p1&q2=p2' | "https://domain.mine.ru?q1=p1&q2=p2"
    }
}
