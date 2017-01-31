#!/usr/bin/env groovy

import groovy.text.SimpleTemplateEngine

import java.nio.file.Files
import java.nio.file.Paths

def engine = new SimpleTemplateEngine(false)

println engine.createTemplate(new File("./manifest.json.template"))
    .make([
    manifest:            [
      oidc_auth_api:     [
        mem:             '512',
        xmx:             '768',
        version:         'tratatatatversion',
        spring_profiles: 'dev,cloud',
        cpu:             '1',
        version_type:    'snapshots',
        config_branch:   'test',
        instances:       '2',
        logging_profile: 'dev',
      ],
      logger: [
        version               :'1.3.2',
        host                  :'localhost',
        port                  :'514',
        tags                  :'forwarding,json',
        host_env_var          :'HOST',
      ]
    ]
]).toString()
