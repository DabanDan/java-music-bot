with song as (
    insert into songs (
        track_data
    ) values (
        $2
    ) on conflict (track_data) do update set
        track_data = songs.track_data
    returning songs.song_id
)
insert into queues (
    guild_id,
    song_ids
) values (
    $1,
    ARRAY[(select song_id from song)]
) on conflict (guild_id) do update set
    song_ids = array_append(queues.song_ids, (select song_id from song));