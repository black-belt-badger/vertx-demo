# AWS SSM and CloudWatch Setup

## [Back](../README.md)

## Create a key pair

This key pair can be reused.

## Create instance

* *Image*: latest *Debian*
* *IMDSv2*: *required*

## Install Docker

* `./bash/docker/setup-docker-repo.sh`
* `./bash/docker/install-docker.sh`

## Install AWS Systems Manager Agent (SSM Agent)

* `./bash/ssm-agent/download-ssm-agent.sh`
* `./bash/ssm-agent/install-ssm-agent.sh`

## Create IAM role

* *IAM* → *Roles* → *Create role*
    * *Trusted entity type*: AWS service
    * *Use case*: EC2
    * *Permissions policies*:
        * *AmazonSSMManagedInstanceCore*
        * *CloudWatchAgentServerPolicy*
    * *Name*: **newly-created-role**

## Assign a newly created role to an instance

* Go to an instance details page
* *Actions* → *Security* → *Modify IAM role*
* *Choose*: **newly-created-role**
* *Update IAM role*
* It takes some time for changes to take effect

## Check the status of SSM Agent

* `./bash/cloud-watch/check-ssm-agent-status.sh`

## Install Cloud Watch agent

* `./bash/cloud-watch/download-cloud-watch-agent.sh`
* `./bash/cloud-watch/install-cloud-watch-agent.sh`

## Configure Cloud Watch agent

* Go to an instance details page
* *Monitoring* → *Configure CloudWatch agent*
* Click through six steps of the wizard

## Configure Cloud Watch

* *CloudWatch* → *Log groups* → *Create log group*
    * *Log group name*: **training-log-group**
    * *Create*
* Go to the newly created log group details page
    * *Create log stream*
    * *Log stream name*: **training-log-stream**
    * *Create*

## Confirm that logs can be written

* Create file `events.json`, adjusting timestamp to a relatively recent
    * [https://currentmillis.com/](https://currentmillis.com/)
    * Example command is stored in `./bash/aws-instance-setup/cloud-watch/confirm-logs-can-be-written.sh`
    * It should emit `JSON` with `nextSequenceToken` element
* *CloudWatch* -> *Log groups* -> **training-log-group** -> **training-log-stream**

## Configure Cloud Watch to collect data from file

* Create configuration

  `./bash/aws-instance-setup/cloud-watch/config.json`

* Append

  `sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a append-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/bin/config.json -s`

* Remove

  `sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a remove-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/bin/config.json -s`

## Install and set up AWS mobile app

No complications there.

