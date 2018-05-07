with ids as (
    select song_ids[1] from queues
    where guild_id = $1
),
 up as (
    update queues
        set song_ids = song_ids[2:]
    where guild_id = $1
)
select track_data from songs
where song_id = (select song_ids from ids);