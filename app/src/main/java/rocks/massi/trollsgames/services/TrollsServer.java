package rocks.massi.trollsgames.services;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import rocks.massi.trollsgames.data.*;

import java.util.List;

public interface TrollsServer {
    @RequestLine("GET /v1/users/get")
    List<User> getUsers();

    @RequestLine("GET /v1/games/get")
    List<Game> getGames();

    @RequestLine("GET /v1/collection/get/{nick}")
    List<Game> getCollectionForUser(@Param("nick") String nick);

    @RequestLine("GET /v1/server/information")
    ServerInformation getInformation();

    @RequestLine("GET /v1/server/quote")
    Quote getQuote();

    @RequestLine("GET /v1/games/search?q={search}")
    List<Game> search(@Param("search") String search);

    @RequestLine("POST /v1/users/register?redirect=tdj://massi.rocks/login")
    @Headers({"Content-Type: application/json"})
    User register(User user);

    @RequestLine("POST /v1/users/login")
    @Headers({"Content-Type: application/json"})
    Response login(LoginInformation loginInformation);
}
