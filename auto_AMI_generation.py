import json
import boto3
from datetime import datetime

def lambda_handler(event, context):
    # Retrieve the list of instances in the ASG
    autoscaling = boto3.client('autoscaling')
    response = autoscaling.describe_auto_scaling_groups(AutoScalingGroupNames=['eks-EKS-node-28c5ae11-6c8d-e4dc-5b14-54aec599cab6'])
    instances = response['AutoScalingGroups'][0]['Instances']

    # Create AMI for the first running instance in the ASG
    if instances:
        instance_id = instances[0]['InstanceId']
        ami_name = f'AMI for instance pocRajnish {datetime.now().strftime("%Y-%m-%d-%H-%M-%S")}'
        response = create_ami(instance_id, ami_name)
        ami_id = response['ImageId']
        response_message = {
            "status": "AMI created",
            "AMI_ID": ami_id
        }
        print(f'Created AMI: {ami_id}')
        return {
            "statusCode": 200,
            "body": json.dumps(response_message)
        }
    else:
        print('No running instances found in the ASG')
        return {
            "statusCode": 200,
            "body": "No running instances found in the ASG"
        }

def create_ami(instance_id, ami_name):
    # Create AMI with a unique name
    ec2 = boto3.client('ec2')
    response = ec2.create_image(
        InstanceId=instance_id,
        Name=ami_name,
        Description='Automatically created AMI',
        NoReboot=True
    )
    ami_id = response['ImageId']
    
    # Add the name tag to the AMI
    ec2.create_tags(
        Resources=[ami_id],
        Tags=[
            {
                'Key': 'Name',
                'Value': ami_name
            }
        ]
    )
    
    return response
