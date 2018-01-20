package edu.usf.myweb.jcameron2;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;

public class AjitPaiBot {

    private final IDiscordClient discordClient;

    public AjitPaiBot(IDiscordClient discordClient) {
        this.discordClient = discordClient;
        EventDispatcher dispatcher = discordClient.getDispatcher(); // Gets the client's event dispatcher
        dispatcher.registerListener(new MessageListener(this));
    }

}
