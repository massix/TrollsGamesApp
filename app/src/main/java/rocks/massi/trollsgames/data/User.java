package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class User {
    private final String bggNick;
    private final String forumNick;
    private final String games;
    private final String wants;

    private List<Integer> collection;
    private List<Game> gamesCollection;

    @Override
    public String toString() {
        return forumNick;
    }
}
