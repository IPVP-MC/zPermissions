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

/**
 * Holder/parser for world-specific permissions as specified on the command-line.
 * Permissions with no world specifier assume the world is null (i.e. global
 * permission). Permissions specific to a world should be "&lt;world>:&lt;permission>"
 * 
 * @author asaddi
 */
class WorldPermission {

    private final String world;
    
    private final String permission;

    WorldPermission(String worldPermission) {
        // Break up into world/permission, as appropriate
        String[] parts = worldPermission.split(":", 2);
        if (parts.length == 1) {
            // No world
            world = null;
            permission = parts[0];
        }
        else {
            world = parts[0];
            permission = parts[1];
        }
    }

    /**
     * Return the name of the world if this is a world-specific permission.
     * 
     * @return the name of the world or null if global
     */
    public String getWorld() {
        return world;
    }

    /**
     * Return the permission.
     * 
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

}
