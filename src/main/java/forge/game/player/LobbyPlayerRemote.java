package forge.game.player;

import org.apache.commons.lang.NotImplementedException;

import forge.game.GameState;
import forge.net.client.INetClient;
import forge.net.protocol.outcoming.ChatMessage;

public class LobbyPlayerRemote extends LobbyPlayer {
    
    private final INetClient connection;
    
    public LobbyPlayerRemote(String name, INetClient netClient) { // This is actually a doubtful idea - this means 1 window per player.
        super(name);
        connection = netClient;
    }

    @Override
    public PlayerType getType() {
        return PlayerType.REMOTE;
    }

    /* (non-Javadoc)
     * @see forge.game.player.LobbyPlayer#getPlayer(forge.game.GameState)
     */
    @Override
    public Player getPlayer(GameState gameState) {
        // Cannot create remote players yet 
        throw new NotImplementedException();
    }

    @Override
    public void hear(LobbyPlayer player, String message) {
        connection.send(new ChatMessage(player.getName(), message));
    }
}