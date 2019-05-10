# Example Use Case

A teacher needs to teach a class of 5 students, teaching them about EC2 and S3. Each student will be tasked with creating an EC2 instance and 2 S3 buckets. They will be using the AWS CLI on that instance to copy a file from one bucket to another.

### Installation

The teacher will create a new AWS account and deploy the Cirrus Formation AMI as an EC2 instance in their account. They will attach an IAM role of administrator to the instance so it can manage the member organisations on their behalf

Following the setup process of the Cirrus Formation too the teacher creates 5 new member organisations.

### Before the class - Deployment 

Now the AWS accounts are created the teacher can choose to deploy a lab to them. By choosing the `AWS EC2 and S3 lab` the following will happen in each of the 5 member accounts

- A new student user will be created with a username and random password.
- A VPC will be created in `eu-west-2` with a public and private subnet with a standard set of IP's.
- A role will be created for assigning to the EC2 instance that gives it permissions to read and write from any S3 buckets within the account
- An IAM policy will be created and attached to a group which is then attached to the student user. The IAM policy will give the minimum requirements required to complete the lesson exercises.
    - Create only `T2.Micro` instances.
    - Create instances only in `eu-west-2`.
    - Delete any instances.
    - Create and delete S3 buckets.
    
_The lab is actually just a [cloud formation script](https://aws.amazon.com/cloudformation/), these can be user contributed or developed in house if you like. Anything cloud-formation can do can be included in a lab!_

Once this process is complete the teacher is presented with a list of usernames, passwords and login URL's for each account. These can be distributed to each student when they start the class.

### In the classroom

Students are free to use the AWS account as they choose within the limits of their permissions. Students in this case will create a number of EC2 instances and S3 buckets and potentially not clean up by removing the assets when finishing the class

### After the class - Un-deployment

There is no simple delete all for an AWS Account so the un-deployment system will walk through all of the services in AWS, in all of the regions if required and scan for assets to remove. During the un-deployment process __everything__ found will be deleted.

Once the process has completed the account is considered clean and can be reused for another class, if its sat idle for any period now all the assets have been removed there should be no cost in having the account setup.
 