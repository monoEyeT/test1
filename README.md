#### By engaging with this repository you explicitly agree with the terms of the Unlicense.

Thanks to all.


#### Item Filter:
* NUMPAD_1 -> Draw All Item
* NUMPAD_2 -> Stop rotating, back to original
* NUMPAD_3 -> I dont use
* NUMPAD_4 -> I dont use
* NUMPAD_5 -> I dont use
* NUMPAD_6 -> I dont use
* NUMPAD_0 -> I dont use

#### Zooms:
* NUMPAD_8 -> Looting - Combat -Scouting
* NUMPAD_PLUS ->  Camera Zoom ++
* NUMPAD_MINUS -> Camera Zoom --

#### Other
* F1 -> Change Player Info (Name, Distance, HP, Weapon)
* F2 -> Toggle Compass
* F3 -> Toggle Mini Map
* F4 -> Toggle View Line
* F5 -> Toggle Vehicles (icon, name or both)
* F12 -> Toggle View Line



### Online Mode:
Check Online.bat file

## Build

1. Install [Maven](https://maven.apache.org/install.html)
2. Add Maven to your environment PATH, screenshot below.
3. Add MAVEN_OPTS environment variable, screenshot below.
4. Install [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
5. Add JAVA_HOME to your Environment Path, screenshot below.
6. Use the command prompt to go to your VMRadar directory (with the src folder)
7. type `mvn verify install` into the command prompt.

#### You can find detailed instructions on how to run a maven project [here](https://maven.apache.org/run.html)

[IntelliJ IDEA](https://www.jetbrains.com/idea/?fromMenu)

#### MAVEN_OPTS
![Imgur](https://i.imgur.com/aWCdgUX.png)

#### Path (Java and Maven)
![Imgur](https://i.imgur.com/hSCYrCM.png)

#### JAVA_HOME
![Imgur](https://i.imgur.com/4zT1YNR.png)

## Install

### VM

1. Install Virtual Box
2. Install Virtual Box Extension Pack ( https://download.virtualbox.org/virtualbox/5.2.6/Oracle_VM_VirtualBox_Extension_Pack-5.2.6-120293.vbox-extpack )
3. Install Windows 7  x64 (64 bit) (!!!NOT Windows 10!!!) on the VM (recommended: 2gb ram, 16gb storage, 2 processors)
Recommended: https://softlay.net/operating-system/windows-7-all-in-one-iso-free-download-32-64-bit.html
Use Windows 7 ultimate when installing
4. Install https://www.winpcap.org/install/ and https://www.microsoft.com/en-us/download/details.aspx?id=48145 on the VM
5. Install .Net framework 4.5: https://www.microsoft.com/en-us/download/details.aspx?id=30653
6. Go to settings of your VM, go to network, set attached to: 'Bridged adapter' and set promiscuous mode: 'Allow all'
7. Go to settings of your VM, go to video, set video memory to max and enable both acceleration checkboxes.
8. Run radar on VM.

#### No need to set up VPN using this method!

#### If you have "GLFW_PLATFORM_ERROR" try this:

1. Press windows + r
2. Type in msconfig and press enter
3. Go to 'boot' and check 'safe boot'
4. Apply/ok and restart VM (it will boot in safe mode)
5. Go to VirtualBox toolbar and go to Devices -> Insert Guest Additions CD image...
6. Press Windows + E and open the CD drive for guest additions
7. Install, select Direct3D support when installing! Do not reboot after installation.
8. Go to msconfig, boot, disable safe boot and restart to normal windows

### 2nd PC

Download:
https://www.winpcap.org/install/

#### This is for PC/VM where your radar runs.
1. Go to command prompt (CMD)
2. Type "ipconfig"
3. Write your IPv4 address and default gateway down
4. Use windows seacrh and look for ncpa.cpl
5. Go to your internet adapter properties and then IPv4 properties and then check "Obtain an IP address automatically" and also "Obtain DNS server address automatically"
6. Uncheck IPv6 and then just press OK
7. Then go back to ncpa.cpl (should already be open)
8. Press "alt" and go "File" from left top corner and there "New incoming connection"
9. Add someone and make sure to remember username and password then go next
10. check "Through the internet" then go next
11. Go to IPv4 properties and make sure "Network access" is checked
12. Under "IP address assignment" Check "Specifiy IP Addresses"
13. Use your Default Gateway, to create a proper IP range for "Specify IP addresses" Example: gateway is 192.168.0.1, use 192.168.0.2 to 192.168.0.10 for the ip range. Then select "OK"
14. Select "Allow access" and now we can move to our PUBG pc where we will play our games.

#### This is for PC where your PUBG runs.
1. For this computer also search for ncpa.cpl
2. Go to your internet adapter properties and check "Obtain an IP address automatically" and also "Obtain DNS server address automatically"
3. Uncheck IPv6 and press OK
4. Go to your "Network & Internet" settings
5. Go to "VPN" and from there "Add a VPN connection"
6. These setting are recommended:
VPN Provider: Windows (build-in)
Connection name: random
Server name or address: This is the IPv4 address that you wrote down first
VPN Type: automatic
User and Password as the ones you created earlier and now click save.

Now try to connect to your VPN and if it succeeded you are ready to go

#### Read the Guide thats provided
Located in the Help folder

## Run
-----------------

```
@echo off
for /f "tokens=14" %%a in ('ipconfig ^| findstr IPv4') do set _IPaddr=%%a
echo YOUR IP ADDRESS IS: %_IPaddr%
echo "RUNNING VMRADAR"
set /p game=ENTER GAMEVM IP:
echo "%game%"
java -jar target\pubg-radar-1.0-SNAPSHOT-jar-with-dependencies.jar %_IPaddr% PortFilter "%game%"
```
or

```
@echo off
for /f "tokens=14" %%a in ('ipconfig ^| findstr IPv4') do set _IPaddr=%%a
java -jar target\pubg-radar-1.0-SNAPSHOT-jar-with-dependencies.jar %_IPaddr% PortFilter %_IPaddr% Offline

```

## For others
```

1. Gaming PC
Just play game. 

2. For 2nd PC
Gaming PC and 2nd PC should be connected at the same router. 
Check both ips. 

Lanport 1 is gaming PC.
Lanport 3 is monitoring PC.(laptop)
```
![Imgur](https://i.imgur.com/k0jmJpb.png)
```
Download JDK 1.8.161 ~
Download Maven
Just Follow the Build section 
and!!!!
Open java.security file at "C:\Program Files\Java\jre1.8.0_161\lib\security"
line 829: crypto.policy=unlimited 
You should change the option from  #crypto.policy=unlimited to crypto.policy=unlimited


Compile downloaded src. 

Run Online.bat at laptop. 
and Playing game.
```