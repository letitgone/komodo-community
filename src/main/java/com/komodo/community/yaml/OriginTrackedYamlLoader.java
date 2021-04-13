package com.komodo.community.yaml;

import com.komodo.community.utils.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * @Author ZhangGJ
 * @Date 2021/04/12 22:13
 */
public class OriginTrackedYamlLoader {

    private List<DocumentMatcher> documentMatchers = Collections.emptyList();

    private boolean matchDefault = true;

    private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

    private String location;

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Map<String, Object>> load(String location) {
        setLocation(location);
        final List<Map<String, Object>> result = new ArrayList<>();
        process((properties, map) -> result.add(getFlattenedMap(map)));
        return result;
    }

    private void process(MatchCallback matchCallback) {
        Yaml yaml = createYaml();
        boolean found = process(matchCallback, yaml);
        if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND && found) {
            return;
        }
    }

    private Yaml createYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(options);
    }

    private boolean process(MatchCallback matchCallback, Yaml yaml) {
        int count = 0;
        try (Reader reader = new UnicodeReader(
                OriginTrackedYamlLoader.class.getClassLoader().getResourceAsStream(location))) {
            for (Object object : yaml.loadAll(reader)) {
                if (object != null && process(asMap(object), matchCallback)) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (count > 0);
    }

    private boolean process(Map<String, Object> map, MatchCallback callback) {
        Properties properties = createStringAdaptingProperties();
        properties.putAll(getFlattenedMap(map));
        if (this.documentMatchers.isEmpty()) {
            callback.process(properties, map);
            return true;
        }

        MatchStatus result = MatchStatus.ABSTAIN;
        for (DocumentMatcher matcher : this.documentMatchers) {
            MatchStatus match = matcher.matches(properties);
            result = MatchStatus.getMostSpecific(match, result);
            if (match == MatchStatus.FOUND) {
                callback.process(properties, map);
                return true;
            }
        }

        if (result == MatchStatus.ABSTAIN && this.matchDefault) {
            callback.process(properties, map);
            return true;
        }
        return false;
    }

    private Map<String, Object> asMap(Object object) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            result.put("document", object);
            return result;
        }
        Map<Object, Object> map = (Map<Object, Object>) object;
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                result.put("[" + key.toString() + "]", value);
            }
        });
        return result;
    }

    private final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source,
            String path) {
        source.forEach((key, value) -> {
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result,
                                Collections.singletonMap("[" + (count++) + "]", object), key);
                    }
                }
            } else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }

    private static Properties createStringAdaptingProperties() {
        return new SortedProperties(false) {
            @Override
            public String getProperty(String key) {
                Object value = get(key);
                return (value != null ? value.toString() : null);
            }
        };
    }

    public interface MatchCallback {

        /**
         * Process the given representation of the parsing results.
         *
         * @param properties the properties to process (as a flattened
         *                   representation with indexed keys in case of a collection or map)
         * @param map        the result map (preserving the original value structure
         *                   in the YAML document)
         */
        void process(Properties properties, Map<String, Object> map);
    }


    public interface DocumentMatcher {

        /**
         * Test if the given properties match.
         *
         * @param properties the properties to test
         * @return the status of the match
         */
        MatchStatus matches(Properties properties);
    }


    public enum MatchStatus {

        /**
         * A match was found.
         */
        FOUND,

        /**
         * No match was found.
         */
        NOT_FOUND,

        /**
         * The matcher should not be considered.
         */
        ABSTAIN;

        /**
         * Compare two {@link MatchStatus} items, returning the most specific status.
         */
        public static MatchStatus getMostSpecific(MatchStatus a, MatchStatus b) {
            return (a.ordinal() < b.ordinal() ? a : b);
        }
    }


    public enum ResolutionMethod {

        /**
         * Replace values from earlier in the list.
         */
        OVERRIDE,

        /**
         * Replace values from earlier in the list, ignoring any failures.
         */
        OVERRIDE_AND_IGNORE,

        /**
         * Take the first resource in the list that exists and use just that.
         */
        FIRST_FOUND
    }

}
