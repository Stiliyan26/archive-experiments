#!/bin/bash

# Variables
REPOSITORY_URL="http://37.157.143.162:12080/releases"
GROUP_ID="bg.latona"
ARTIFACT_ID="santa"

# Fetch the latest version
LATEST_VERSION=$(curl -s "${REPOSITORY_URL}/${GROUP_ID//.//}/${ARTIFACT_ID}/maven-metadata.xml" -H "Authorization: xBasic bmVwYWxSZWFkZXI6NEVvQnBObE5VbC9kenZNRnlyVmplbjl0bHlkTVNvTjQ3Um9Lenl6MlJIL0Rsb0d4eTVUSUJvR01oZjJFaXJkcQ==" | grep -oPm1 "(?<=<release>)[^<]+")
echo "Latest version: $LATEST_VERSION"

# Download the latest artifact
ARTIFACT_URL="${REPOSITORY_URL}/${GROUP_ID//.//}/${ARTIFACT_ID}/${LATEST_VERSION}/${ARTIFACT_ID}-${LATEST_VERSION}.jar"
echo "Download from URL: $ARTIFACT_URL"
curl  -H "Authorization: xBasic bmVwYWxSZWFkZXI6NEVvQnBObE5VbC9kenZNRnlyVmplbjl0bHlkTVNvTjQ3Um9Lenl6MlJIL0Rsb0d4eTVUSUJvR01oZjJFaXJkcQ==" -o santa.jar $ARTIFACT_URL