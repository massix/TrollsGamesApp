package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class User {
    private final String bggNick;
    private final String forumNick;
    private final String email;
    private final String password;
    private final boolean bggHandled;

    private List<Integer> collection;
    private List<Game> gamesCollection;

    @Override
    public String toString() {
        return forumNick;
    }
}
