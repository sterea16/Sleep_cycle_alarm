# Sleep_cycle_alarm
An smart alarm clock which will wake you up at the right time based on the sleep cycle.

The first step is developing a simple alarm clock. It has the following features/characteristics:
shows the current time and date on MainActivity screen; has a button for setting the alarm which pops up a time picker dialog fragment for the user to choose the time for wake up; has a button to cancel the alarm if set; if the user sets a time before the current time then the alarm will ring the next day at the chosen time; when the time comes another Activity screen will show up while a ring is playing; this new Activity will be shown even if the device has the screen locked; a dismiss button will bring back the user to a reset view of the MainActivity screen.

The next step: create a feature which calculates the right time to go for sleep based on the aimed time of wake up.
Example: user wants to wake up at 7:00. The app will return him the time when he should go for sleep = 23:16 (5 complete cycles of sleep + 14 minutes to fall asleep in reverse).
