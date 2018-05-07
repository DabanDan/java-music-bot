update guild_settings
set
    prefix = ?
where guild_id = ?;
