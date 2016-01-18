# Drowsy-Alert

Drowsy Alert is an Android application created in Hack the North 2015.

This app connects to the Muse Headband via Bluetooth, and detects the drowsiness of the user.

It is mainly targeted towards drivers who get drowsy while driving, and is meant to prevent accidents by alerting the driver.
If it detects that the driver is falling asleep, it alerts the driver using the Google Text-to-Speech API, and also plays the default alarm ringtone. If the driver does not dismiss the alert within a given timeframe, the app assumes that the driver has fallen asleep and an accident has occured. It then send SMS messages containing the driver's location to an emergency contact list to notify others of the accident.
