package rocks.massi.trollsgames.services;

import feign.Param;
import feign.RequestLine;
import rocks.massi.trollsgames.data.PhilibertSearchResponse;

import java.util.List;

public interface Philibert {
    @RequestLine("GET /fr/recherche?q={game}&limit={limit}&timestamp={date}&ajaxSearch=1&id_lang=1")
    List<PhilibertSearchResponse> search(@Param("game") final String game,
                                         @Param("limit") final int limit,
                                         @Param("date") final String timestamp);
}
