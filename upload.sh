#!/bin/sh
curl -F "status=2" -F "notify=1" -F "notes=Some new features and fixed bugs." -F "notes_type=0" \
    -F "ipa=@app/build/outputs/apk/app-debug.apk" -H "X-HockeyAppToken: $HOCKEYAPP_TOKEN" https://rink.hockeyapp.net/api/2/apps/upload
