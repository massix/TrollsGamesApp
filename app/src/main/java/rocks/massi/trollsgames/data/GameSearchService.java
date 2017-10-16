package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GameSearchService {
    private final ThirdPartyServices service;
    private final Game game;
    private final String url;
    private final String displayName;
}
