curl -F "status=2" -F "notify=1" \
-F "notes=Branch: $TRAVIS_BRANCH | Pull Request: $TRAVIS_PULL_REQUEST_BRANCH | Last Commit: $TRAVIS_COMMIT $TRAVIS_COMMIT_MESSAGE | Travis Build Number: $TRAVIS_BUILD_NUMBER" \
-F "notes_type=0" -F "ipa=@app/build/outputs/apk/travis/app-travis.apk" \
-H "X-HockeyAppToken: $HOCKEYAPP_TOKEN" "https://rink.hockeyapp.net/api/2/apps/upload"
