select track_data
from songs
where song_id in (
    select unnest(song_ids)
    from queues
    where guild_id = $1
);
