# Sleep_cycle_alarm
An smart alarm clock which will wake up the user at the right time based on the sleep cycle.
The app calculates the right time to go for sleep based on the aimed time of wake up.
Exemple: user wants to wake up at 7:00. The app will return him the time when he should go for sleep = 23:16 (5 complete cycles of sleep + 14 minutes to fall asleep in reverse).
It also calculates the right moment to wake up based on the moment of fall asleep. 
Example: user knows he wants to go in bed at 22:30. The app will return him the time of waking up = 6:14 (5 complete cycles of sleep + 14 minutes to fall asleep added to the initial time given by the user).

It has the following features/characteristics:
-shows the current time and date on MainActivity screen; 
-a button for setting the alarm which pops up a time picker dialog fragment for the user to choose the time for wake up; 
-a button to cancel the alarm if set; if the user sets a time before the current time then the alarm will ring the next day at the chosen time;
-a button for setting up the alarm for a perfect wake up if the user is going to sleep at the very next moment
-when the time comes another Activity screen will show up while a ring is playing; this new Activity will be shown even if the device has the screen locked; 
-a dismiss button will bring back the user to a reset view of the MainActivity screen;
-intidactes the right time to go for sleep if an hour of wake up is given
