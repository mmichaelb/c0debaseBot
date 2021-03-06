package de.c0debase.bot.listener.message;

import com.vdurmont.emoji.EmojiManager;
import de.c0debase.bot.core.Codebase;
import de.c0debase.bot.database.data.CodebaseUser;
import de.c0debase.bot.utils.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpiringMap;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MessageReceiveListener extends ListenerAdapter {

    private static final long PROJECT_ROLE_ID = 408957966998568960L;

    private final Codebase bot;
    private final Map<Member, String> lastMessage;
    private final List<String> gifs = Arrays.asList(
            "https://media.giphy.com/media/5VKbvrjxpVJCM/giphy.gif",
            "https://media.giphy.com/media/4cUCFvwICarHq/giphy.gif",
            "https://media.giphy.com/media/1ym5LJ17vp77BL8X5O/giphy.gif",
            "https://media.giphy.com/media/KI9oNS4JBemyI/giphy.gif",
            "https://media.giphy.com/media/l1CC9FjH54QhYHExq/source.gif",
            "https://media.giphy.com/media/2gYhkl6mLIYZxpMve1/giphy.gif",
            "https://media.giphy.com/media/kmU72Ms75Zhlu/giphy.gif",
            "https://media.giphy.com/media/xHMIDAy1qkzNS/giphy.gif",
            "https://media.giphy.com/media/yJFeycRK2DB4c/giphy.gif",
            "https://media.giphy.com/media/cbb8zL5wbNnfq/giphy.gif",
            "https://media.giphy.com/media/aLdiZJmmx4OVW/giphy.gif",
            "https://media.giphy.com/media/qPcX2mzk3NmjC/giphy.gif",
            "https://media.giphy.com/media/kjCFOUT3ZIlAA/giphy.gif",
            "https://media.giphy.com/media/ZisaVxhbs1iDK/giphy.gif"
    );

    private static final long DISCUSSION_CHANNEL_ID = 361606003386613761L;

    public MessageReceiveListener(final Codebase bot) {
        this.bot = bot;
        final ExpiringMap.Builder<Object, Object> mapBuilder = ExpiringMap.builder();
        mapBuilder.expiration(30, TimeUnit.SECONDS).build();
        lastMessage = mapBuilder.build();
        bot.getJDA().addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getChannel().getTopic() != null && event.getChannel().getTopic().contains("\uD83D\uDCCC")) {
            createPoll(event.getMessage());
            return;
        }

        final Member member = event.getMember();
        if (lastMessage.containsKey(member) && lastMessage.get(member).equalsIgnoreCase(event.getMessage().getContentRaw()) && event.getMessage().getAttachments().isEmpty()) {
            event.getMessage().delete().queue();
            return;
        }
        lastMessage.put(member, event.getMessage().getContentRaw());

        updateXP(event.getMessage());
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.appendDescription("Private Nachrichten sind deaktiviert");
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    private void createPoll(final Message message){
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setFooter("@" + message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator(), message.getAuthor().getEffectiveAvatarUrl());
        embedBuilder.setTitle("Poll");
        embedBuilder.setDescription(message.getContentDisplay());
        message.delete().queue();
        message.getChannel().sendMessage(embedBuilder.build()).queue(sentMessage -> {
            sentMessage.addReaction(EmojiManager.getForAlias("thumbsup").getUnicode()).queue();
            sentMessage.addReaction(EmojiManager.getForAlias("thumbsdown").getUnicode()).queue();
        });
    }

    private void updateXP(final Message message){
        final CodebaseUser codebaseUser = bot.getDataManager().getUserData(message.getGuild().getId(), message.getAuthor().getId());
        final float time = (System.currentTimeMillis() - codebaseUser.getLastMessage()) / 1000;
        if (time >= 50.0f) {
            if (codebaseUser.addXP(50)) {
                final EmbedBuilder levelUpEmbed = new EmbedBuilder();
                final int newLevel = codebaseUser.getLevel();
                levelUpEmbed.appendDescription(message.getAuthor().getAsMention() + " ist nun Level " + newLevel);
                if(message.getIdLong() != DISCUSSION_CHANNEL_ID){
                    levelUpEmbed.setImage(gifs.get(Constants.RANDOM.nextInt(gifs.size())));
                }

                message.getTextChannel().sendMessage(levelUpEmbed.build()).queue();
                if (newLevel > 2 && !message.getMember().getRoles().contains(message.getJDA().getRoleById(PROJECT_ROLE_ID))) {
                    message.getGuild().addRoleToMember(message.getMember(), message.getJDA().getRoleById(PROJECT_ROLE_ID)).queue();
                }
            }
            codebaseUser.setLastMessage(System.currentTimeMillis());
            bot.getDataManager().updateUserData(codebaseUser);
        }
    }

}
