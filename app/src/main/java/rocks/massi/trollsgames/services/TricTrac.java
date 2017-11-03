package rocks.massi.trollsgames.services;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import rocks.massi.trollsgames.data.trictrac.SearchResponse;

public interface TricTrac {

    @Headers({
        "Accept: application/json",
        "X-Requested-With: XMLHttpRequest"
    })
    @RequestLine("GET /recherche?search={game}&limit={limit}")
    SearchResponse search(@Param("game") final String game, @Param("limit") final int limit);

}
