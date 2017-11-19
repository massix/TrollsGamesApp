package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.Game;

import java.util.List;

@RequiredArgsConstructor
@Data
public class SearchFinishedEvent {
    private final List<Game> games;
}
