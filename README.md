# Marcia-bot

---

Spring boot application that listens to Telegram and discord Messages. Search YTS library for movie torrent files.

# Requirements

---

* Java 11
* Maven 3

```bash
mvn -e -P clean package -DskipTests
```

* Docker 

```bash
docker build --tag=marcia-bot:latest
```

# Configuring the bot tokens

---

Change the configuration in spring-boot application.yml file

```yaml
application-configurations:
discord-bot-token: { your discord bot token goes here }
telegram-bot-token: { your telegram bot token goes here }
```

#Live versions 

---

Telegram: [https://t.me/marcia_movie_bot](https://t.me/marcia_movie_bot)

Discord: [click here to invite bot to your channel](https://discord.com/oauth2/authorize?client_id=874578310955421716&scope=bot&permissions=0)





