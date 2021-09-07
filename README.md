# WorkoutsEvents

## Screenshots

<table>
<tr>
<td>
<img src="/art/Component61–1.png" width="300"/>

</td>
<td>
<img src="/art/Component62–1.png" width="300"/>

</td>
<td>
<img src="/art/Component63–1.png" width="300"/>

</td>
<td>
<img src="/art/Component64–1.png" width="300"/>

</td>
</tr>

<tr>
<td>
<img src="/art/Component65–1.png" width="300"/>

</td>
<td>
<img src="/art/Component66–1.png" width="300"/>

</td>
<td>
<img src="/art/Component67–1.png" width="300"/>

</td>
<td>
<img src="/art/Component68–1.png" width="300"/>

</td>
</tr>
</table>


## Introduction

Workout & Events is a reference application for HMS Kits to phones running with the android based HMS Service. Workout & Events app is provides you to increase your sociability while doing sports by creating Sport Events and sharing these events with people. Motionless is a big disadvantage for our health. With this application, we can start taking action. It allows you to apply the movements you want from the 4 different categories offered to you, along with the video, and check whether you are doing the movement correctly. Also, it follows your upcoming events and historical events.

## Project Structure

Workouts & Events app is designed with MVVM design pattern and Kotlin is used for development.

## What You Will Need


### Hardware Requirements
-	A computer that can run Android Studio.
-	A Huawei phone for debugging.
### Software Requirements
-	Android SDK package
-	Android Studio 3.X-4.X
-	HMS Core (APK) 4.X or later

## Getting Started

Workout & Events app uses Huawei services. In order to use them, you have to create an app first. Before getting started, please sign up for a Huawei developer account.

After creating the application, you need to generate a signing certificate fingerprint. Then you have set to this fingerprint to the application you created in App Gallery Connect.

•  Go to "My Projects" in AppGallery Connect. 
•  Find your project from the project list and click the app on the project card. 
•  On the Project Setting page, set SHA-256 certificate fingerprint to the SHA-256 fingerprint you've generated.

## Using the Application

Before you run the app, make sure that you have a working internet connection since the application uses Huawei Mobile Services. Otherwise, the app will not be able to perform the security checks only after which you can use the app.

When the user first enter the application, the login screen opens, the login process with the account kit is made, after the permissions are made and the necessary permissions are obtained, the home screen is switched to. There are 3 tabs on the main screen. The first tab can display your event activities list, date and location. The user can create event volleyball, football etc. in the lower right corner. After you click plus symbol, you can see the event creation detail page. You must enter event name, event date, time, duration, category and place. Description is an optional. In the second tab, the user can see the all workout categories and their names, calories, duration and equipments. When user click any workout name, s/he can see all activities that related to workout. 
The user just needs to click on the exercise they want to do to start the App or see if user done the action correctly. If the user wants, they can start to apply the movement or click on the "exercise detail" button and they are directed to the relevant page where they can check the accuracy of the movement. On this page, the user needs to do is mentioned step by step. After clicking the button, the correct view of the action he wants to do is shown to the user at the bottom right. The user can also make the same movement and check the similarity with the help of the camera. The result of similarity is shown to the user. If there is no similarity, the user can repeat the action. The user can continue to make the move after making the necessary checks. Video Kit, Audio Kit and ML kit used for this use case. The third tab is the profile tab. Here your information is displayed. You can see the upcoming activities, activities history, total activity count and up to date location. 

## Implemented Kits And Services

### Account Kit

Account Kit provides you with simple, secure, and quick sign-in and authorization functions. Instead of entering accounts and passwords and waiting for authentication, users can just tap the Sign in with HUAWEI ID button to quickly and securely sign in to your app with their HUAWEI IDs.

### Video Kit

Video Kit was used to transfer the videos in the application to the users.

### ML Kit

Huawei ML Kit allows your apps to easily leverage Huawei's long-term proven expertise in machine learning to support diverse artificial intelligence (AI) applications throughout a wide range of industries. Your app can gain insight into a user’s movement similarities with referance photo. Also, text to speech can convert workout text informations into audio output in real time.
 
### Map Kit

Map Kit provides powerful and convenient map services for you to implement personalized map display and interaction at ease. Your app can show user event’s location on map directly.

### Cloud DB

Cloud DB is a device-cloud synergy database product that provides data synergy  management capabilities between the device and cloud, unified data models, and various data management APIs.

### Cloud Storage

Cloud Storage allows you to store high volumes of data such as images, audios, and videos generated by your users securely and economically. Video and data shown to the user in the application are stored in cloud storage.

### Location Kit

Location Kit combines the Global Navigation Satellite System (GNSS), Wi-Fi, and base station location functionalities into your app to build up global positioning capabilities, allowing you to provide flexible location-based services for global users. 

### Audio Kit

Audio provides you with audio playback capabilities based on the HMS ecosystem, including audio encoding and decoding capabilities at the hardware level and system bottom layer. The application allows motivational music to be played while the user is doing the exercises.

### Libraries

-	Auth Service
-	Audio Kit
-	Cloud DB
-	Cloud Storage
-	ML Kit 
-	Location Kit
-	Video Kit
-	Map Kit
-	Retrofit
-	Navigation
-	Koin
-	Material Components
-	Glide


## Licence
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.