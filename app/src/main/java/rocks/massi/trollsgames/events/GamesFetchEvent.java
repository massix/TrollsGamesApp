package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.Game;

import java.util.List;

@Data
@RequiredArgsConstructor
public class GamesFetchEvent {
    private final boolean finished;
    private final List<Game> games;
}
