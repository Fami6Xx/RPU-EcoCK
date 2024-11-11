package me.fami6xx.rpuecock;

import me.TechsCode.UltraEconomy.UltraEconomy;
import me.TechsCode.UltraEconomy.UltraEconomyAPI;
import me.TechsCode.UltraEconomy.objects.Account;
import me.TechsCode.UltraEconomy.objects.BankAccount;
import me.fami6xx.rpuniverse.core.api.CharacterKilledEvent;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class RPU_EcoCK extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCharacterKill(CharacterKilledEvent event) {
        Player player = event.getPlayer();

        UltraEconomyAPI api = UltraEconomy.getAPI();
        Account account = api.getAccounts().uuid(player.getUniqueId()).orElse(null);
        if (account == null) {
            return;
        }

        getLogger().info("Player " + player.getName() + " has been killed. Resetting their economy data.");
        getLogger().info("Checking if player has bank account");
        if (account.getOwnBankAccount().isPresent()) {
            getLogger().info("Player has bank account. Deleting it.");
            BankAccount bankAccount = account.getOwnBankAccount().get();
            bankAccount.delete();
            bankAccount.issueSync();
        }

        getLogger().info("Checking if player has shared bank accounts");
        if (account.getSharedBankAccounts().owner(account).isPresent()) {
            getLogger().info("Player has shared bank accounts. Deleting them.");
            BankAccount bankAccount = account.getSharedBankAccounts().owner(account).get();
            bankAccount.delete();
            bankAccount.issueSync();
        }

        getLogger().info("Checking if player has any bank accounts with access");
        account.getSharedBankAccounts().forPlayer(account).forEach(bankAccount -> {
            getLogger().info("Player has bank account with access. Deleting it.");
            bankAccount.removeAccess(account);
            bankAccount.removeStandingOrder(account);
            bankAccount.issueSync();
        });

        getLogger().info("Checking if player has any transactions");
        api.getTransactions().forEach(transaction -> {
            if (
                    transaction.getFrom().isPresent() && transaction.getFrom().get().isPresent() && transaction.getFrom().get().get().equals(account)
                    || transaction.getTo().isPresent() && transaction.getTo().get().isPresent() && transaction.getTo().get().get().equals(account)
            ) {
                getLogger().info("Player has transaction. Deleting it.");
                transaction.remove();
            }
        });

        getLogger().info("Deleting player account data.");
        account.delete();
        account.issueSync();

        getLogger().info("Player " + player.getName() + " has been reset.");
        player.kickPlayer(FamiUtils.formatWithPrefix("&7CK bylo provedeno."));
    }
}
