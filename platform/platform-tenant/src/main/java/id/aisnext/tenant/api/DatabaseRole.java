package id.aisnext.tenant.api;

/** Logical database roles supported for each tenant. */
public enum DatabaseRole {
    /** Academic, identity, master, finance, and other structured tenant data. */
    CORE,
    /** Binary files, images, attachments, and streaming content. */
    FILE
}
