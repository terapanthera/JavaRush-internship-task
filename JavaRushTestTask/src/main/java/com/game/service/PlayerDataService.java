package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.List;

public interface PlayerDataService {

    Player getPlayerForId(long id);

    List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after,
                            Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                            Integer minLevel, Integer maxLevel);

    List<Player> getSortedPages(List<Player> list, Integer pageNumber, Integer pageCount);

    List<Player> getSortedPlayers(List<Player> list, PlayerOrder order);

    Player createNewPlayer(Player player);

    Player changePlayer(Player oldPlayer, Player newTmpPlayer);

    void deletePlayer(long id);
}
