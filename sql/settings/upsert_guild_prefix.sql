insert into guild_settings (
    guild_id,
    prefix
) values (
    ?,
    ?
) on conflict (guild_id) do update set
    prefix = ?;
