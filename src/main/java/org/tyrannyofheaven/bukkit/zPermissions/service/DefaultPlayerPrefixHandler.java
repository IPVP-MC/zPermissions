/*
 * Copyright 2014 ZerothAngel <zerothangel@tyrannyofheaven.org>
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
package org.tyrannyofheaven.bukkit.zPermissions.service;

import java.util.UUID;

import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsConfig;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;
import org.tyrannyofheaven.bukkit.zPermissions.util.MetadataConstants;

public class DefaultPlayerPrefixHandler implements PlayerPrefixHandler {

    private final ZPermissionsConfig config;

    public DefaultPlayerPrefixHandler(ZPermissionsConfig config) {
        this.config = config;
    }

    @Override
    public String getPlayerPrefix(ZPermissionsService service, UUID uuid) {
        String prefix = service.getPlayerMetadata(uuid, MetadataConstants.PREFIX_KEY, String.class);
        return prefix == null ? "" : prefix;
    }
    
    @Override
    public String getPlayerSuffix(ZPermissionsService service, UUID uuid) {
        String suffix = service.getPlayerMetadata(uuid, MetadataConstants.SUFFIX_KEY, String.class);
        return suffix == null ? "" : suffix;
    }
}
