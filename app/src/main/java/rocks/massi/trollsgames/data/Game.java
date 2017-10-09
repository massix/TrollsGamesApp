package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class Game implements Comparable<Game>, Serializable {
    private final int id;
    private final String name;
    private final String description;
    private final int minPlayers;
    private final int maxPlayers;
    private final int playingTime;
    private final int yearPublished;
    private final int rank;
    private final boolean extension;
    private final String thumbnail;
    private final String authors;
    private final String expands;

    @Override
    public int compareTo(Game o) {
        return name.compareTo(o.getName());
    }
}
