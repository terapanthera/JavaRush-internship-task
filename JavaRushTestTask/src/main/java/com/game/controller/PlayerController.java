package com.game.controller;


import com.game.exceptions.BadRequestException;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class PlayerController {

    PlayerDataService playerDataService;

    public PlayerController() {
    }

    @Autowired
    public PlayerController(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    @GetMapping("/players/{id}")
    public Player getId(@PathVariable("id") String id) {
        return playerDataService.getPlayerForId(convertIdToLong(id));
    }

    @GetMapping("/players")
    public List<Player> getList(@RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "race", required = false) Race race,
                                @RequestParam(value = "profession", required = false) Profession profession,
                                @RequestParam(value = "after", required = false) Long after,
                                @RequestParam(value = "before", required = false) Long before,
                                @RequestParam(value = "banned", required = false) Boolean banned,
                                @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                @RequestParam(value = "order", required = false) PlayerOrder order,
                                @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        final List<Player> list = playerDataService.getPlayers(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        final List<Player> sortedList = playerDataService.getSortedPlayers(list, order);
        return playerDataService.getSortedPages(sortedList, pageNumber, pageSize);
    }

    @GetMapping("/players/count")
    public Integer getCount(@RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "race", required = false) Race race,
                            @RequestParam(value = "profession", required = false) Profession profession,
                            @RequestParam(value = "after", required = false) Long after,
                            @RequestParam(value = "before", required = false) Long before,
                            @RequestParam(value = "banned", required = false) Boolean banned,
                            @RequestParam(value = "minExperience", required = false) Integer minExperience,
                            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                            @RequestParam(value = "minLevel", required = false) Integer minLevel,
                            @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        return playerDataService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel).size();
    }

    @PostMapping("/players")
    public Player addPlayer(@RequestBody Player player) {
        return playerDataService.createNewPlayer(player);
    }

    @DeleteMapping("/players/{id}")
    public void deletePlayer(@PathVariable("id") String id) {
        playerDataService.deletePlayer(convertIdToLong(id));
    }

    @PostMapping("/players/{id}")
    public Player updatePlayer(
            @PathVariable(value = "id") String id,
            @RequestBody Player player) {
        final Player old = playerDataService.getPlayerForId(convertIdToLong(id));
        if (old == null) {
            throw new BadRequestException();
        }
        Player result;
        try {
            result = playerDataService.changePlayer(old, player);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException();
        }
        return result;
    }

    private Long convertIdToLong(String id) {
        if (id == null) {
            return null;
        } else
            try {
                Long result = Long.parseLong(id);
                if (result <= 0) {
                    throw new BadRequestException();
                }
                return result;
            } catch (NumberFormatException e) {
                throw new BadRequestException();
            }
    }
}
