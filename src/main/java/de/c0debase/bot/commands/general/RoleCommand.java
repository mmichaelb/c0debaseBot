package de.c0debase.bot.commands.general;

import de.c0debase.bot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Biosphere
 * @date 23.01.18
 */

public class RoleCommand extends Command {

    private static final List<String> FORBIDDEN;

    static {
        FORBIDDEN = Arrays.asList("Projekt", "Friend", "-_-", "Mute", "@everyone");
    }

    public RoleCommand() {
        super("role", "Weise dir eine Programmiersprache zu", Category.GENERAL, "rolle", "roles");
    }

    @Override
    public void execute(final String[] args, final Message message) {
        if (args.length == 0) {
            EmbedBuilder embedBuilder = getEmbed(message.getGuild(), message.getAuthor());
            embedBuilder.setFooter("!role Java,Go,Javascript", message.getMember().getUser().getEffectiveAvatarUrl());

            embedBuilder.appendDescription("__**Es gibt diese Rollen:**__\n\n");

            for (Role role : message.getGuild().getRoles()) {
                if (!role.isManaged() && !FORBIDDEN.contains(role.getName()) && PermissionUtil.canInteract(message.getGuild().getSelfMember(), role) && !role.getName().startsWith("Color")) {
                    embedBuilder.appendDescription("***" + role.getName() + "***" + "\n");
                }
            }
            embedBuilder.appendDescription("\n__**Es gibt diese Farben:**__\n\n");
            for (Role role : message.getGuild().getRoles()) {
                if (role.getName().startsWith("Color")) {
                    embedBuilder.appendDescription("***" + role.getName().replace("Color-", "")+ "***" + "\n");
                }
            }
            message.getTextChannel().sendMessage(embedBuilder.build()).queue();
        } else {
            changeRole(String.join(" ", args).replaceAll(",", " "), message);
        }
    }

    private void changeRole(final String args, final Message message) {
        final List<Role> addRoles = new ArrayList<>();
        final List<Role> removeRoles = new ArrayList<>();
        for (String role : args.split(" ")) {
            if (!role.isEmpty() && !message.getGuild().getRolesByName(role, true).isEmpty() && !FORBIDDEN.contains(role)) {
                Role rrole = message.getGuild().getRolesByName(role, true).get(0);
                if (PermissionUtil.canInteract(message.getGuild().getSelfMember(), rrole) && !rrole.isManaged()) {
                    if (message.getGuild().getMembersWithRoles(rrole).contains(message.getMember()) && !removeRoles.contains(rrole)) {
                        removeRoles.add(rrole);
                    } else if (!addRoles.contains(rrole)) {
                        addRoles.add(rrole);
                    }
                }
            } else if(!message.getGuild().getRolesByName("Color-" + role, true).isEmpty()){
                Role rrole = message.getGuild().getRolesByName("Color-" + role, true).get(0);
                if (message.getGuild().getMembersWithRoles(rrole).contains(message.getMember()) && !removeRoles.contains(rrole)) {
                    removeRoles.add(rrole);
                } else if (!addRoles.contains(rrole)) {
                    addRoles.add(rrole);
                }
            }
        }
        final EmbedBuilder embedBuilder = getEmbed(message.getGuild(), message.getAuthor());
        embedBuilder.setTitle("Rolle(n) geupdatet");
        embedBuilder.appendDescription("Du bist " + addRoles.size() + (addRoles.size() > 1 ? " Rollen " : " Rolle ") + "beigetreten\n");
        embedBuilder.appendDescription("Du hast " + removeRoles.size() + (removeRoles.size() == 1 ? " Rolle " : " Rollen ") + "verlassen");
        message.getTextChannel().sendMessage(embedBuilder.build()).queue();
        message.getGuild().modifyMemberRoles(message.getMember(), addRoles, removeRoles).queue();
    }
}
