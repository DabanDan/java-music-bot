package ovh.not.javamusicbot.utils;

import net.dv8tion.jda.core.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Orchestrator extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);
    private Map<Integer, CountDownLatch> shardMap = new ConcurrentHashMap<>();
    private final JedisPool jedisPool;
    private final Jedis subscriber;

    public Orchestrator(String host) {
        super("orchestrator");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(poolConfig, host);
        subscriber = jedisPool.getResource();
    }

    @Override
    public void run() {
        logger.debug("jedis subscribing");
        subscriber.subscribe(new Subscriber(), "shards:cluster");
    }

    public void close() {
        logger.debug("closing jedis subscriber");
        subscriber.close();
        logger.debug("cleaning jedis pool");
        jedisPool.close();
    }

    public void requestShardAndWait(JDA.ShardInfo shardInfo) {
        Integer id = shardInfo.getShardId();
        CountDownLatch latch = new CountDownLatch(1);

        shardMap.put(id, latch);

        logger.info("entering shard {}", id);
        String message = String.format("shards:%d", id);

        // try with resources automatically returns the resource
        try (Jedis publisher = jedisPool.getResource()) {
            publisher.publish("shards:master", message);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("clear for shard {}", id);

        shardMap.remove(id);
    }

    public void announceStarted(JDA.ShardInfo shardInfo) {
        String message = String.format("started:%d", shardInfo.getShardId());

        try (Jedis publisher = jedisPool.getResource()) {
            publisher.publish("shards:master", message);
        }
    }

    private class Subscriber extends JedisPubSub {
        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            logger.info("subscribe: {}", channel);
        }

        @Override
        public void onMessage(String channel, String message) {
            if (!channel.equals("shards:cluster")) return;
            if (message.startsWith("start:")) {
                int shard = Integer.parseInt(message.substring(6));
                logger.debug("received start for shard {}", shard);
                CountDownLatch latch = shardMap.getOrDefault(shard, null);
                if (latch != null) {
                    latch.countDown();
                }
            } else {
                logger.warn("master sent unknown payload: {}", message);
            }
        }
    }
}
