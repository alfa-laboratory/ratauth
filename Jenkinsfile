node('mesos-asdev') {
  ws('ws-'+env.BUILD_NUMBER) {
    echo "BRANCH_NAME:                ${env.BRANCH_NAME}"
    echo "CHANGE_ID:                  ${env.CHANGE_ID}"
    echo "CHANGE_URL:                 ${env.CHANGE_URL}"
    echo "CHANGE_TITLE:               ${env.CHANGE_TITLE}"
    echo "CHANGE_AUTHOR:              ${env.CHANGE_AUTHOR}"
    echo "CHANGE_AUTHOR_DISPLAY_NAME: ${env.CHANGE_AUTHOR_DISPLAY_NAME}"
    echo "CHANGE_AUTHOR_EMAIL:        ${env.CHANGE_AUTHOR_EMAIL}"
    echo "CHANGE_TARGET:              ${env.CHANGE_TARGET}"

    stage('fetch'){
      checkout scm
      sh 'ls -la'
    }

        //black magic with Gradle init script. Need to add --init-script=./.gradle/init.gradle to gradle switches
    stage('gradle configure') {
      sh 'mkdir .gradle/'
      writeFile file:'./.gradle/init.gradle', text: '''rootProject {
        afterEvaluate {
          def file = file("$buildDir/project-version")
          if(!it.buildDir.exists()){
            it.buildDir.mkdir()
          }
          file << "$version"
        }
      }'''.stripIndent().stripMargin()
    }

    //predefined
    def appGroup       = 'ru/ratauth/server'

    //build
    def isRelease    = hasTag()
    def buildInfo = Artifactory.newBuildInfo()
    def currentVersion
    def server
    def rtGradle
    def gradleTasks

    //deploy
    String appId = '/oidc/auth-api'
    String depoloyRepo
    String appUrl

    stage('anounce resolved properties') {
      echo "Try to build version: ${isRelease}"
    }

    stage('configure artifactory') {
      server      = Artifactory.server 'alfa-laboratory'
      rtGradle    = Artifactory.newGradleBuild()
      gradleTasks = ''
      if(isRelease && env.GIT_BRANCH =~ 'release') {
        gradleTasks = 'final -Prelease.useLastTag=true'
        deployRepo  = 'releases'
      } else {
        deployRepo  = 'snapshots'
        gradleTasks = 'snapshot -Prelease.scope=patch'
        buildInfo.retention maxDays: 10, deleteBuildArtifacts: true
      }
      rtGradle.deployer repo: deployRepo, server: server
      rtGradle.deployer.deployMavenDescriptors = true
      rtGradle.resolver repo:'public', server: server
      rtGradle.useWrapper = true
    }

    stage('build') {
      withCredentials([usernamePassword(credentialsId: 'cecda320-9ccb-4827-931c-1e372124b75b', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        withEnv(['GRADLE_OPTS=-Dorg.ajoberstar.grgit.auth.username=$GIT_USERNAME -Dorg.ajoberstar.grgit.auth.password=$GIT_PASSWORD']) {
          rtGradle.run switches: '--gradle-user-home=/home/javagit/.gradle --init-script=./.gradle/init.gradle --stacktrace --info', tasks: gradleTasks, buildInfo: buildInfo
          server.publishBuildInfo buildInfo
        }
      }
      archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true
      currentVersion = resolveLatestVersion(isRelease) //read from it. Version will be able resolve now
      appUrl         = "${server.url}/${deployRepo}/${appGroup}/${currentVersion}/server-${currentVersion}.jar"
    }

    stage('Aggregate test results') {
      junit allowEmptyResults: true, testResults: 'server/build/test-results/test/*.xml'
      echo 'Test results ... DONE'
    }

    if(!appUrl) {
      currentBuild.result = 'FAILURE'
      echo "[FAILURE] agents with discovery server not found"
      return
    }

    stage('dev:deploy') {
      echo "Deploy app: ${appUrl}"
      input 'deploy to dev?'
      def agents = ['asappdev1','asnodedev2','asnodedev3','asnodedev1','asappdev2','asappdev3']
      def masters = ['http://asorcdev1:8080/v2/apps','http://asorcdev2:8080/v2/apps','http://asorcdev3:8080/v2/apps']
      def eurekaServers = extractEurekaServers(agents)
      if(!eurekaServers) return

      def template = readFile("./manifest.json.template")
      echo """
              template: $template
        currentVersion: $currentVersion
        eureka servers: $eurekaServers
                app id: $appId
        """.stripIndent().stripMargin()

      String devManifest = generateManifest(template, currentVersion,eurekaServers, appId)
      echo "manifest: $devManifest"
      def appIsExist = appIsExistInCluster(appId, masters)
      if(appIsExist) {
        echo 'with appid'
        deployTo(masters,  devManifest, appId)
      } else {
        echo 'without appid'
        deployTo(masters,  devManifest, '')
      }
    }

    if(isRelease) {
      stage('prod:deploy') {
          input 'deploy to dev?'
          echo 'try to deploy...'
          echo 'this is stub!'
      }
    }
  }
}

@NonCPS
def deployTo(List<String> agents, String manifest, String appId) {
  for(agent in agents) {
    try {
      String marathonDeploymentEndpoint = agent + appId
      String httpMethod = 'PUT'

      /* def appIsExist = appIsExistInCluster(appId, agents) */
      if(!appId) {
        echo 'change deployment url'
        marathonDeploymentEndpoint = agent
        httpMethod = 'POST'
      }

      echo "try to deploy [${marathonDeploymentEndpoint}"
      def response = httpRequest acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: httpMethod,
        requestBody: manifest,
        url: marathonDeploymentEndpoint

      echo "request was sent. status: ${response.status}"
      if(response.status == 201) {
        echo 'New app has been deployed'
        return
      }
    } catch(e) {
      echo "server ${agent} is unreachable, ${e.message}"
    }
  }
  echo '[FAILURE] marathon not found'
}

def appIsExistInCluster(String appId, List<String> agents) {
  for(agent in agents) {
    try {
      def checkAppResponse = httpRequest httpMode: 'GET',
          validResponseCodes: '200,302,404,409',
          acceptType: 'APPLICATION_JSON',
          contentType: 'APPLICATION_JSON',
          url: agent + appId

      if(checkAppResponse.status == 404) {
        echo 'app does not exists'
        return false
      }
    } catch(e) { echo "Error during check ${appId} existing. Message: ${e.message}" }
  }
  echo 'app is exists'
  return true
}

@NonCPS
def deployTo(List<String> agents, String manifest) {
  for(agent in agents) {
    def response = httpRequest acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: manifest, url: agent
    if(response.status == 201) {
      echo 'New app has been deployed'
      return
    }
  }
  echo '[FAILURE] marathon not found'
}

def extractEurekaServers(List<String> agents) {
  for(String agent in agents) {
    try {
        def response = httpRequest customHeaders: [[name: 'app-id', value:'/sense/discovery']], acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'GET', url: "http://$agent:81/eureka/apps/SENSE-DISCOVERY-SERVER"
        def result = extractHosts(response.content)
        if(result) return result
    } catch(e) { echo "error ${e.message}" }
  }

  echo "[FAILURE] agents with discovery server was`t found"
  return null
}
@NonCPS
def extractHosts(String content) {
    def data = new groovy.json.JsonSlurper().parseText(content)
    def result = data.application.instance.collect { it-> 'http://'+it.hostName+':'+it.port.'$'+'/eureka/' }.join(',')
}

/**
 * return true if current commit has a tag. False otherwise
 */
def hasTag() {
  try {
    hasTag= sh(returnStdout: true, script: 'git describe --exact-match HEAD --tags').trim()
    if(hasTag) {
      return true
    } else {
      return false
    }
  } catch(e) {
    echo 'Error: ' +e.toString()
    return false
  }
}

/**
 * return latest version or empty string
 */
def resolveLatestVersion(isRelease) {
  try {
    versionFromFile= readFile('build/project-version')
    if(versionFromFile) return versionFromFile
    version = sh(returnStdout: true, script: 'git describe --tags --abbrev=0').trim()
    if(!isRelease){
      version = version+'-SNAPSHOT'
    }
    return version.replace('v','')
  } catch(e) {
    echo 'Error: ' +e.toString()
  }
  return ''
}

@NonCPS
def generateManifest(String template, String version, String eurekaServers, String appId) {
  def engine = new groovy.text.SimpleTemplateEngine(false)
  return engine.createTemplate(template)
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
}
