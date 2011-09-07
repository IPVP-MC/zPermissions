/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tyrannyofheaven.bukkit.zPermissions;

import static org.tyrannyofheaven.bukkit.util.ToHUtils.colorize;
import static org.tyrannyofheaven.bukkit.util.ToHUtils.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.Option;
import org.tyrannyofheaven.bukkit.util.command.Session;
import org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback;
import org.tyrannyofheaven.bukkit.util.transaction.TransactionCallbackWithoutResult;

/**
 * Handler for common commands between "/permissions group" and
 * "/permissions player"
 * 
 * @author asaddi
 */
public abstract class CommonCommand {

    // true if this is handling groups
    private final boolean group;

    /**
     * Instantiate this handler.
     * 
     * @param group true if this is handling groups
     */
    protected CommonCommand(boolean group) {
        this.group = group;
    }

    @Command(value="get", description="View a permission")
    public void get(final ZPermissionsPlugin plugin, CommandSender sender, final @Session("entityName") String name, @Option("permission") String permission) {
        // Get world/permission
        final WorldPermission wp = new WorldPermission(permission);

        // Read entry from DAO, if any
        Boolean result = plugin.getTransactionStrategy().execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() throws Exception {
                return plugin.getDao().getPermission(name, group, wp.getWorld(), wp.getPermission());
            }
        });
        
        if (result == null) {
            sendMessage(sender, colorize("%s%s{YELLOW} does not set {GOLD}%s"), group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name, permission);
            if (!group)
                checkPlayer(plugin, sender, name);
        }
        else {
            sendMessage(sender, colorize("%s%s{YELLOW} sets {GOLD}%s{YELLOW} to {GREEN}%s"), group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name, permission, result);
        }
    }

    @Command(value="set", description="Set a permission")
    public void set(final ZPermissionsPlugin plugin, CommandSender sender, final @Session("entityName") String name, @Option("permission") String permission, final @Option(value="value", optional=true) Boolean value) {
        // Get world/permission
        final WorldPermission wp = new WorldPermission(permission);
    
        // Set permission. Should never fail. World/entity will be created as needed.
        plugin.getTransactionStrategy().execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult() throws Exception {
                plugin.getDao().setPermission(name, group, wp.getWorld(), wp.getPermission(), value == null ? Boolean.TRUE : value);
            }
        });
    
        sendMessage(sender, colorize("{GOLD}%s{YELLOW} set to {GREEN}%s{YELLOW} for %s%s"), permission, value == null ? Boolean.TRUE : value, group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name);
        if (!group)
            checkPlayer(plugin, sender, name);
        plugin.refreshPlayers();
    }

    @Command(value="unset", description="Remove a permission")
    public void unset(final ZPermissionsPlugin plugin, CommandSender sender, final @Session("entityName") String name, @Option("permission") String permission) {
        // Get world/permission
        final WorldPermission wp = new WorldPermission(permission);
    
        // Delete permission entry.
        Boolean result = plugin.getTransactionStrategy().execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() throws Exception {
                return plugin.getDao().unsetPermission(name, group, wp.getWorld(), wp.getPermission());
            }
        });

        if (result) {
            sendMessage(sender, colorize("{GOLD}%s{YELLOW} unset for %s%s"), permission, group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name);
            plugin.refreshPlayers();
        }
        else {
            sendMessage(sender, colorize("%s%s{RED} does not set {GOLD}%s"), group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name, permission);
            if (!group)
                checkPlayer(plugin, sender, name);
        }
    }

    @Command(value="purge", description="Delete this group or player") // doh!
    public void delete(final ZPermissionsPlugin plugin, CommandSender sender, final @Session("entityName") String name) {
        boolean result = plugin.getTransactionStrategy().execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() throws Exception {
                return plugin.getDao().deleteEntity(name, group);
            }
        });
        
        if (result)
            sendMessage(sender, colorize("{YELLOW}%s %s%s{YELLOW} deleted"),
                    (group ? "Group" : "Player"),
                    (group ? ChatColor.DARK_GREEN : ChatColor.AQUA),
                    name);
        else
            sendMessage(sender, colorize("{RED}%s not found."), group ? "Group" : "Player");
    }

    /**
     * Give a little warning if the player isn't online.
     * 
     * @param playerName the player name
     */
    protected void checkPlayer(ZPermissionsPlugin plugin, CommandSender sender, String playerName) {
        if (plugin.getServer().getPlayer(playerName) == null) {
            sendMessage(sender, colorize("{GRAY}WARNING: Player is not online, be sure the name is spelled\n{GRAY}correctly!"));
        }
    }

}
