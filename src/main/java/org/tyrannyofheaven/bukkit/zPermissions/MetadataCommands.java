/*
 * Copyright 2013 Allan Saddi <allan@saddi.com>
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

import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.colorize;
import static org.tyrannyofheaven.bukkit.util.ToHMessageUtils.sendMessage;
import static org.tyrannyofheaven.bukkit.util.command.reader.CommandReader.abortBatchProcessing;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tyrannyofheaven.bukkit.util.ToHStringUtils;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.Option;
import org.tyrannyofheaven.bukkit.util.command.Session;
import org.tyrannyofheaven.bukkit.util.transaction.TransactionCallback;
import org.tyrannyofheaven.bukkit.util.transaction.TransactionCallbackWithoutResult;
import org.tyrannyofheaven.bukkit.zPermissions.dao.MissingGroupException;
import org.tyrannyofheaven.bukkit.zPermissions.model.EntityMetadata;
import org.tyrannyofheaven.bukkit.zPermissions.model.PermissionEntity;

public class MetadataCommands {

    private final ZPermissionsPlugin plugin;
    
    private final boolean group;

    MetadataCommands(ZPermissionsPlugin plugin, boolean group) {
        this.plugin = plugin;
        this.group = group;
    }

    @Command(value="get", description="Retrieve metadata value")
    public void get(CommandSender sender, final @Session("entityName") String name, final @Option("name") String metadataName) {
        Object result = plugin.getRetryingTransactionStrategy().execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction() throws Exception {
                return plugin.getDao().getMetadata(name, group, metadataName);
            }
        });
        
        if (result == null) {
            sendMessage(sender, colorize("%s%s{YELLOW} does not set {GOLD}%s"), group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name, metadataName);
            abortBatchProcessing();
        }
        else {
            sendMessage(sender, colorize("%s%s{YELLOW} sets {GOLD}%s{YELLOW} to {GREEN}%s"), group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name, metadataName, result);
        }
    }

    @Command(value="set", description="Set metadata (string)")
    public void set(CommandSender sender, final @Session("entityName") String name, final @Option("name") String metadataName, @Option("value") String value, String[] rest) {
        final StringBuilder stringValue = new StringBuilder(value);
        if (rest.length > 0) {
            stringValue.append(' ')
                .append(ToHStringUtils.delimitedString(" ", (Object[])rest));
        }
        set0(sender, name, metadataName, stringValue.toString());
    }

    private void set0(CommandSender sender, final String name, final String metadataName, final Object value) {
        try {
            plugin.getRetryingTransactionStrategy().execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult() throws Exception {
                    plugin.getDao().setMetadata(name, group, metadataName, value);
                }
            });
        }
        catch (MissingGroupException e) {
            handleMissingGroup(sender, e);
            return;
        }

        sendMessage(sender, colorize("{GOLD}%s{YELLOW} set to {GREEN}%s{YELLOW} for %s%s"), metadataName, value == null ? Boolean.TRUE : value, group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name);
    }

    @Command(value="setint", description="Set metadata (integer)")
    public void set(CommandSender sender, final @Session("entityName") String name, final @Option("name") String metadataName, @Option("value") long value) {
        set0(sender, name, metadataName, value);
    }

    @Command(value="setreal", description="Set metadata (real)")
    public void set(CommandSender sender, final @Session("entityName") String name, final @Option("name") String metadataName, @Option("value") double value) {
        set0(sender, name, metadataName, value);
    }

    @Command(value="setbool", description="Set metadata (boolean)")
    public void set(CommandSender sender, final @Session("entityName") String name, final @Option("name") String metadataName, @Option(value="value", optional=true) Boolean value) {
        set0(sender, name, metadataName, value == null ? Boolean.TRUE : value);
    }

    @Command(value="unset", description="Remove metadata value")
    public void unset(CommandSender sender, final @Session("entityName") String name, final @Option("name") String metadataName) {
        Boolean result = plugin.getRetryingTransactionStrategy().execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() throws Exception {
                return plugin.getDao().unsetMetadata(name, group, metadataName);
            }
        });
        
        if (result) {
            sendMessage(sender, colorize("{GOLD}%s{YELLOW} unset for %s%s"), metadataName, group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name);
        }
        else {
            sendMessage(sender, colorize("%s%s{RED} does not set {GOLD}%s"), group ? ChatColor.DARK_GREEN : ChatColor.AQUA, name, metadataName);
            abortBatchProcessing();
        }
    }

    @Command(value={"show", "list", "ls"}, description="List all metadata")
    public void list(CommandSender sender, final @Session("entityName") String name) {
        PermissionEntity entity = plugin.getDao().getEntity(name, group);
        if (entity == null || entity.getMetadata().isEmpty()) {
            sendMessage(sender, colorize("{RED}%s has no metadata."), group ? "Group" : "Player");
            return;
        }

        for (EntityMetadata me : Utils.sortMetadata(entity.getMetadata())) {
            sendMessage(sender, colorize("{GOLD}%s{YELLOW}: {GREEN}%s"), me.getName(), me.getValue());
        }
    }

    private void handleMissingGroup(CommandSender sender, MissingGroupException e) {
        sendMessage(sender, colorize("{RED}Group {DARK_GREEN}%s{RED} does not exist."), e.getGroupName());
        abortBatchProcessing();
    }

}