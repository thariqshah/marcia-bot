# Marcia-bot

---

A simple light-weight spring boot application which queries ytx api for movie torrent files. <br>
Discord bot & Telegram bot build on top of [JAVACORD](https://github.com/Javacord/Javacord)  Library and [Telegram Bot](https://github.com/rubenlagus/TelegramBots) Library

# Requirements

---

* Java 21
* Maven 3+ <br>


build command
```bash
mvn -e -P clean package -DskipTests
```

* Docker

```bash
docker build -t marcia-bot:latest .
```

jar run command
```bash
java -jar {jar file name} --SECRET_KEY= {bot token secret}
```

Docker container

```bash
docker run  marcia-bot --SECRET_KEY= {bot token secret}
```


# Configuring the bot tokens

---

Change the configuration in spring-boot application.yml file

```yaml
application-configurations:
telegram-bot-token: { your telegram bot token goes here }
```

# Live versions

---

Telegram: [https://t.me/marcia_movie_bot](https://t.me/marcia_movie_bot)


# Bot Commands

Discord: @Marica {moviename} <br>
telegram: send movie name as private message 

