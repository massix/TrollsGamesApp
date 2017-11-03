package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.trictrac.BoardgameResult;

@Data
@RequiredArgsConstructor
public class GameFoundOnTricTracEvent {
    final BoardgameResult boardgameResult;
}
