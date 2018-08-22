pipeline {
    agent any

    tools {
     maven 'mvn-3.5.4'
     jdk 'jdk-8'
    }

    stages {
     stage('Cleanup') {
         steps {
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