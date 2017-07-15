# This Day In DMB

This is an [Amazon Alexa skill](https://developer.amazon.com/alexa) which retrieves a historical Dave Matthews Band setlist from [DMBAlmanac](http://dmbalmanac.com).

# Deploying

1. Create a fat jar with `gradle shadowJar`
1. Upload to AWS Lambda via `aws lambda update-function-code --function-name=${FUNCTION_NAME} --zip-file=${JAR_FILE}`
