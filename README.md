<p align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png">
</p>

# VoteApp_Android

This is an android client for the [VoteApp](https://github.com/sstamatiadis/VoteApp) e-voting platform.

## Installation

1. Install Android Studio.
2. Clone this project and import it to android studio.
3. Generate APK.
4. Set the server IP address within the app options.

## A Quick view

<div>
  <img width="108px" height="192px" src="../assets/img/screen01.png">
  <img width="108px" height="192px" src="../assets/img/screen02.png">
  <img width="108px" height="192px" src="../assets/img/screen03.png">
  <img width="108px" height="192px" src="../assets/img/screen04.png">
  <img width="108px" height="192px" src="../assets/img/screen05.png">
  <img width="108px" height="192px" src="../assets/img/screen06.png">
  <img width="108px" height="192px" src="../assets/img/screen07.png">
  <img width="108px" height="192px" src="../assets/img/screen08.png">
  <img width="108px" height="192px" src="../assets/img/screen09.png">
  <img width="108px" height="192px" src="../assets/img/screen10.png">
  <img width="108px" height="192px" src="../assets/img/screen11.png">
  <img width="108px" height="192px" src="../assets/img/screen12.png">
  <img width="108px" height="192px" src="../assets/img/screen13.png">
  <img width="108px" height="192px" src="../assets/img/screen14.png">
</div>

## Developers

The app follows the rules of the server API so it should be returning correct error messages.
Networking is done with [Retrofit](https://github.com/square/retrofit) along with [jsonapi-converter](https://github.com/jasminb/jsonapi-converter)
to keep up with the [JSON-API specification](http://jsonapi.org/) and rxjava2. Finally, [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
used to draw charts.

### Issues

- No data persistence.
- In some cases system interrupts are not handled.
- Friend-lists are saved using `SharedPreferences`.
- App size is about 40Mb.
- Not tested with different devices.

## License

This project is licensed under the MIT License - see the [LICENSE.md](./LICENSE.md) file for details.
