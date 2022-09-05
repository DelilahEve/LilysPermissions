# LilysPermissions
A Simple permissions and chat plugin for Bukkit, Spigot, and Paper!

Need a hand figuring out permissions? The plugin ships with comprehensive examples for all configuration files. All examples have extensive documentation to demonstrate what this plugin is capable of 💜

### Features:
- Group permissions
  - Can be configured per-world
  - Can be world-restricted
- Group prefix/suffix
  - Can be configured per-world
- Per-user permissions
- Per-user prefix/suffix
- Configurable prefix/suffix modes (combine vs override)
- Full colour formatting for player display names
  - Supports strikethrough, bold, magic, and more!

### Commands and Permissions:

| Permission | Command | Description |
| --- | --- | --- |
| `lilys_permissions.promote` | `/promote <user> [ladder]` | Promote the given `<user>`</br>with option to specify a `[ladder]`.</br></br>If none is given, the plugin will</br>attempt to determine the ladder. |
| `lilys_permissions.demote` | `/demote <user> [ladder]` | Demote the given `<user>`</br>with option to specify a `[ladder]`.</br></br>If none is given, the plugin will</br>attempt to determine the ladder. |
| `lilys_permissions.set_group` | `/set-group <user> <group1> [group2]...` | Set the group(s) for the given `<user>`.</br></br>This will overwrite `<user>`'s</br>existing groups. |
| `lilys_permissions.add_group` | `/add-group <user> <group1> [group2]...` | Add group(s) to the given `<user>`. |
| `lilys_permissions.reload` | `/lp-reload` | Reload permissions for all players.</br></br>This will take any changes</br>made to `permissions.yml`</br>and`users.yml` into account. |

### Default Configurations:
  
<details>
<summary>permissions.yml</summary>

```yml
# A note on reading this documentation: Players may at some points be
# referred to as users, or members, and groups may at points be referred to
# as roles  or ranks. I consider users and players the same thing, same for
# groups, roles, and ranks. :)
#
# All permissions groups must be defined in this "groups" node.
# Any groups defined outside this node will be ignored.
# Groups should be defined in ascending order, meaning your lowest rank
# should be defined first, and your highest rank last. When determining
# permissions and chat formatting for players, if a conflict arises between
# assigned groups, the plugin will take the highest group as the "correct"
# one. This means if you assign a world-specific role (i.e. a creative world
# role) to a user with a more broad role (one that applies to any role),
# the one to come later in the list will be the one to take precedent.
#
# If you wish to use colours/formatting you may use any of these replacement keys:
#
# Black ---------- &0    Green ------------ &a    Italic --------- &o
# Dark Blue ------ &1    Aqua ------------- &b    Reset ---------- &r
# Dark Green ----- &2    Red -------------- &c
# Dark Aqua ------ &3    Light Purple ----- &d
# Dark Red ------- &4    Yellow ----------- &e
# Dark Purple ---- &5    White ------------ &f
# Gold ----------- &6    Magic ------------ &k
# Gray ----------- &7    Bold ------------- &l
# Dark Gray ------ &8    Strikethrough ---- &m
# Blue ----------- &9    Underline -------- &n
#
# Use of replacement keys requires wrapping the entire string in "", otherwise
# the YAML will fail to parse. If you need to validate your file you can use
# http://www.yamllint.com/
#
# These formatting keys are Bukkit's ChatColor values and can be found in their
# documentation. These values are standard to all Bukkit-based servers including
# Spigot and Paper.
groups:

  # Example of a default group, this will be the group given to
  # any player on their first join of the server.
  default:
    # Having default: true here is how we know this is the default group.
    # Only the first group defined with this will be treated as default.
    default: true
    # Chat prefix to be displayed for users in this group, supports colour coding.
    prefix: "&a[Default]"
    # Chat suffix to be displayed for users in this group. It's recommended to have
    # &r as the final text here as that will return the player's text to the normal
    # white colour, without &r the player's text will be coloured as per the prefix.
    suffix: "&r"
    # list of permissions given to this group's members.
    permissions:
    - some_plugin.basic_permission

  # Example of a nether default group. This would be assigned as the default role
  # when a user's first joined world is the nether (as opposed to the over-world)
  # This supports any world so long as naming matches internal naming. If you're
  # not sure of the name to use, you'd be best to contact the developer of the
  # custom world to find out.
  netherDefault:
    # In addition to supporting 'true', default can be set to a world name. This
    # is how we achieve defaults for alternate worlds.
    default: world_nether
    prefix: "&4[Nether-Default]"
    suffix: "&r"
    # We can define an 'inherit' node and specify the names of groups we wish to
    # inherit permissions, prefixes, and suffixes from. Permission lists will be
    # merged, and prefix/suffix will only be inherited if not explicitly defined.
    inherit:
    - default
    permissions:
    - some_plugin.basic_permission

  # Example of a world-specific group. This role and it's associated properties would
  # only be applied in the worlds listed, and will override any previously defined
  # groups the player is a member of. If the user is a member of a group defined after
  # this one, and that role can be applied to the world(s) listed, that role will
  # take precedent.
  worldSpecificGroup:
    prefix: "&3[world only group]"
    suffix: "&r"
    # We can define a worlds node to specify which worlds this group can be applied in.
    # If not defined, the group will be assumed to be globally available.
    worlds:
    - world_end
    permissions:
    - some_plugin.basic_permission

  # Example of a moderator role whose permissions and prefix/suffix are defined per-world.
  mod:
    prefix: "&2[Mod]"
    suffix: "&r"
    inherit:
    - default
    # We can define a node with the name of a world to override prefix, suffix, and permissions
    # when the player is in that world:
    world:
      prefix: "&2[World Mod]"
      suffix: "&r"
      permissions:
      - some.world.permission
    world_nether:
      prefix: "&4[Nether Mod]"
      suffix: "&r"
      permissions:
      - some.nether.permission
    # We can still define global permissions for this group in addition to world-specific ones:
    permissions:
    - global.permission

  # Example of an admin group given a wildcard permission
  admin:
    prefix: "&c[Admin]"
    suffix: "&r"
    inherit:
    - mod
    # We can define wildcard permissions, even when other plugins don't explicitly support
    # them. This is achieved by placing a '*' at the point you wish to grant any descendant
    # permissions. This can be dangerous if a plugin does not explicitly support wildcards,
    # be sure to double-check the target plugin's permission inheritance to ensure you're
    # only handing out permissions you intend to!
    permissions:
    - "some_plugin.*"

  # Example of an owner group given all permissions
  owner:
    prefix: "&d[Owner]"
    suffix: "&r"
    # We can define a true wildcard permission with a simple '*'. This grants members of this
    # group all permissions, acting as a bypass on all permission checks. This is to say that
    # members of this group will effectively not be checked for having permissions, instead
    # being given carte-blanche to perform any action or command
    permissions:
    - "*"
    # We can define a denyPermissions node to force-disable permissions. This is useful when you
    # have a small number of permissions you don't want a group to have. Instead of listing off the
    # hundreds of permissions you do want, you can simply deny the handful you don't want.
    #
    # Denied permissions do not support the '*' wildcard intentionally to avoid wasted processor cycles
    # should it be used in the "permissions" list.
    #
    # Denied permissions will be applied after the permissions list, meaning they will overwrite any
    # granted permissions, either from this group or inherited groups.
    #
    # Akin to the "permissions" list, this will inherit from any groups defined in the "inherit" list.
    #
    # The "denyPermissions" node can also be defined in a world override.
    denyPermissions:
    - some.permission

# Rank ladder lists allow you to quickly /promote and /demote users within the first ladder
# where they have a group defined. You can optionally provide a ladder name to promote/demote
# a user within when using the /promote and /demote commands to avoid the plugin choosing the
# ladder to move a player within.
#
# Ladders should be organized ascending.
ladders:
  default:
  - default
  - mod
  - admin
  - owner

```
</details>

