#!/usr/bin/env groovy

pipeline {
    agent none

    tools {
        maven 'mvn-3.5.4'
        jdk 'jdk-8'
    }
    stages {
        stage('Cleanup') {
            sh 'mvn clean'
        }
        stage('Test') {
            sh 'mvn test'
        }
    }
}