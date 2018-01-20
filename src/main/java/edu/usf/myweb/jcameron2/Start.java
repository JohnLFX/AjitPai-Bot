package edu.usf.myweb.jcameron2;

import org.apache.commons.io.IOUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.io.IOException;
import java.io.InputStream;

public class Start {

    public static void main(String[] args) throws IOException {

        String botToken;

        try (InputStream in = Start.class.getResourceAsStream("/bot_token.txt")) {
            botToken = new String(IOUtils.toByteArray(in));
        }

        ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance
        builder.withToken(botToken); // Sets the bot token for the client

        IDiscordClient discordClient = builder.login();

        new AjitPaiBot(discordClient);

    }

}
