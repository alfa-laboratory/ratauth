# app

beautiful app service

## Links for read:
* [OpenID spec](http://openid.net/specs/openid-connect-core-1_0.html)
* [Uaa project API](https://github.com/cloudfoundry/uaa/blob/master/docs/UAA-APIs.rst#client-obtains-token-post-oauth-token)
* [OpenId descriptuion by ForgeRock](https://backstage.forgerock.com/#!/docs/openam/12.0.0/admin-guide/chap-openid-connect)
* [Connect2Id docs](http://connect2id.com/learn/openid-connect)

## Run

Main Spring Boot Application: `RatAuthApplication`. Run with `local` profiles


```bash
java -jar server/build/libs/server.jar --spring.profiles.active=local --ratpack.baseDir='/' --ratpack.templatesPath="BOOT-INF/classes/templates"
```

If you want to run in IDEA, you will add `idea` active profile