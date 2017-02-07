#!/usr/bin/env groovy

import groovy.text.SimpleTemplateEngine

import java.nio.file.Files
import java.nio.file.Paths

def engine = new SimpleTemplateEngine(false)

def appId         = '/oidc/auth-api'
def version       = '0.0.1'
def eurekaServers = 'http:eureka'

println engine.createTemplate(new File("./manifest.json.template"))
    .make([
          manifest : [
        oidc_auth_api      : [
          app_id           : appId,
          mem              : '512',               // docker container memory limit
          xmx              : '768',               // java xmx
          version          : version,             // application version, need for resolve app url
          spring_profiles  : 'dev,debug,cloud',   // profiles for spring app
          cpu              : '1',                 // mesos cpu resource
          version_type     : 'snapshots',         // artifact type
          config_branch    : 'test',              // config branch
          instances        : '1',                 // run instances
          logging_profile  : 'dev',               // logging profile
          ha_group         : 'mobile',            // marathon lb ha group
          ha_path          : '/nonfinancial/api', // marathon lb path for route to app
          ha_vhost         : 'mobile',            // marathon lb vhost for route to app
          deployment_group : 'nonfinancial',      // marathon lb deployment group
          eureka_servers   : eurekaServers        // eureka servers for register
        ],
        logger : [
          version          : '1.3.2',             // logging config version
          host             : 'localhost',         // sink host
          port             : '514',               // sink port
          tags             : 'forwarding,json',   // rsyslog tags for filtering
          host_env_var     : 'HOST',              // discover hostname from this env var
        ]
      ]
]).toString()
