package de.c0debase.bot.database;

import de.c0debase.bot.database.data.CodebaseUser;
import de.c0debase.bot.utils.Pagination;

public interface Database extends AutoCloseable {
    
    CodebaseUser getUserData(final String guildID, final String userID);

    void updateUserData(final CodebaseUser codebaseUser);

    Pagination getLeaderboard(final String guildID);

}