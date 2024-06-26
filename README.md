# GP-Service

This is a first pass at the Java service to act as a bridge between the 4D ePOS system and an Ingenico Lane/3000 card terminal.

All development has been done in IntelliJ, using Java JDK 20.0.2.

## Required Libraries:
- [Gson](https://mvnrepository.com/artifact/com.google.code.gson/gson) - v2.10.1
- [jSerialComm](https://github.com/Fazecast/jSerialComm) - v2.6.0
- [globalpayments-sdk](https://developer.globalpay.com/point-of-sale/semi-integrated) - v1.8.2

These libraries have been included in the `lib/` folder, and should be correctly setup within the IntelliJ project already.

## Config

A `config.json` file is required in the root directory of the repository. An example of its contents can be found below:

```
{
  "cardTimeout": 65000,
  "cardPort": 4,
  "serverPort": 7359
}
```

## Build

To build the `GP-Service.jar` file from IntelliJ, simply go to `Build/Build Artifacts` and click the `Build` action. This will build `GP-Service.jar` and put it in the `build/` directory.

Once the `GP-Service.jar` file has been built, the `build` directory is ready to be placed on an EPOS system and the service restarted.

## Todo:
- [x] Fix Ingenico `device.cancel();` issue
- [x] Add command for getting card reader status
- [ ] Add command to trigger a `getTerminalConfiguration` job
- [x] Pass currency code through to Ingenico
- [x] Error logging & handling
- [x] Service wrapper for Java project
- [x] Installer for Java service
- [ ] Event logging
