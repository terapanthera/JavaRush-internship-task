package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;


@Service
@Transactional
public class PlayerDataServiceImp implements PlayerDataService {

    @Autowired
    private PlayerRepository playerRepository;

    public PlayerDataServiceImp(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getPlayerForId(long id) {
        if (playerRepository.existsById(id)) {
            return playerRepository.findById(id).get();
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public List<Player> getPlayers(String name, String title, Race race, Profession profession,
                                   Long after, Long before, Boolean banned, Integer minExperience,
                                   Integer maxExperience, Integer minLevel, Integer maxLevel) {
        Date afterDate = after == null ? null : new Date(after);
        Date beforeDate = before == null ? null : new Date(before);
        List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach((player) -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            list.add(player);
        });
        return list;
    }

    @Override
    public List<Player> getSortedPlayers(List<Player> list, PlayerOrder order) {
        if (order != null) {
            switch (order) {
                case ID: {
                    list.sort(Comparator.comparing(Player::getId));
                    break;
                }
                case NAME: {
                    list.sort(Comparator.comparing(Player::getName));
                    break;
                }
                case EXPERIENCE: {
                    list.sort(Comparator.comparing(Player::getExperience));
                    break;
                }
                case BIRTHDAY: {
                    list.sort(Comparator.comparing(Player::getBirthday));
                    break;
                }
            }
        }
        return list;
    }

    public List<Player> getSortedPages(List<Player> list, Integer pageNumber, Integer pageCount) {
        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageCount == null) {
            pageCount = 3;
        }
        int start = pageNumber * pageCount;
        int end = start + pageCount;
        if (end > list.size()) end = list.size();
        return list.subList(start, end);
    }

    @Override
    public void deletePlayer(long id) {
        if (isIdValid(id)) {
            playerRepository.delete(getPlayerForId(id));
        } else throw new BadRequestException();
    }

    @Override
    public Player createNewPlayer(Player player) {
        if ((!isDateValid(player.getBirthday())) ||
                (!isNameValid(player.getName())) ||
                (!isTitleValid(player.getTitle())) ||
                (!isExperienceValid(player.getExperience()))) {
            throw new BadRequestException();
        }
        countLevelAndExperience(player);
        playerRepository.save(player);
        return player;
    }

    @Override
    public Player changePlayer(Player oldPlayer, Player newTmpPlayer) throws IllegalArgumentException {
        String name = newTmpPlayer.getName();
        if (name != null) {
            if (isNameValid(name)) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        String title = newTmpPlayer.getTitle();
        if (title != null) {
            if (isTitleValid(title)) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }
        Race race = newTmpPlayer.getRace();
        if (race != null) {
            oldPlayer.setRace(race);
        }
        Profession profession = newTmpPlayer.getProfession();
        if (profession != null) {
            oldPlayer.setProfession(profession);
        }
        Date birthday = newTmpPlayer.getBirthday();
        if (birthday != null) {
            if (isDateValid(birthday)) {
                oldPlayer.setBirthday(birthday);
            } else {
                throw new IllegalArgumentException();
            }
        }
        Boolean banned = newTmpPlayer.getBanned();
        if (banned != null) {
            oldPlayer.setBanned(banned);
        }
        Integer experience = newTmpPlayer.getExperience();
        if (experience != null) {
            if (isExperienceValid(experience)) {
                oldPlayer.setExperience(experience);

            } else {
                throw new IllegalArgumentException();
            }
        }
        countLevelAndExperience(oldPlayer);
        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    private void countLevelAndExperience(Player player) {
        player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
    }

    private boolean isNameValid(String name) {
        return name.length() <= 12 && !name.isEmpty();
    }

    private boolean isTitleValid(String title) {
        return title.length() <= 30 && !title.isEmpty();
    }

    private boolean isDateValid(Date id) {
        if (id == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        Date after = new Date();
        Date before = new Date();
        calendar.set(1999, Calendar.DECEMBER, 31);
        after = calendar.getTime();
        calendar.set(3000, Calendar.DECEMBER, 31);
        before = calendar.getTime();
        return (id.before(before) && id.after(after));
    }

    private boolean isExperienceValid(Integer experience) {
        return experience >= 0 && experience <= 10000000;
    }

    private boolean isIdValid(long id) {
        return id > 0;
    }
}
