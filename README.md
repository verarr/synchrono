# Synchrono

A mod for Minecraft that syncs the in-game daylight cycle to a location in real life.

It uses data from [SunriseSunset.io](https://sunrisesunset.io) to get accurate timestamps for sunrise and sunset. The location of the server can be set in configuration files.

## Usage

Before starting to play, you need to **set the coordinates of your server** (or whichever place you want to sync in-game time to). To get the appropriate coordinates, you could, for example, **search for "\<your city\> coordinates"** on the internet ([geodatos.net](https://www.geodatos.net/en/coordinates) often comes up). You should have **two decimal values**, either of which may be negative.

### GUI configuration

In the GUI configuration menu (needs [Mod Menu](https://modrinth.com/mod/modmenu)), you need the Latitude and Longitude options. If you're unsure which one is which, latitude usually comes first.

### Configuration file

The configuration file (needed eg. when on a server) is located at `<server directory>/config/synchrono.json5`. It looks something like:

```json5
{
    latitude: 51.11,
    longitude: 17.02,
    /* rest of the file */
}
```

Change the two values to your coordinates. Make sure you leave the commas on the end of the lines.

### Other configuration options

See the [wiki](https://github.com/verarr/synchrono/wiki/Configuration)

## Dependencies

[Custom Time Cycle](https://modrinth.com/mod/customtimecycle) by [eclipseisoffline](https://github.com/eclipseisoffline) is used to make the in-game daylight cycle slow down smoothly.

[YetAnotherConfigLib](https://modrinth.com/mod/yacl) is used for its configuration API and GUI.

## Recommendations for other mods

[Adjusted Phantom Spawns](https://modrinth.com/mod/adjusted-phantom-spawns) let's you change how often phantoms spawn. (useful if the in-game days are longer than usual)

## External API usage

This mod periodically queries some APIs on the internet to retrieve necessary data to function.

* [SunriseSunset.io](https://sunrisesunset.io/) - needed for accurate localized sunrise and sunset timestamps.
* [GeoTimeZone.com](https://geotimezone.com) - needed for automatically computing the time zone of a location.

If your server doesn't have access to these, it might crash with the mod installed.

## License

This mod is licensed under GNU LGPLv3.
