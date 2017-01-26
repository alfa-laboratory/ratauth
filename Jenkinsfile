node('mesos-asdev') {
    ws('ws-'+env.BUILD_NUMBER) {
        def server = Artifactory.server 'alfa-laboratory'
        def rtGradle = Artifactory.newGradleBuild()

        stage('fetch'){
            git branch: 'bank_build', credentialsId: 'cecda320-9ccb-4827-931c-1e372124b75b', url: 'http://u_m0ln1@git/scm/auts/ratauth.git'
            sh 'ls -la'
        }

        stage('configure artifactory') {
            rtGradle.resolver server: server, repo: 'public'
            rtGradle.deployer.deployMavenDescriptors = true
            rtGradle.useWrapper = true
            rtGradle.usesPlugin = true
        }

        def gitTag = ''
        stage('resolve type') {
            try {
                gitTag = sh(returnStdout: true, script: 'git describe --exact-match HEAD').trim()
            } catch(e) { 
                echo 'Error: ' +e.toString()
            }
        }

        stage('build') {
            withCredentials([usernamePassword(credentialsId: 'cecda320-9ccb-4827-931c-1e372124b75b', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                withEnv(['GRADLE_OPTS=-Dorg.ajoberstar.grgit.auth.username=$GIT_USERNAME -Dorg.ajoberstar.grgit.auth.password=$GIT_PASSWORD']) {
                    def gradleTasks = ''
                    if(gitTag) {
                        gradleTasks = 'final'
                    } else {
                        gradleTasks = 'snapshot'
                    }
                    def buildInfo = rtGradle.run useWrapper: true, usesPlugin: true, switches: '--gradle-user-home=/home/javagit/.gradle --stacktrace --info', tasks: gradleTasks
                }
            }
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true 
        }

        stage('publish build information') {
            echo 'Publishing... DONE'
        }

        stage('deploy') {
            input 'deploy to dev?'
            echo 'try to deploy'
        }
    }
}
