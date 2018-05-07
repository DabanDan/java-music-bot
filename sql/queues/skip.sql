update queues set
    song_ids = queues.song_ids[$2:]
where guild_id = $1;