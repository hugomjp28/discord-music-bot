import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class Main {
    private final static String TOKEN = "ODkyNDY3NTQyOTYxMDQ1NjE3.YVNVKw.oTpfs-Bh6u1jmrWnMgF8VE09Ato";
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(TOKEN);
        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Disable compression (not recommended)
        builder.setCompression(Compression.NONE);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.listening("Lily Santos"));

        //Listeners
        builder.addEventListeners(new Listener('!'));
        builder.build();
    }
}
