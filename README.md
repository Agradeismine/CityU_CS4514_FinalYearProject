# CityU_CS4514_FinalYearProject
This is the CityU FYP.

How to Debug and Deploy for the Android Wear which do not have USB Debugging function:
1. download SDK Platform Tools
2. open cmd with corresponding file path
3. open 'Debug over WiFi' function in Android Wear
4. type 'adb connect (IP address):(port number)' in command prompt, e.g. 192.168.5.112:5555
5. connected
detail can found in 'adb help' or https://developer.android.com/training/wearables/get-started/debugging

If error: 'more than one device/emulator' occurs, try 1. adb kill-server or 2. taskkill /f /im adb.exe


