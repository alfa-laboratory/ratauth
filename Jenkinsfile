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
            git branch: 'bank_build', credentialsId: 'cecda320-9ccb-4827-931c-1e372124b75b', url: 'http://u_m0ln1@git/scm/auts/ratauth.git'
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
        def hasTag    = hasTag()
        def buildInfo = Artifactory.newBuildInfo()
        def currentVersion
        def server
        def rtGradle
        def gradleTasks

        //deploy
        String appUrl
        String appBundleUrl
        String depoloyRepo

        stage('anounce resolved properties') {
          echo "Try to build version: ${hasTag}"
        }

        stage('configure artifactory') {
            server      = Artifactory.server 'alfa-laboratory'
            rtGradle    = Artifactory.newGradleBuild()
            gradleTasks = ''
            if(hasTag&& env.GIT_BRANCH =~ 'release') {
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
            currentVersion = resolveLatestVersion(hasTag) //read from it. Version will be able resolve now
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

          String devManifest = '''{
  "id": "/cd/test-app",
  "cpus": 1,
  "mem": 128,
  "disk": 0,
  "instances": 1,
  "container": {
    "docker": {
      "image": "docker.moscow.alfaintra.net/nginx:latest",
      "network": "BRIDGE",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ]
    },
    "type": "DOCKER"
  },
    "env": {
      "test": "1"
  }
}'''
          deployTo(masters,  devManifest)
        }

        stage('prod:deploy') {
            input 'deploy to dev?'
            echo 'try to deploy...'
            echo 'this is stub!'
    }
  }
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

@NonCPS
def extractEurekaServers(List<String> agents) {
  for(String agent in agents) {
    try {
        def response = httpRequest customHeaders: [[name: 'app-id', value:'/sense/discovery']], acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'GET', url: "http://$agent:81/eureka/apps/SENSE-DISCOVERY-SERVER"
        def data = new groovy.json.JsonSlurper().parseText(response.content)
        def result = data.application.instance.collect { it-> 'http://'+it.hostName+':'+it.port.'$'+'/eureka/' }.join(',')
        if(result) return result
    } catch(e) { echo "error ${e.message}" }
  }

  echo "[FAILURE] agents with discovery server was`t found"
  return null
}

/**
 * return true if current commit has a tag. False otherwise
 */
def hasTag() {
  try {
    hasTag= sh(returnStdout: true, script: 'git describe --exact-match HEAD').trim()
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
def generateManifest(buildInfo) {
  echo "deployed artifacts:             ${buildInfo.deployedArtifacts.size()}"
  echo "deployed modules:               ${buildInfo.modules.size()}"
  echo "deployed publishedDependencies: ${buildInfo.publishedDependencies.size()}"
  echo "deployed buildDependencies:     ${buildInfo.buildDependencies.size()}"

  return buildInfo.deployedArtifacts
}
