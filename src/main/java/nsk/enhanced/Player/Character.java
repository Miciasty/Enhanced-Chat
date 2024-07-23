package nsk.enhanced.Player;

import java.util.UUID;

public class Character {

    private UUID uuid;
    private int chatMode;

    public Character(UUID uuid, int chatMode) {
        this.setUUID(uuid);
        this.setChatMode(chatMode);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public void setChatMode(int chatMode) {
        this.chatMode = chatMode;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public UUID getUUID() {
        return uuid;
    }
    public int getChatMode() {
        return chatMode;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //
}
