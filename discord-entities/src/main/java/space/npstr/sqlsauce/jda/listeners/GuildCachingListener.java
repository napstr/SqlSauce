/*
 * MIT License
 *
 * Copyright (c) 2017, Dennis Neufeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package space.npstr.sqlsauce.jda.listeners;

import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.update.GenericGuildUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.sqlsauce.AsyncDatabaseWrapper;
import space.npstr.sqlsauce.DatabaseException;
import space.npstr.sqlsauce.DatabaseWrapper;
import space.npstr.sqlsauce.entities.discord.BaseDiscordGuild;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by napster on 20.10.17.
 * <p>
 * Caches entities that extend DiscordGuild
 * <p>
 * Limitation: Currently only events relevant for DiscordGuild are listened to. Extending classes might be interested in
 * more events.
 */
public class GuildCachingListener<E extends BaseDiscordGuild<E> & CacheableGuild<E>> extends CachingListener<E, GuildCachingListener<E>> {

    private static final Logger log = LoggerFactory.getLogger(GuildCachingListener.class);

    private final Consumer<Consumer<DatabaseWrapper>> wrapperAdapter;

    public GuildCachingListener(DatabaseWrapper wrapper, final Class<E> entityClass) {
        super(entityClass);
        this.wrapperAdapter = wrapperConsumer -> wrapperConsumer.accept(wrapper);
    }

    public GuildCachingListener(AsyncDatabaseWrapper asyncWrapper, final Class<E> entityClass) {
        super(entityClass);
        this.wrapperAdapter = wrapperConsumer -> {
            Function<DatabaseWrapper, Void> consumerAdapter = wrapper -> {
                wrapperConsumer.accept(wrapper);
                return null;
            };
            asyncWrapper.execute(consumerAdapter);
        };
    }

    @Override
    public void onGuildJoin(final GuildJoinEvent event) {
        onGuildEvent(wrapper -> DiscordEntityCacheUtil.joinGuild(wrapper, event.getGuild(), this.entityClass), event);
    }

    @Override
    public void onGuildLeave(final GuildLeaveEvent event) {
        onGuildEvent(wrapper -> DiscordEntityCacheUtil.leaveGuild(wrapper, event.getGuild(), this.entityClass), event);
    }

    @Override
    public void onGenericGuildUpdate(final GenericGuildUpdateEvent event) {
        onGuildEvent(wrapper -> DiscordEntityCacheUtil.cacheGuild(wrapper, event.getGuild(), this.entityClass), event);
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        Consumer<DatabaseWrapper> action = wrapper -> {
            Collection<DatabaseException> exceptions = DiscordEntityCacheUtil.cacheAllGuilds(
                    wrapper, event.getJDA().getGuildCache().stream(), this.entityClass);
            for (DatabaseException e : exceptions) {
                log.error("Exception when caching all guilds of shard {} on reconnect",
                        event.getJDA().getShardInfo().getShardId(), e);
            }
        };
        submit(() -> this.wrapperAdapter.accept(action),
                e -> log.error("Failed to cache all guilds of shard {} on reconnect",
                        event.getJDA().getShardInfo().getShardId(), e)
        );
    }

    private void onGuildEvent(final Consumer<DatabaseWrapper> action, final GenericGuildEvent event) {
        submit(() -> this.wrapperAdapter.accept(action), e -> log.error("Failed to cache event {} for guild {}",
                event.getClass().getSimpleName(), event.getGuild().getIdLong(), e));
    }
}
