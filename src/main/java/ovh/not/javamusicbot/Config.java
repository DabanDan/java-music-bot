package ovh.not.javamusicbot;

import java.util.Map;

public class Config {
    // toggles
    boolean dev;
    public boolean patreon;

    // discord
    String token;
    String game;

    // vars
    public String[] owners;
    String regex;
    String prefix;
    public String invite;

    // messages
    public String about;
    String join;

    // bot list statistics tokens
    String carbon;
    String dbots;
    String dbotsOrg;

    // patron system server & roles
    String discordServer;
    String supporterRole;
    String superSupporterRole;
    String superDuperSupporterRole;

    // owo.whats-th.is api key
    public String owoKey;

    // lavalink
    String lavalinkUserId;
    Map<String, String> lavalinkNodes;
}
