package id.aisnext.identity.application;

/**
 * One authorized result returned by the extensible AIS Next global search.
 *
 * @param type human-readable result category
 * @param id stable source-system identifier
 * @param title display label
 * @param url safe AIS Next navigation target
 */
public record SearchResult(String type, String id, String title, String url) {
}
