# MusicalMigrator (MM)
A simple tool to migrate music libraries between streaming services
Currently only supports Google Play Music -> Spotify, since that's all I needed it for

This tool uses the following github projects for modifying streaming libraries:
https://github.com/FelixGail/gplaymusic - MM uses a modified fork of this project to grab _all_ songs, not just those with valid streaming URLs
https://github.com/thelinmichael/spotify-web-api-java

# Build
Simply `mvn clean install` to create a runnable jarfile.

Be forewarned that this project uses a forked library for getting music from google play. The fork was hacked together and wouldn't be available if you built locally. MM will still work with the library published in Maven artifactory, but it will only fetch tracks that have streaming URLs, which likely means that music you uploaded to your google play library will be ignored.
If you just want to run this program, just download one of the jars from the release directory

# Usage
CD into the target directory and run with `java -jar MusicalMigrator-1.0.jar`

You will first be prompted for 3 pieces of information:
- Google play music email
- The IMEI # of a device associated with your Google music account
  - https://www.wikihow.com/Find-the-IMEI-or-MEID-Number-on-a-Mobile-Phone
- Your Google play password

If you plan on running the migration multiple times, you can save time in the future by creating a file named "authData" and inserting information in the following format:
```
email
IMEI
```
For security reasons, passwords will not be read from the file

Once this information is received, the streaming service will be queried to grab all songs in your library.
When the library import has been completed, MM requests a token from spotify to grant full read/write control of your spotify library. Because of limitations to my patience, the token is not directly acquired. Instead a url is printed to the console that you need to click, which will navigate you to google, but the url will have your token appended. Copy everything after /?code, and paste it back into the terminal. MM will handle the migration from there!

You must be signed into spotify on the machine MM is running from.