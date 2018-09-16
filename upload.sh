curl -F "status=2" -F "notify=1" \
-F "notes=Branch: $TRAVIS_BRANCH\nLast Commit: $TRAVIS_COMMIT $TRAVIS_COMMIT_MESSAGE\nPull Request: $TRAVIS_PULL_REQUEST_BRANCH - Origin($TRAVIS_PULL_REQUEST_BRANCH)\nTravis Build Number: $TRAVIS_BUILD_NUMBER" \
-F "notes_type=0" -F "ipa=@app/build/outputs/apk/debug/app-debug.apk" \
-H "X-HockeyAppToken: $HOCKEYAPP_TOKEN" "https://rink.hockeyapp.net/api/2/apps/upload"
