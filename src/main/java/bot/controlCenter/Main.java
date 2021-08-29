package bot.controlCenter;

import bot.controlCenter.MsgListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

public class Main {
    public static JDA jda;
    public static String prefix = "!";

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(Token.token);
        builder.addEventListeners(new MsgListener());
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        );
        MemberCachePolicy policy = MemberCachePolicy.ALL;
        builder.setMemberCachePolicy(policy);
        jda = builder.build();
    }
}

