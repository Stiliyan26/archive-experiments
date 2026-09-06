#!/bin/bash

# Variables
REPOSITORY_URL="http://37.157.143.162:12080/releases"
GROUP_ID="bg.latona"
ARTIFACT_ID="santa_evn_selfie"

# Fetch the latest version
LATEST_VERSION=$(curl -s "${REPOSITORY_URL}/${GROUP_ID//.//}/${ARTIFACT_ID}/maven-metadata.xml" -H "Authorization: xBasic YWRtaW46Y21MaExJWlFLMjhUUkEyNUJ4RnVHQ1dEVlNUSFZITU5yb3pIUmNzYWdvNEc4QW5oZzVJMm5TOHpDREgwaU5rZA==" | grep -oPm1 "(?<=<release>)[^<]+")
echo "Latest version: $LATEST_VERSION"

# Download the latest artifact
ARTIFACT_URL="${REPOSITORY_URL}/${GROUP_ID//.//}/${ARTIFACT_ID}/${LATEST_VERSION}/${ARTIFACT_ID}-${LATEST_VERSION}.jar"
echo "Download from URL: $ARTIFACT_URL"
curl  -H "Authorization: xBasic YWRtaW46Y21MaExJWlFLMjhUUkEyNUJ4RnVHQ1dEVlNUSFZITU5yb3pIUmNzYWdvNEc4QW5oZzVJMm5TOHpDREgwaU5rZA==" -o santa_evn_selfie.jar $ARTIFACT_URL