package command;

import data.PlayerScore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.micra_twentyfive.Main;

public class EnemyDownCommand implements CommandExecutor, Listener {

  private final Main main;
  private int gameTime = 20;
  private final List<PlayerScore> playerScoreList = new ArrayList<>();

  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] strings) {
    if (commandSender instanceof Player player) {
      if (playerScoreList.isEmpty()) {
        addNewPlayer(player);
      } else {
        boolean playerExists = playerScoreList.stream()
            .anyMatch(playerScore -> playerScore.getPlayerName().equals(player.getName()));
        if (!playerExists) {
          addNewPlayer(player);
        }
      }

      gameTime = 60;
      World world = player.getWorld();

      initPlayerStatus(player);

      // 時間制限
      Bukkit.getScheduler().runTaskTimer(main, task -> {
        // 時間制限キャンセル
        if (gameTime <= 0) {
          task.cancel();
          player.sendMessage("ゲーム終了。");
          return;
        }
        // 時間制限開始
        world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
        gameTime -= 5;
      }, 0, 5 * 20);
    }
    return true;
  }

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    Player player = e.getEntity().getKiller();
    // イベント実行前と実行後で、プレイヤーがNullであれば、スキップする(Null対策)
    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    for (PlayerScore playerScore : playerScoreList) {
      if (playerScore.getPlayerName().equals(player.getName())) {
        playerScore.setScore(playerScore.getScore() + 10);
        player.sendMessage("敵を倒した！ 現在のスコアは、" + playerScore.getScore() + "点です。");
      }
    }
  }

  private void addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
  }

  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);
    player.setAllowFlight(true);
    player.setFlying(true);
    player.setFlySpeed(1f);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    inventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
  }

  private Location getEnemySpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x, y, z);
  }

  private EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE, EntityType.SPIDER,
        EntityType.SKELETON);
    int random = new SplittableRandom().nextInt(enemyList.size());
    return enemyList.get(random);
  }
}
