# ZeroBot [![Build Status](https://travis-ci.org/zoewithabang/zerobot.svg?branch=master)](https://travis-ci.org/zoewithabang/zerobot) [![CodeFactor](https://www.codefactor.io/repository/github/zoewithabang/zerobot/badge/master)](https://www.codefactor.io/repository/github/zoewithabang/zerobot/overview/master)

ZeroBot is a Discord bot that supports [Markov chain messaging](https://blog.codinghorror.com/markov-and-you/),
[CyTube](https://github.com/calzoneman/sync) integration and other fun commands and features (like cat pictures on demand!).

If you have any questions or issues setting up or operating ZeroBot, please create a new issue on GitHub.

![All currently available ZeroBot commands](https://i.imgur.com/VZg5izJ.png)
*All currently available ZeroBot commands*

## Commands
Note: To see the syntax required for each command, use the `help` command once the bot is on your server.

### Markov
Once you have written permission from a user, you can use the `getposts` command to store their posts on your database. 
Once stored, you can use the `markov` command to generate a Markov chain message from a given single user, multiple users
or from all users on the server. Optionally, a seed can also be provided to force the chain message to begin with one or 
more given words.

If a user wants their stored posts removed, this automatically occurs when their account is cleared via the `user` command.

### CyTube
*Note: These are disabled by default.*

If you have a CyTube server hosted on the same machine and have configured the options table in the database appropriately,
you can use `music` and `np` to link a CyTube room and the currently playing song.

### Cat & Dog!
Use the `cat` and `dog` commands to get cat and dog pictures!

### Command Aliases
In order to not ping people constantly with `markov` commands, you can use `alias` to create a shorthand command alias! 
This works with any other command that ZeroBot recognises as well.

### Ranks and Management
Commands can be enabled, disabled and rank restricted using the `command` command. You can give users different permission 
ranks with the `user` command.

## Requirements
- Java 8+
- Apache Maven
- MySQL server
- Linux 64-bit (should work on Windows / other platforms too especially if you don't plan to support CyTube features)

## Setup
### Database
- Create a database for ZeroBot on your MySQL server with a dedicated user account like below (please don't use root).
```sql
GRANT USAGE ON *.* TO zerobot@localhost IDENTIFIED BY 'secret_zerobot_mysql_password';
GRANT ALL PRIVILEGES ON zerobot.* TO zerobot@localhost;
CREATE DATABASE zerobot;
```
- In `zerobot.sql` in the project root directory, edit the last line to put your Discord user ID in as a replacement to 
`insertyouridhere`, leaving the `''` around your ID. 
[Click here if you do not know how to find your Discord user ID.](https://support.discordapp.com/hc/en-us/articles/206346498)
- To get more cat and dog pictures, generate API keys on [The Cat API](https://thecatapi.com) and 
[The Dog API](https://thedogapi.com) and insert them between the empty `''`s on these lines, also in `zerobot.sql`:
```sql
INSERT INTO options (`key`, `value`) VALUES ('cat_api_key', 'cat api key here');

INSERT INTO options (`key`, `value`) VALUES ('dog_api_key', 'dog api key here');
```
- Run `zerobot.sql` on the new database that has been made, like below.
```
mysql -u zerobot  -p zerobot < zerobot.sql
```
### Bot Account
- Go to the [Discord Developer Portal](https://discordapp.com/developers) and create a new application.
- Select `Bot`, then `Add Bot` and copy the bot token.
### Configuration
- In `src/main/resources`, copy `zerobot.properties.example` to `zerobot.properties`.
- Edit `zerobot.properties` to add the bot account's token, the command prefix that you want to use and the MySQL database
details.
### Running
You should now be able to launch ZeroBot with `run.sh` in the project root directory.
