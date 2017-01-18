## Android SDK - Push notifications

Implementing push notifications for your app consists of two steps. First, you need to get your push backend up and running ([Android SDK - Push Notifications](https://developer.zendesk.com/embeddables/docs/android/push_notifications)). Second, you need to update your existing Android app to receive and handle messages issued by Google Cloud Messaging (GCM).

This guide focuses on how the Remember the Date app implements push ([Github](https://github.com/zendesk/sdk_demo_app_android), [Google Play](https://play.google.com/store/apps/details?id=com.zendesk.rememberthedate)).
This is just one way of implementing push.

### Implementing GCM Client on Android

You need to set up a GCM client on Android. If you haven’t done so yet, see [Implementing GCM Client on Android](https://developer.android.com/google/gcm/client.html) from Google.

During the registration process, you'll receive a registration ID. Use this ID to enable and disable push notifications through the Zendesk SDK.

```java
ZendeskConfig.INSTANCE.enablePush("{registrationId}", new ZendeskCallback<PushRegistrationResponse>() {
    @Override
    public void onSuccess(PushRegistrationResponse pushRegistrationResponse) {

    }

    @Override
    public void onError(ErrorResponse errorResponse) {

    }
});
```

#### Check if the basic setup is working
To check if your GCM setup if working properly, you can use the following curl command:

```bash
 curl -vv -X POST "https://android.googleapis.com/gcm/send" \
   -H "Content-Type: application/json" \
   -H "Authorization: key={api_key}" \
   -d '{"registration_ids":["{registration_id}"],"data":{"zendesk_sdk_request_id":"{request_id}"}}'
```

If everything is working as expected, your `IntentService`/`BroadcastReceiver` will be invoked. The given Intent should contain an extra string, called `zendesk_sdk_request_id`.

### Handling push notifications
You'll find the complete implementation on Github: [PushIntentService](https://github.com/zendesk/sdk_demo_app_android/blob/master/app/src/main/java/com/zendesk/rememberthedate/push/PushIntentService.java)

#### Requirements
To get the most out of Android’s notifications in our app, we aim to do the following:

1. Display the newest reply message
2. Show the agent’s avatar
3. Open the updated ticket, if the user clicks on the notification
  1. If the request is shown to the user, trigger a refresh
4. Handle notifications for multiple requests
  1. Show only one notification per updated request
  2. Update already exsisting notification

#### Refresh the comment stream
Assuming that we received a push notification containing a valid request ID, the first thing we want to check is if the user is looking at this specific request.

We do that by using the built-in deep-linking functionality of the SDK. To trigger a refresh, invoke the following:

```java
...
final boolean refreshTriggered =  ZendeskDeepLinking.INSTANCE.refreshComments(requestId);

if(!refreshTriggered){
	// User isn't looking at the pushed request. Show a notification.
}
...
```
`refreshComments()` will return `false` if no refresh was triggered.

#### Fetching data
We use [providers](https://developer.zendesk.com/embeddables/docs/android/providers) to download the data we want to display.

##### Newest comment
To download the latest comment, we use the following provider function:

```java
...
// RequestProvider:
public void getComments(String requestId, ZendeskCallback<CommentsResponse> callback);
...
```
The callback passed into `getComments()` returns a `CommentsResponse`, which contains a list of comments and users.

##### Request details
To get more detailed information about the request, we use the following provider method:

```java
...
// RequestProvider:
public void getRequest(String requestId, ZendeskCallback<Request> callback);
...
```

##### Fetching the agent’s avatar picture
We'll need the agent’s avatar picture as a `Bitmap` to put it into an Android notification. You'll find the URL pointing to the picture in the `User` object.
Because `User#getPhoto()` could be `null`, make sure you provide a fallback picture.

We use [Picasso](http://square.github.io/picasso/) to download a Bitmap:

```java
...
RequestCreator requestCreator = ZendeskPicassoProvider.getInstance(getApplicationContext())
    .load(user.getPhoto().getContentUrl())
    .error(com.zendesk.sdk.R.drawable.zd_user_default_avatar)
    .transform(ZendeskPicassoTransformationFactory.INSTANCE.getRoundedTransformation((2 * dp), 0));

requestCreator.into(new Target() {
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // show notification
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        // show notification, provide a default image
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        // Intentionally empty
    }
});
...
```

#### Building a notification
|||
|-----------------|----------------------------------------|
| User            | Detailed user information              |
| CommentResponse | The newest comments                    |
| Request         | Detailed information about the request |
| Bitmap          | The user's avatar                      |

Time to build the notification. Android provides a builder called `NotificationCompat.Builder` that helps us do that.

##### Content text
As mentioned before, the content of the notification should contain the agent’s name and newest comment. The agent's name should be bold. We use a `SpannableString` to do that.

```java
...
String line = String.format("%s:  %s", user.getName(), commentResponse.getBody());
SpannableString spannableString = SpannableString(line);
spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, user.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
...
```

##### Content Intent
We want to show the updated request as a response to a notification.
Therefore we use the SDK’s deep-linking functionality. We aquire an Intent by calling `ZendeskDeepLinking.INSTANCE.getRequestIntent()`, wrap it into a ```PendingIntent```, and put it into the notification.

###### Deep linking
Before invoking `getRequestIntent()`, we make sure our `ZendeskConfig` is initialized.
The Intent could be fired at some point in the future. During this time, Android could kill our app. When opening a request, deep linking has to make sure that `ZendeskConfig` is initialized.
So, if you're using deep linking in an `IntentService` for example, check if your `ZendeskConfig` is configured or use the `getRequestIntent` function, which accepts a Zendesk URL, application ID, and oAuth client ID.


To be compliant with the Android design guidelines, we have to provide a proper back stack of activities, after the user navigates back from viewing the request.
`getRequestIntent()` automatically opens list of requests after the user presses back.
In the case of the Remember the Date app, the user should be forwarded to the `MainActivity` displaying the `HelpFragment`.
To achieve that, we create an Intent for MainActivity, put it into a list, and pass it to `getRequestIntent()`.

```java
...
final Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
mainActivity.putExtra(MainActivity.EXTRA_VIEWPAGER_POSITION, MainActivity.VIEWPAGER_POS_HELP);

final ArrayList<Intent> backStackItems = new ArrayList<>();
backStackItems.add(mainActivity);
...
```

In addition, a fallback activity can be provided and it will be shown if deep linking fails.
It’s unlikely, but can happen if the user configuration changes, for example.

###### Create a PendingIntent
After receiving an Intent from `ZendeskDeepLinking`, we have to wrap it in a PendingIntent. To do that, we need to provide an ID and flags. We can use them to handle notification for multiple request properly.

We can update an existing notification by adding the flag `PendingIntent.FLAG_UPDATE_CURRENT
`. We must ensure that we provide a unique ID per request.

```java
...
PendingIntent contentIntent = PendingIntent.getBroadcast(context, requestId.hashCode(), deepLinkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
...
```

#### Wiring everything together

```java
...
Notification notification = new NotificationCompat.Builder(context)
    .setContentTitle(request.getSubject())
    .setContentText(spannableString)
    .setSmallIcon(R.drawable.ic_conversations)
    .setLargeIcon(bitmap)
    .setDefaults(Notification.DEFAULT_ALL)
    .setStyle(new NotificationCompat.BigTextStyle().bigText(spannableString))
    .setAutoCancel(true)
    .setContentIntent(contentIntent)
    .build();

NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
nm.notify(request.getId().hashCode(), notification);
```
