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

package space.npstr.sqlsauce.entities;

import space.npstr.sqlsauce.DatabaseException;
import space.npstr.sqlsauce.DatabaseWrapper;
import space.npstr.sqlsauce.converters.PostgresHStoreConverter;
import space.npstr.sqlsauce.fp.types.EntityKey;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by napster on 06.07.17.
 * <p>
 * Basic HStore table
 * <p>
 * JPA-only dependant = not Hibernate or other vendors dependant
 * <p>
 * The x makes it sound awesome and also prevents a name/type collision in postgres
 * <p>
 * todo: ideas for this class: explore possibilities of merging the giant blocks of methods in this class for
 * default / provided database by partially applying the data source
 */
@Entity
@Table(name = "hstorex")
public class Hstore extends SaucedEntity<String, Hstore> {

    @Transient
    public static final String DEFAULT_HSTORE_NAME = "default";

    //you are responsible for using unique names when you want to access unique hstores
    @Id
    @Column(name = "name", columnDefinition = "text")
    public String name;

    @Column(name = "hstorex", columnDefinition = "hstore")
    @Convert(converter = PostgresHStoreConverter.class)
    public final Map<String, String> hstore = new HashMap<>();

    //for jpa && sauced entity
    //prefer to use Hstore.load() to create on of these to avoid overwriting an existing one
    public Hstore() {
    }

    @Override
    @CheckReturnValue
    public Hstore setId(final String id) {
        this.name = id;
        return this;
    }

    @Override
    public String getId() {
        return this.name;
    }

    /**
     * @return itself for chaining calls
     */
    @CheckReturnValue
    public Hstore set(final String key, final String value) {
        this.hstore.put(key, value);
        return this;
    }

    /**
     * Intended as a finishing move, so no @CheckReturnValue annotation. Manually check the return value if you want
     * to keep using this Hstore
     */
    public Hstore setAndSave(final String key, final String value) throws DatabaseException {
        this.hstore.put(key, value);
        return this.save();
    }

    /**
     * @return the requested value
     */
    @CheckReturnValue
    public String get(final String key, final String defaultValue) {
        return this.hstore.getOrDefault(key, defaultValue);
    }

    /**
     * @return the requested value or null if it doesnt exist
     */
    @Nullable
    @CheckReturnValue
    public String get(final String key) {
        return this.hstore.getOrDefault(key, null);
    }


    //################################################################################
    //                 Static single connection convenience stuff
    //################################################################################

    /**
     * @return load a value from an hstore object
     */
    @CheckReturnValue
    public static String loadAndGet(final HstoreKey entityKey, final String key, final String defaultValue)
            throws DatabaseException {
        return Hstore.loadAndGet(getDefaultSauce(), entityKey, key, defaultValue);
    }

    /**
     * @return loads a value from the default hstore
     */
    @CheckReturnValue
    public static String loadAndGet(final String key, final String defaultValue) throws DatabaseException {
        return Hstore.loadAndGet(getDefaultSauce(), HstoreKey.DEFAULT, key, defaultValue);
    }

    /**
     * @return the default Hstore object
     */
    @CheckReturnValue
    public static Hstore load() throws DatabaseException {
        return SaucedEntity.load(getDefaultSauce(), HstoreKey.DEFAULT);
    }

    /**
     * Shortcut method to set a single value on a named hstore on the default database and save it
     */
    public static Hstore loadSetAndSave(final HstoreKey entityKey, final String key, final String value)
            throws DatabaseException {
        return loadSetAndSave(getDefaultSauce(), entityKey, key, value);
    }

    /**
     * Shortcut method to set a single value on the default hstore on the default database and save it
     */
    public static Hstore loadSetAndSave(final String key, final String value) throws DatabaseException {
        return loadSetAndSave(HstoreKey.DEFAULT, key, value);
    }

    /**
     * Apply some functions to the default Hstore of the default database and save it
     */
    public static Hstore loadApplyAndSave(final HstoreKey entityKey, final Function<Hstore, Hstore> transformation)
            throws DatabaseException {
        return loadApplyAndSave(getDefaultSauce(), entityKey, transformation);
    }

    /**
     * Apply some functions to the default Hstore of the default database and save it
     */
    public static Hstore loadApplyAndSave(final Function<Hstore, Hstore> transformation) throws DatabaseException {
        return loadApplyAndSave(HstoreKey.DEFAULT, transformation);
    }

    //################################################################################
    //                  Static convenience stuff with custom sauce
    //################################################################################

    /**
     * @return load a value from an hstore
     */
    @CheckReturnValue
    public static String loadAndGet(final DatabaseWrapper databaseWrapper, final HstoreKey entityKey, final String key,
                                    final String defaultValue) throws DatabaseException {
        return SaucedEntity.load(databaseWrapper, entityKey).hstore
                .getOrDefault(key, defaultValue);
    }

    /**
     * @return loads a value from the default hstore
     */
    @CheckReturnValue
    public static String loadAndGet(final DatabaseWrapper databaseWrapper, final String key, final String defaultValue)
            throws DatabaseException {
        return loadAndGet(databaseWrapper, HstoreKey.DEFAULT, key, defaultValue);
    }


    /**
     * @return the default Hstore object from the provided database
     */
    @CheckReturnValue
    public static Hstore load(final DatabaseWrapper databaseWrapper) throws DatabaseException {
        return SaucedEntity.load(databaseWrapper, HstoreKey.DEFAULT);
    }

    /**
     * Shortcut method to set a single value on a named hstore on the provided database and save it
     */
    public static Hstore loadSetAndSave(final DatabaseWrapper databaseWrapper, final HstoreKey entityKey,
                                        final String key, final String value) throws DatabaseException {
        return SaucedEntity.loadApplyAndSave(databaseWrapper, entityKey, setTransformation(key, value));
    }

    /**
     * Shortcut method to set a single value on the default hstore on the provided database and save it
     */
    public static Hstore loadSetAndSave(final DatabaseWrapper databaseWrapper, final String key, final String value)
            throws DatabaseException {
        return Hstore.loadApplyAndSave(databaseWrapper, setTransformation(key, value));
    }


    /**
     * Apply some functions to the default Hstore of the provided database and save it
     */
    public static Hstore loadApplyAndSave(final DatabaseWrapper databaseWrapper,
                                          final Function<Hstore, Hstore> transformation) throws DatabaseException {
        return SaucedEntity.loadApplyAndSave(databaseWrapper, HstoreKey.DEFAULT, transformation);
    }


    //################################################################################
    //                              Transformations
    //################################################################################
    //these can be used with the loadApplyAndSave methods

    /**
     * Function that set a key to a value on the applied hstore
     */
    public static Function<Hstore, Hstore> setTransformation(final String key, final String value) {
        return (hstore) -> hstore.set(key, value);
    }


    public static class HstoreKey extends EntityKey<String, Hstore> {

        public static final HstoreKey DEFAULT = HstoreKey.of(DEFAULT_HSTORE_NAME);

        public static HstoreKey of(final String name) {
            return new HstoreKey(name);
        }

        private HstoreKey(final String name) {
            super(name, Hstore.class);
        }
    }
}