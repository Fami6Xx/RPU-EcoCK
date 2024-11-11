package me.fami6xx.rpuecock;

import me.TechsCode.UltraEconomy.UltraEconomy;
import me.TechsCode.UltraEconomy.UltraEconomyAPI;
import me.TechsCode.UltraEconomy.objects.Account;
import me.fami6xx.rpuniverse.core.api.CharacterKilledEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class RPU_EcoCK extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCharacterKill(CharacterKilledEvent event) {
        Player player = event.getPlayer();

        UltraEconomyAPI api = UltraEconomy.getAPI();
        Account account = api.getAccounts().uuid(player.getUniqueId()).orElse(null);
        if (account == null) {
            return;
        }

        api.getBankAccounts().owner(account).ifPresent(bankAccount -> {
            api.getBankAccounts().remove(bankAccount);
        });
        api.getBankAccounts().forPlayer(account).forEach(bankAccount -> {
            bankAccount.removeAccess(account);
        });
        api.getTransactions().parallelStream().filter(transaction -> transaction.getFrom().isPresent() && transaction.getFrom().get().isPresent() && transaction.getFrom().get().get().equals(account) || transaction.getTo().isPresent() && transaction.getTo().get().isPresent() && transaction.getTo().get().get().equals(account)).forEach(transaction -> {
            api.getTransactions().remove(transaction);
        });

        api.getAccounts().remove(account);
    }
}