<details>
<summary>users.yml</summary>

```yml
# Users (players) recorded here can be provided with overriding properties.
# It's recommended to read through permissions.yml before going over this file.
#
# Users do not have to be recorded here if they're only being given default
# permissions (though you can configure the plugin such that all players are).
#
# Users who are given non-default groups are automatically recorded here and
# should not be removed otherwise the plugin will forget any groups they've
# been assigned.
#
# If you wish to use colours/formatting you may use any of these replacement keys:
#
# Black ---------- &0    Green ------------ &a    Italic --------- &o
# Dark Blue ------ &1    Aqua ------------- &b    Reset ---------- &r
# Dark Green ----- &2    Red -------------- &c
# Dark Aqua ------ &3    Light Purple ----- &d
# Dark Red ------- &4    Yellow ----------- &e
# Dark Purple ---- &5    White ------------ &f
# Gold ----------- &6    Magic ------------ &k
# Gray ----------- &7    Bold ------------- &l
# Dark Gray ------ &8    Strikethrough ---- &m
# Blue ----------- &9    Underline -------- &n
#
# Use of replacement keys requires wrapping the entire string in "", otherwise
# the YAML will fail to parse. If you need to validate your file you can use
# http://www.yamllint.com/
#
# These formatting keys are Bukkit's ChatColor values and can be found in their
# documentation. These values are standard to all Bukkit-based servers including
# Spigot and Paper.
users:

  # User's entry, should be their UUID. If you need to find a UUID for a player
  # to manually add them, you can use: https://mcuuid.net/
  #
  # This entry is for Dinnerbone, and whilst it's a very low chance he'd join
  # your server at random, it'd be good security to remove this entry as it's
  # only for demonstration. If you do remove it, place [] after the "users" node
  # to ensure the file formatting remains valid.
  61699b2e-d327-4a01-9f1e-0ea8c3f06bc6:
    # Groups this user is part of
    groups:
      - netherDefault
      - admin
    # Prefix this user should have instead of their group's
    prefix: "&k[Upside Down]"
    # Suffix this user should have instead of their group's
    suffix: "&r"
    # Permissions this user should be given in addition to their group's
    permissions:
      - upsidedown.allow
    # Permissions this user should be denied in addition to their group's
    denyPermissions:
      - rightsideup.allow

```
</details>

<details>
<summary>config.yml</summary>

```yml
# Should we save player's group information if they only have default groups?
# Note that once a player is given a non-default role they will be recorded
# in users.yml and this setting is only in regard to players with only a
# default group.
#
# It's recommended to turn this off for very large servers.
save_all_players: true

# Toggle player name formatting
chat_formatting: true

# Whether the plugin should alter display names in the player list (tab list)
tab_list_formatting: true

# Whether the plugin should only alter tab list names for whitelisted groups
tab_list_restrict_groups: false

# List of group names whose member's names should be altered in the tab list
#
# This list will only take effect if "tab_list_restrict_groups" is true
tab_list_groups: []

# What "mode" should a user's prefix operate in? Valid options are:
#
# override - This will result in the prefix a user is given in users.yml
#            replacing the prefix given to them by their assigned group
#
# combine  - This will result in the prefix a user is given in users.yml
#            being placed before the group's prefix
user_prefix_mode: override

# What "mode" should a user's suffix operate in? Valid options are
# the same as prefix modes.
user_suffix_mode: override

# Whether a space should be inserted after the prefix and before the player's name
space_after_prefix: true

# Whether a space should be inserted after the player's name and before the suffix
space_before_suffix: true

```
</details>
  
