package rocks.massi.trollsgames.services;

import feign.Param;
import feign.RequestLine;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.ServerInformation;
import rocks.massi.trollsgames.data.User;

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
}
