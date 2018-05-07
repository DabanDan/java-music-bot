select
    guild_id, prefix
from guild_settings
where
    prefix is not null;
