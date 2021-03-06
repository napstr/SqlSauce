/*
 * MIT License
 *
 * Copyright (c) 2017 Dennis Neufeld
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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.sqlsauce.AsyncDatabaseWrapper;
import space.npstr.sqlsauce.DatabaseWrapper;
import space.npstr.sqlsauce.entities.discord.BaseDiscordUser;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by napster on 20.10.17.
 * <p>
 * Caches entities that extend DiscordUser
 * <p>
 * Limitation: Currently only events relevant for DiscordUser are listened to. Extending classes might be interested in
 * more events.
 */
public class UserMemberCachingListener<E extends BaseDiscordUser<E> & CacheableUser<E>> extends CachingListener<E, UserMemberCachingListener<E>> {

    private static final Logger log = LoggerFactory.getLogger(GuildCachingListener.class);

    private final Consumer<Consumer<DatabaseWrapper>> wrapperAdapter;

    public UserMemberCachingListener(DatabaseWrapper wrapper, final Class<E> entityClass) {
        super(entityClass);
        this.wrapperAdapter = wrapperConsumer -> wrapperConsumer.accept(wrapper);
    }

    public UserMemberCachingListener(AsyncDatabaseWrapper asyncWrapper, final Class<E> entityClass) {
        super(entityClass);
        this.wrapperAdapter = wrapperConsumer -> {
            Function<DatabaseWrapper, Void> consumerAdapter = wrapper -> {
                wrapperConsumer.accept(wrapper);
                return null;
            };
            asyncWrapper.execute(consumerAdapter);
        };
    }

    //user events

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        onUserEvent(event);
    }

    @Override
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {
        onUserEvent(event);
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        onUserEvent(event);
    }

    //member events

    @Override
    public void onGuildMemberUpdateNickname(final GuildMemberUpdateNicknameEvent event) {
        onMemberEvent(event, event.getMember());
    }

    @Override
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        onMemberEvent(event, event.getMember());
    }

    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {
        Member member = event.getMember();
        if (member != null) {
            onMemberEvent(event, member);
        }
    }

    private void onUserEvent(final GenericUserEvent event) {
        Consumer<DatabaseWrapper> action = wrapper -> DiscordEntityCacheUtil.cacheUser(wrapper, event.getUser(), this.entityClass);
        submit(() -> this.wrapperAdapter.accept(action),
                e -> log.error("Failed to cache event {} for user {}",
                        event.getClass().getSimpleName(), event.getUser().getIdLong(), e));
    }

    private void onMemberEvent(GenericGuildEvent event, final Member member) {
        Consumer<DatabaseWrapper> action = wrapper -> DiscordEntityCacheUtil.cacheMember(wrapper, member, this.entityClass);
        submit(() -> this.wrapperAdapter.accept(action),
                e -> log.error("Failed to cache event {} for member {} of guild {}",
                        event.getClass().getSimpleName(), member.getUser().getIdLong(), member.getGuild().getIdLong(), e));
    }


    //batch events

    @Override
    public void onGuildJoin(final GuildJoinEvent event) {
        Consumer<DatabaseWrapper> action = wrapper -> DiscordEntityCacheUtil.cacheAllMembers(
                wrapper, event.getGuild().getMemberCache().stream(), this.entityClass);
        submit(() -> this.wrapperAdapter.accept(action),
                e -> log.error("Failed to mass cache members on event {} for guild {}",
                        event.getClass().getSimpleName(), event.getGuild().getIdLong()));
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        //not doing anything here. the load would be way too high with the current DiscordUser#cacheAll code
    }
}
