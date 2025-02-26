package graphql.cachecontrol;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.PublicApi;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static graphql.Assert.assertNotEmpty;
import static graphql.Assert.assertNotNull;
import static graphql.collect.ImmutableKit.map;

/**
 * Apollo has deprecated their Cache Control specification https://github.com/apollographql/apollo-cache-control
 * This has been deprecated/removed from Apollo some time.
 * Apollo now provides an alternative approach via the @cacheControl directive https://www.apollographql.com/docs/apollo-server/performance/caching/
 * We are deprecating CacheControl inside graphql-java and this will be deleted in a future release.
 *
 * This class implements the graphql Cache Control specification as outlined in https://github.com/apollographql/apollo-cache-control
 * <p>
 * To best use this class you need to pass a CacheControl object to each {@link graphql.schema.DataFetcher} and have them decide on
 * the caching hint values.
 * <p>
 * The easiest way to do this is create a CacheControl object at query start and pass it in as a "context" object via {@link ExecutionInput#getGraphQLContext()} and then have
 * each {@link graphql.schema.DataFetcher} that wants to make cache control hints use that.
 * <p>
 * Then at the end of the query you would call {@link #addTo(graphql.ExecutionResult)} to record the cache control hints into the {@link graphql.ExecutionResult}
 * extensions map as per the specification.
 */
@Deprecated
@PublicApi
public class CacheControl {

    public static final String CACHE_CONTROL_EXTENSION_KEY = "cacheControl";

    /**
     * If the scope is set to PRIVATE, this indicates anything under this path should only be cached per-user,
     * unless the value is overridden on a sub path. PUBLIC is the default and means anything under this path
     * can be stored in a shared cache.
     */
    public enum Scope {
        PUBLIC, PRIVATE
    }

    private static final class Hint {
        private final List<Object> path;
        private final Integer maxAge;
        private final Scope scope;

        private Hint(List<Object> path, Integer maxAge, Scope scope) {
            assertNotEmpty(path);
            this.path = path;
            this.maxAge = maxAge;
            this.scope = scope;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("path", path);
            if (maxAge != null) {
                map.put("maxAge", maxAge);
            }
            if (scope != null) {
                map.put("scope", scope.name());
            }
            return map;
        }
    }

    private final List<Hint> hints;

    private CacheControl() {
        hints = new CopyOnWriteArrayList<>();
    }


    /**
     * This creates a cache control hint for the specified path
     *
     * @param path   the path to the field that has the cache control hint
     * @param maxAge the caching time in seconds
     * @param scope  the scope of the cache control hint
     * @return this object builder style
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public CacheControl hint(ResultPath path, Integer maxAge, Scope scope) {
        assertNotNull(path);
        assertNotNull(scope);
        hints.add(new Hint(path.toList(), maxAge, scope));
        return this;
    }

    /**
     * This creates a cache control hint for the specified path
     *
     * @param path  the path to the field that has the cache control hint
     * @param scope the scope of the cache control hint
     * @return this object builder style
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public CacheControl hint(ResultPath path, Scope scope) {
        return hint(path, null, scope);
    }

    /**
     * This creates a cache control hint for the specified path
     *
     * @param path   the path to the field that has the cache control hint
     * @param maxAge the caching time in seconds
     * @return this object builder style
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public CacheControl hint(ResultPath path, Integer maxAge) {
        return hint(path, maxAge, Scope.PUBLIC);
    }

    /**
     * This creates a cache control hint for the specified field being fetched
     *
     * @param dataFetchingEnvironment the path to the field that has the cache control hint
     * @param maxAge                  the caching time in seconds
     * @param scope                   the scope of the cache control hint
     * @return this object builder style
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public CacheControl hint(DataFetchingEnvironment dataFetchingEnvironment, Integer maxAge, Scope scope) {
        assertNotNull(dataFetchingEnvironment);
        assertNotNull(scope);
        hint(dataFetchingEnvironment.getExecutionStepInfo().getPath(), maxAge, scope);
        return this;
    }

    /**
     * This creates a cache control hint for the specified field being fetched with a PUBLIC scope
     *
     * @param dataFetchingEnvironment the path to the field that has the cache control hint
     * @param maxAge                  the caching time in seconds
     * @return this object builder style
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public CacheControl hint(DataFetchingEnvironment dataFetchingEnvironment, Integer maxAge) {
        hint(dataFetchingEnvironment, maxAge, Scope.PUBLIC);
        return this;
    }

    /**
     * This creates a cache control hint for the specified field being fetched with a specified scope
     *
     * @param dataFetchingEnvironment the path to the field that has the cache control hint
     * @param scope                   the scope of the cache control hint
     * @return this object builder style
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public CacheControl hint(DataFetchingEnvironment dataFetchingEnvironment, Scope scope) {
        return hint(dataFetchingEnvironment, null, scope);
    }

    /**
     * Creates a new CacheControl object that can be used to trick caching hints
     *
     * @return the new object
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public static CacheControl newCacheControl() {
        return new CacheControl();
    }

    /**
     * This will record the values in the cache control object into the provided execution result object which creates a new {@link graphql.ExecutionResult}
     * object back out
     *
     * @param executionResult the starting execution result object
     * @return a new execution result with the hints in the extensions map.
     *
     * @deprecated - Apollo has deprecated the Cache Control specification
     */
    @Deprecated
    public ExecutionResult addTo(ExecutionResult executionResult) {
        return ExecutionResultImpl.newExecutionResult()
                .from(executionResult)
                .addExtension(CACHE_CONTROL_EXTENSION_KEY, hintsToCacheControlProperties())
                .build();
    }

    private Map<String, Object> hintsToCacheControlProperties() {
        List<Map<String, Object>> recordedHints = map(hints, Hint::toMap);

        Map<String, Object> cacheControl = new LinkedHashMap<>();
        cacheControl.put("version", 1);
        cacheControl.put("hints", recordedHints);
        return cacheControl;
    }
}
