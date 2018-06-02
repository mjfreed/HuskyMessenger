Husky Messenger

Developed by:
Marshall Freed
Jon Anderson
Karan Kurbur
Mahad Fahiye

Links:
Google Drive: https://drive.google.com/drive/folders/1omcy1Qqcv_DX3HQEdOuXY8-AaqrPs3Kn
Android Repository: https://github.com/karankurbur/TCSS-450-Group-3
Heroku Backend: https://dashboard.heroku.com/apps/group3-messenger-backend

Successfully implemented:

Register
	Required info:
		First and last name
		Email (verified)
		Nickname (displayed in app)
		Password
	Client side checks for validity
		Password requires 5+ characters with 1+ uppercase and 1+ number
	On registration attempt, takes the user to verification page to enter code from email

Login
	Uses username
	Client and server side checks
		Client side checks for no empty fields and password requirements
		Server side makes sure the login credentials exist in the database
	Option to stay logged in stored in shared preferences
	Upon successful login, takes user to home/landing page

Connections
	View existing connections
	View connections sent by you
	View connections sent to you
	Searching for new connections yields results
		Searches by email, username, first or last name
	Remove existing connections
	Rescind connections requests
	Accept or deny connection requests
	Start a chat directly from the connections page

Chat
	Individual or group chat with existing connections
		Start and end a chat
		Send and receive messages
			Messages are stored server side
	Continue an individual chat with an existing connection
	Open a new chat request from an existing connection
	Ability to add or remove members from a chat while inside that chat
	
Notifications
	All notifications appear when:
		App is not in the foreground
		The user is viewing the app on the home page
		The user is viewing the app outside the context of the notification (limited)
	Notifications for the following reasons
		New connection request
		New messages from an existing conversation
	Notifications when the app is not visible only appear when logged in
	Notifications display:
		Via status bar when app is not in the foreground
			Selecting the notifications opens the correct state
			Status bar/notification drawer shows sender and notification type
		Via a notification bar when the app is in the foreground
			Bar notifies user where to navigate for notification

Weather
	Displays the weather forecast for:
		The devices location
	Ability to save any of the above locations to display that weather locations forecast at this time
	Weather display includes:
		Current conditions 
		24 hour forecast
		10-day forecast 
	
Home/Landing Page
	When a user logs in our home page will display:
		Notification bar displaying new notifications with notification type
		Links to 5 most recent chats loaded dynamically
		Button to navigate to search connections menu
		Custom logo and greeting
	Ability to start new chats from homepage if a user has less than 5 recent chats
		
Look and Feel
	Custom logo on the login page
	Logo as the app icon
	Logo on the navigation drawer
	Themes and styles
		Option to select from 4 themes
			Saved in shared preferences
	
	
Not Implemented:

UI for weather data
Chat is not scaled correctly on all devices
Notification for a new conversation request (app does not have conversation requests)
The ability to display weather forecast by selecting location on a map
A location searched for by zip code
