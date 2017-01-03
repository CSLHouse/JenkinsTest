node {
    try {
        stage 'checkout'
        checkout scm

        stage 'build & deploy'
        timeout(time: 15, unit: 'MINUTES') {
            sh "./gradlew clean debugDeploy"
        }
    } catch (e) {
        // If there was an exception thrown, the build failed
        currentBuild.result = "FAILED"
        throw e
    } finally {
        // Success or failure, always send notifications
        notifyMail(currentBuild.result)
    }
}

