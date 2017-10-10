package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.Game;

@Data
@RequiredArgsConstructor
public class GameSelectedEvent {
    private final Game game;
}
