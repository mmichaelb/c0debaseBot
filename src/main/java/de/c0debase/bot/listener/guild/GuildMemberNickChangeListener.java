package de.c0debase.bot.listener.guild;

import de.c0debase.bot.core.Codebase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class GuildMemberNickChangeListener extends ListenerAdapter {

    public GuildMemberNickChangeListener(final Codebase bot) {
        bot.getJDA().addEventListener(this);
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        event.getGuild().getTextChannelsByName("log", true).forEach(channel -> {
            final EmbedBuilder logBuilder = new EmbedBuilder();
            logBuilder.setTitle("Nickname geändert");
            logBuilder.appendDescription("Neuer Nickname:" + event.getNewNickname() + "\n");
            logBuilder.appendDescription("Alter Nickname: " + event.getOldNickname());
            logBuilder.setFooter("@" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator(), event.getMember().getUser().getEffectiveAvatarUrl());
            channel.sendMessage(logBuilder.build()).queue();
        });
    }

}