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
  "serverPort": 7359,
  "serverTimeout":0
}
```

## Todo:
- [ ] Fix Ingenico `device.cancel();` issue
- [ ] Add command for getting card reader status
- [ ] Add command to trigger a `getTerminalConfiguration` job
- [ ] Pass currency code through to Ingenico
- [ ] Error logging & handling
- [ ] Service wrapper for Java project
- [ ] Installer for Java service
