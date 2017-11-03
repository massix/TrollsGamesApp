package rocks.massi.trollsgames.data.trictrac;

import lombok.Data;

@Data
public class SearchResponse {
    Results results = new Results();
    Action action = new Action();
}
