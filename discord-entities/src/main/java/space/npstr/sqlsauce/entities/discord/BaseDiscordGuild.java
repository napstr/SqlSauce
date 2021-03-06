/*
 * MIT License
 *
 * Copyright (c) 2017-2018, Dennis Neufeld
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

package space.npstr.sqlsauce.entities.discord;

import org.hibernate.annotations.NaturalId;
import space.npstr.sqlsauce.entities.SaucedEntity;

import javax.annotation.CheckReturnValue;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

/**
 * Created by napster on 25.01.18.
 * <p>
 * Defines just the id for a discord guild entity and nothing else. Great as a base class for an entity saving more
 * information about a guild, without the caching cruft of {@link DiscordGuild}
 */
@MappedSuperclass
public abstract class BaseDiscordGuild<S extends SaucedEntity<Long, S>> extends SaucedEntity<Long, S> {


    @Id
    @NaturalId
    @Column(name = "guild_id", nullable = false)
    protected long guildId;

    @Override
    @CheckReturnValue
    public S setId(final Long guildId) {
        this.guildId = guildId;
        return getThis();
    }

    @Override
    public Long getId() {
        return getGuildId();
    }

    public long getGuildId() {
        return this.guildId;
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof BaseDiscordGuild) && ((BaseDiscordGuild) obj).guildId == this.guildId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.guildId);
    }
}
