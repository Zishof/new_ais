package id.aisnext.tenant.api;

/** Identifies which application is currently permitted to serve a strangler route. */
public enum RouteOwner {
    /** The route remains owned by the operational legacy application. */
    LEGACY,
    /** The route is served by AIS Next. */
    NEXT
}
