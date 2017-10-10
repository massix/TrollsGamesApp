package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.Game;

@Data
@RequiredArgsConstructor
public class GameFetchedEvent {
    private final Game game;
}
