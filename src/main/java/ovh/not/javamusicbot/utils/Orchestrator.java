package ovh.not.javamusicbot.utils;

import net.dv8tion.jda.core.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class Orchestrator {
    private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);
    private ConcurrentMap<Integer, CountDownLatch> shardMap;
    private Jedis jedis;

    public Orchestrator(String host) {
        jedis = new Jedis(host);
        new Thread(this::run).run();
    }

    void run() {
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                logger.info("subscribe: {}", channel);
            }

            @Override
            public void onMessage(String channel, String message) {
                if (channel != "shards:cluster") return;
                if (message.startsWith("start:")) {
                    Integer shard = Integer.parseInt(message.substring(6));
                    CountDownLatch latch = shardMap.getOrDefault(shard, null);
                    if (latch != null) {
                        latch.countDown();
                    }
                } else {
                    logger.warn("master sent unknown payload: {}", message);
                }
            }
        }, "shards:cluster");
    }

    public void requestShardAndWait(JDA.ShardInfo shardInfo) {
        Integer id = shardInfo.getShardId();
        CountDownLatch latch = new CountDownLatch(1);

        shardMap.put(id, latch);

        logger.info("entering shard {}", id);
        String message = String.format("shards:%d", id);
        jedis.publish("shards:master", message);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("clear for shard {}", id);
    }

    public void announceStarted(JDA.ShardInfo shardInfo) {
        String message = String.format("started:%d", shardInfo.getShardId());
        jedis.publish("shards:master", message);
    }
}
