package rocks.massi.trollsgames.data.trictrac;


import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Boardgame {
    String name;
    List<BoardgameResult> results = new LinkedList<>();
}