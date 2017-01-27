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

        //predefined
        def appGroup       = 'ru/ratauth/server'

        //build
        def hasTag= hasTag()
        def currentVersion = resolveLatestVersion(hasTag)
        def buildInfo      = Artifactory.newBuildInfo()
        def server
        def rtGradle
        def gradleTasks

        //deploy
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
            rtGradle.deployer repo: deployRepo,  server: server
            rtGradle.deployer.deployMavenDescriptors = true
            rtGradle.resolver repo:'public', server: server
            rtGradle.useWrapper = true
            rtGradle.usesPlugin = true
        }

        stage('build') {
            withCredentials([usernamePassword(credentialsId: 'cecda320-9ccb-4827-931c-1e372124b75b', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                withEnv(['GRADLE_OPTS=-Dorg.ajoberstar.grgit.auth.username=$GIT_USERNAME -Dorg.ajoberstar.grgit.auth.password=$GIT_PASSWORD']) {
                    rtGradle.run usesPlugin: true, switches: '--gradle-user-home=/home/javagit/.gradle --stacktrace --info', tasks: gradleTasks, buildInfo: buildInfo
                    server.publishBuildInfo buildInfo
                }
            }
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true 
        }

        stage('Aggregate test results') {
            junit allowEmptyResults: true, testResults: 'server/build/test-results/test/*.xml'
            echo 'Test results ... DONE'
        }

        stage('dev:deploy') {
            input 'deploy to dev?'
            echo 'try to deploy'
            echo 'this is stub!'
            echo "Generate manifest for app: ${server.url}/${deployRepo}/${appGroup}/${currentVersion}/server-${currentVersion}.jar"
        }

        stage('prod:deploy') {
            input 'deploy to dev?'
            echo 'try to deploy...'
            echo 'this is stub!'
    }
  }
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
