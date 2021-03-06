package tick.tack.toe.client.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import tick.tack.toe.client.models.Match;
import tick.tack.toe.client.models.Player;

public class AskToResumeNotification extends Notification {
    private Player player;
    private Match match;

    public AskToResumeNotification() {
        super(NOTIFICATION_ASK_TO_RESUME);
    }

    public AskToResumeNotification(Player player, Match match) {
        super(NOTIFICATION_ASK_TO_RESUME);
        this.player = player;
        this.match = match;
    }

    public AskToResumeNotification(@JsonProperty("type") String type,
                                   @JsonProperty("player") Player player,
                                   @JsonProperty("match") Match match) {
        super(type);
        this.player = player;
        this.match = match;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }
}
