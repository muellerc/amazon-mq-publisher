# Maven build steps
`mvn clean package`

## export the AWS account ID into an env variable for later use
`export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query 'Account' --output text)`

## export your AWS default region into an env variable for later use
`export AWS_REGION=$(aws --profile default configure get region)`

## login to Amazon ECR to be able to upload the image later
`$(aws ecr get-login --no-include-email)`  

## optionally, create the Amazon ECR repo first, if it doesn't exist already
`aws ecr create-repository --repository-name amazon-mq-publisher` 

# build the Docker image
`docker build -t amazon-mq-publisher .`  

## tag the Docker image
`docker build --tag $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/amazon-mq-publisher:latest .`  

## push the Docker image to Amazon ECR
`docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/amazon-mq-publisher:latest`  

## create the Amazon CloudFormation stack
`sam deploy --guided  --stack-name amazon-mq-publisher  --capabilities CAPABILITY_IAM`  