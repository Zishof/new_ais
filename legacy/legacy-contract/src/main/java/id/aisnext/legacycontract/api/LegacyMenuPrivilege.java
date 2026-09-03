package id.aisnext.legacycontract.api;

public record LegacyMenuPrivilege(long menuId, String label, String legacyUrl, long parentCode,
                                  long nodeCode, boolean readable, boolean creatable,
                                  boolean updatable, boolean deletable, boolean approvable,
                                  boolean rejectable) {}
