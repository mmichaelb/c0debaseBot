package de.c0debase.bot.utils;

import de.c0debase.bot.core.Codebase;
import net.dv8tion.jda.api.entities.Icon;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerBanner implements Runnable {

    private final Codebase bot;
    private URL unsplashUrl;

    public ServerBanner(final Codebase bot) {
        this(335434, bot);
    }

    public ServerBanner(final int collectionId, final Codebase bot) {
        this.bot = bot;
        try {
            this.unsplashUrl = new URL("https://source.unsplash.com/collection/" + collectionId + "/960x540");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates / replaces the current server banner.
     *
     * @param icon The new server banner.
     */
    public void setServerBanner(final Icon icon) {
        if(bot.getGuild() == null) return;
        if (!bot.getGuild().getFeatures().contains("BANNER")) {
            throw new UnsupportedOperationException("Your guild can not have a banner!");
        }
        if (icon == null)
            return;
        bot.getGuild().getManager().setBanner(icon).queue();
    }

    /**
     * Get the banner url of the provided guild.
     *
     * @return The banner url, if present. Otherwise null.
     */
    public String getBannerUrl() {
        if(bot.getGuild() == null) return null;
        if (!bot.getGuild().getFeatures().contains("BANNER")) {
            throw new UnsupportedOperationException("Your guild can not have a banner!");
        }
        return bot.getGuild().getBannerUrl();
    }

    /**
     * Get a random image from the provided (or default / fallback) collection (id).
     *
     * @return Image as {@link Icon} or null, if the requests failed.
     */
    private Icon getRandomUnsplashPicture() {
        try {
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) this.unsplashUrl.openConnection();
            httpsURLConnection.setRequestMethod("GET");
            try (final InputStream inputStream = httpsURLConnection.getInputStream()) {
                // something went wrong
                if (inputStream == null) {
                    return null;
                }
                return Icon.from(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method is called by {@link ServerBannerScheduler#start(ServerBanner)} to frequently update the guilds banner.
     */
    @Override
    public void run() {
        if (this.unsplashUrl == null) return;
        this.setServerBanner(getRandomUnsplashPicture());
    }
}