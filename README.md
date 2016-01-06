# MilestoneTest1
Android milestone test1

## Requirements
This is an app that can get all of your photos from Facebook and display them. It also helps you to post photo to Facebook.

Unfortunately, the app was poorly written and there are some bugs/features need to be fixed. We need your skill to fix them :)

## List of Bugs/Features from PM
1. After login successfully, the 2nd time users open the app, they cannot go into main screen. The right logic should be user wait for 2s and then go to the main screen.
2. In the main screen of the app, when user pull to refresh, the loading indicator still shows even if the request has been finished.
3. Current images' quality is too low, could you enhance it? We need to use images that has width/height nearest to 1024px.
4. Textview to display image’s name is supposed to take two third of the width, but currently it’s not.
5. We should also display the image's width, height at the top left corner of the list item. (in white text color)
5. Currently, the main screen only loads some of user’s photos. Could you implement the load more function?
6. There's no option for user to logout now. Add a menu at main screen to provide logout feature.
7. Pressing the floating action button at the main screen will let user to take a picture and post it to Facebook. 
    * However, there's no loading indicator showing an upload task is executing. Please provide one
    * Another problem is that the image quality that the app upload to Facebook is too low. Could you fix this?
8. Implement a feature to bookmark the images.
    * Add bookmark icon at the top right corner of the image view. Press on this icon will toggle image bookmark.
    * On main screen, which images have been bookmarked should display the selected state of bookmark icon.
    * Implement a Bookmark page that has the same UI as main screen but only show images that have been bookmarked.
    * Add a way for user to open bookmark page, use one of the below patterns or use your creativity to do it :)
        * Screen with [tabs](http://1.bp.blogspot.com/-VhMIJ24KNe4/VKvWaLY3flI/AAAAAAAA0DA/faVJBT4-WJk/s1600/1.png)
        * Add a spinner into the topbar similar to [this](spinner_navigation.gif)
9. Run this app on Android 6.0 will crash when user attempt to take a picture. Could you investigate why and fix it?
        
## Refereneces:
- All Facebook-related apis can be found [here](https://developers.facebook.com/docs/)

- You can use your own Facebook account or use two test accounts provided below:
    * first_hyhzefz_hasbrain@tfbnw.net/h@sBrain
    * second_gwuhrpt_hasbrain@tfbnw.net/h@sBrain

