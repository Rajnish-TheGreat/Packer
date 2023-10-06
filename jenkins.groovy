pipeline {
    agent any

    environment {
        PACKER_AMI_ID = '' // Create an environment variable to store the AMI ID
    }

    stages {
        stage('Create AMI with all configurations and changes') {
            steps {
                cleanWs()
                // Clone your Packer repository or use a Packer workspace
                git url: 'https://github.com/Rajnish-TheGreat/Packer.git', branch: 'main'
                

                // Install Packer (if not already installed)
                // sh 'wget https://releases.hashicorp.com/packer/1.7.4/packer_1.7.4_linux_amd64.zip'
                // sh 'unzip packer_1.7.4_linux_amd64.zip'

                // Build the AMI using Packer and capture the AMI ID
                sh 'rm -f test.pkr.hcl'
                sh 'ls'
                sh 'packer init .'

                // Capture the AMI ID from the Packer output
                script {
                    def packerOutput = sh(script: 'packer build -machine-readable -var-file=variables.json testAnsible.pkr.hcl | tee packer-output.txt', returnStdout: true).trim()
                    // Extract AMI ID using regular expression
                    PACKER_AMI_ID = sh(script: '''#!/bin/bash
                        grep 'AMIs were created' packer-output.txt | awk '{print \$NF}' | head -n 1 | tr -d '[:space:]' | sed 's/\\\\n\$//'
                    ''',returnStdout: true).trim()
                    sh "echo ${PACKER_AMI_ID}"
                }
				
            }
        }

        stage('Provision Infrastructure') {
            steps {
                // Clone your Terraform repository or use a Terraform workspace
                // git 'https://github.com/Rajnish-TheGreat/terraform_main_code.git'
                
                // Install and initialize Terraform
                sh 'terraform init'
                echo "Packer AMI ID: ${PACKER_AMI_ID}"
                // Set Terraform variable to use the AMI ID from Packer
                sh "terraform apply -auto-approve -var 'ami_id=${PACKER_AMI_ID}'"
            }
        }
    }
}
