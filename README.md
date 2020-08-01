## JWiiload
JWiiload is an alternative to Wiiload that is coded in Java. It also has a command line interface mode that can be called. JWiiload does not rely on the wiiload binaries, and instead operates entirely independently of them, enabling it to run on any OS that can utilize the Java Runtime Environment.

![logo](https://github.com/vgmoose/JWiiLoad/blob/master/src/jwiiload.png?raw=true)

See also: [Wiiload for Android](https://github.com/vgmoose/wiiload-for-android), which is based on this app.

### Sending Files to the Wii
Requirements
- Java Runtime Environment (JRE)
- Be on the same network as your Wii
- Linux, Mac, or Windows machine

### Usage
Double click the program, and if you have the JRE installed, it should just launch. First, it will ask for a file if the auto-send is on (by default) and then try to automatically find the Wii behind the scenes. If it can't locate it, which it often cannot, enter the IP address and hit send. There are preferences to specify arguments and a specific port, and also to disable the autosend feature. When autosend is disabled, it will not prompt the user at launch for a file.

To use it from the command line, run it as you normally would a .jar file, and pass it the necessary arguments. Passing any arguments at all will not launch the GUI, and keep it contained in the command line only.

```
java -jar JWiiload.jar <address> <filename> <application arguments>
```

pass $WIILOAD (*nix) or %WIILOAD% (win) as the first argument if your environment is set up that way. (Accepts "tcp:x.x.x.x" or just "x.x.x.x" to ensure compatibility with Wiiload.)

### License
This software is licensed under the GPLv3.

> Free software is software that gives you the user the freedom to share, study and modify it. We call this free software because the user is free. - [Free Software Foundation](https://www.fsf.org/about/what-is-free-software)

### Screen shots
![x](https://i.imgur.com/dQGGq9v.png) ![y](https://i.imgur.com/09yxva5.png)
