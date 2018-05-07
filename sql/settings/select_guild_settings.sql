select
    id, guild_id, prefix
from guild_settings
where guild_id = ?;
