pipeline {
    agent any

    tools {
        maven 'mvn-3.6.0'
        jdk 'jdk-8'
    }

    stages {
        stage('Cleanup') {
            steps {
                cleanWs()
                sh 'mvn clean'
            }
        }

        stage('Test') {
            steps {
              sh 'mvn test'
            }
        }
    }
}