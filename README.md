:warning: *Use of this software is subject to important terms and conditions as set forth in the License file* :warning:

[![Build Status](https://travis-ci.org/zendesk/sdk_demo_app_android.svg?branch=master)](https://travis-ci.org/zendesk/sdk_demo_app_android)

# Zendesk Mobile SDK Android Demo app

The "Remember the Date" demo app demonstrates how to use the Zendesk Mobile SDK to build native support into your mobile application.

##### The following Zendesk Mobile SDK features are demonstrated in the "Remember The Date" app.

* Create/Submit a Zendesk ticket request
* View an existing Zendesk ticket request
* Access and search your Zendesk Help Center Self Service content
* Accessing the Zendesk "Rate my app" feature

Please submit bug reports to [Zendesk](https://rememberthedate.zendesk.com/requests/new). Pull requests are welcome.

### Licence:

By downloading or using the Zendesk Mobile SDK, You agree to the Zendesk Master
Subscription Agreement https://www.zendesk.com/company/customers-partners/#master-subscription-agreement and Application Developer and API License
Agreement https://www.zendesk.com/company/customers-partners/#application-developer-api-license-agreement and
acknowledge that such terms govern Your use of and access to the Mobile SDK.

## Releasing

When you tag a build it will be released automatically. When you are happy with the codebase you can make a tag and push it to GitHub. You will need to know the commit sha that you want to tag and the version number you want to tag it as.

The commit sha will usually be the latest state of master. You can go to the [master branch](https://github.com/zendesk/sdk_demo_app_android/tree/master) and copy the commit sha [as shown here](https://raw.githubusercontent.com/zendesk/sdk_demo_app_android/master/docs/images/commit_sha.png).

In this example we will be releasing version 1.2.1. Please update the 1.2.1 below with the actual version that you are releasing.

```bash
git fetch
git tag -a "v1.2.1" <commit sha> -m "Release v1.2.1"
git push origin --tags
```

After you push the tag a build will be started on the continuous integration server. You can [check the status](https://travis-ci.org/zendesk/sdk_demo_app_android/builds) of the build, which should take less than 10 minutes.

When the build is finished the debug and release builds of the app will be published to the [releases section of the repository](https://github.com/zendesk/sdk_demo_app_android/releases/latest). `app-debug.apk` is a debug build that can be used for testing. `app-release.apk` is a signed release build that should be preferred.
